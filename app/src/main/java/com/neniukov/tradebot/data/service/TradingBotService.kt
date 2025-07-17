package com.neniukov.tradebot.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.neniukov.tradebot.MainActivity
import com.neniukov.tradebot.R
import com.neniukov.tradebot.data.binance.BinanceRepository
import com.neniukov.tradebot.data.binance.mapper.toDomain
import com.neniukov.tradebot.data.binance.model.response.BinancePositionResponse
import com.neniukov.tradebot.data.binance.model.response.OpenOrderResponse
import com.neniukov.tradebot.data.managers.OrderManager
import com.neniukov.tradebot.data.model.mapper.mapToCandle
import com.neniukov.tradebot.domain.model.Candle
import com.neniukov.tradebot.domain.model.CurrentPosition
import com.neniukov.tradebot.domain.model.Side
import com.neniukov.tradebot.domain.model.Ticker
import com.neniukov.tradebot.domain.model.TradingState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
/*
- если нет тейк профита то сделать 2 тейк профита
- если 2 тейк профита и размер верный то скип
- если 2 тайк профита и размер неверный то отменить и сделать 2 новых тейка
- если 1 тейк профит и размер верный то скип
- если 1 тейк профит и размер неверный то отменить и сделать 2 новых тейка
 */
@AndroidEntryPoint
class TradingBotService : Service() {

    @Inject
    lateinit var repository: BinanceRepository

    @Inject
    lateinit var orderManager: OrderManager

    private val currentPosition = MutableStateFlow(Side.None)
    val currentPositionFlow = currentPosition.asStateFlow()

    private val positions = MutableStateFlow<List<CurrentPosition>?>(null)
    val positionsFlow = positions.asStateFlow()

    private val error = MutableStateFlow<String?>(null)
    val errorsFlow = error.asStateFlow()

    private val walletBalance = MutableStateFlow<String?>(null)
    val walletBalanceFlow = walletBalance.asStateFlow()

    private var socketConnection = MutableStateFlow<Boolean?>(null)
    val socketConnectionFlow = socketConnection.asStateFlow()

    private val binder = LocalBinder()

    private var job: Job? = null
    private var jobPositions: Job? = null

    private val channelId = "trade_bot_channel"
    private val channelName = "Trade Bot"

    private var cryptoData: Ticker? = null

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        handleError(e)
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO + coroutineExceptionHandler)

    private var isActiveAutomatedBot = false

    private var state = TradingState()

    private val SMA_PERIOD = 15
    private val FIRST_TAKE_PROFIT_PERCENT = 0.01 // 1%
    private val SECOND_TAKE_PROFIT_PERCENT = 0.015 // 1.5%
    private val TAKE_PROFIT_PERCENT_FOR_BAD_POSITION = 0.003 // 1%

    inner class LocalBinder : Binder() {
        fun getService(): TradingBotService = this@TradingBotService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        loadPositions()
    }

    override fun onDestroy() {
        job?.cancel()
        jobPositions?.cancel()
        super.onDestroy()
    }

    private fun startForegroundService() {

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(channelId) == null) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Bot inactive")
            .setSmallIcon(R.drawable.ic_notification)
            .build()

        startForeground(1, notification)
    }

    private fun updateNotification(title: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setSmallIcon(R.drawable.ic_notification)
            .setSilent(true)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }

    fun setData(data: Ticker) {
        currentPosition.value = Side.None
        cryptoData = data
    }

    fun startBot() {
        state = state.copy(isActive = true)
        job = scope.launch {
            while (isActive) {
                cryptoData?.let { cryptoData ->
                    try {
                        val list = repository.getMarkPriceKline(
                            symbol = cryptoData.symbol,
                            interval = orderManager.interval,
                        )
                        val candles = list.map { mapToCandle(it) }
                        runTradingLogic(candles)
                        updateNotification("Bot active")
                    } catch (e: Exception) {
                        error.value = e.message.orEmpty()
                        delay(2000)
                        error.value = null
                    }
                }

                delay(orderManager.requestDelay)
            }
        }
    }

    fun startAutomatedBot(amountInUSD: String) {
        scope.launch {
            isActiveAutomatedBot = true
//            repository.loadPriceForTickers()
            updateNotification("Bot active")
        }
    }

    fun stopAutomatedBot() {
        isActiveAutomatedBot = false
        updateNotification("Bot inactive")
    }

    fun stopBot() {
        state = state.copy(isActive = false)
        job?.cancel()
        job = null
        currentPosition.value = Side.None
        cryptoData = null

        updateNotification("Bot inactive")
    }

    private fun loadPositions() {
        jobPositions = scope.launch {
            while (isActive) {
                val balance = repository.getBalance()
                walletBalance.emit(balance)
                val allPositions = repository.getAllPositions()
                var openOrders = listOf<OpenOrderResponse>()
                if (allPositions.isNotEmpty()) {
                    val firstPosition = allPositions.firstOrNull()
                    if (firstPosition != null) {
                        state = state.copy(
                            isInPosition = true,
                            initialEntryPrice = firstPosition.entryPrice.toDouble(),
                            currentAverageEntryPrice = firstPosition.entryPrice.toDouble(),
                            totalPositionQuantity = firstPosition.positionAmt.toDouble(),
                            baseOrderQuantity = cryptoData?.qty?.toDouble() ?: 0.0
                        )
                        openOrders = repository.getOpenOrders(firstPosition.symbol)
                        setTP(firstPosition, openOrders)
                        val markPrice = firstPosition.markPrice.toDoubleOrNull()
                        val entryPrice = firstPosition.entryPrice.toDoubleOrNull()
                        if (markPrice != null && entryPrice != null) {
                            addPosition(firstPosition)
                        }
                        val title =
                            "${firstPosition.symbol} Pnl: ${"%.2f".format(firstPosition.unRealizedProfit?.toDouble())}$"
                        updateNotification(title)
                    }
                    positions.emit(allPositions.map { position ->
                        openOrders.firstOrNull { it.symbol == position.symbol }?.let { order ->
                            position.toDomain(order.price)
                        } ?: run { position.toDomain() }
                    })
                    delay(orderManager.requestDelayForOpeningPosition)
                } else {
                    positions.emit(emptyList())
                    state = state.copy(isInPosition = false)
                    updateNotification("No active positions")
                    delay(orderManager.requestDelay)
                }
            }
        }
    }

    private suspend fun setTP(position: BinancePositionResponse, openOrders: List<OpenOrderResponse>) {
        val numberOfInputs = state.totalPositionQuantity / state.baseOrderQuantity
        if (numberOfInputs > 30) {
            if (openOrders.size == 1 && openOrders.first().origQty == position.positionAmt) return
            if (openOrders.isNotEmpty()) {
                repository.cancelOpenOrders(position.symbol)
            }
            val takeProfitPrice = state.currentAverageEntryPrice * (1 + TAKE_PROFIT_PERCENT_FOR_BAD_POSITION)
            repository.setTP(
                symbol = position.symbol,
                side = "SELL",
                quantity = position.positionAmt,
                closePrice = takeProfitPrice.toString()
            )
            return
        }

        if (openOrders.isEmpty()) {
            setTwoTakeProfits(position)
            return
        }

        if (openOrders.size == 2 && openOrders.all { it.origQty != position.positionAmt }) {
            repository.cancelOpenOrders(position.symbol)
            setTwoTakeProfits(position)
            return
        }

        if (openOrders.size == 1 && openOrders.first().origQty != position.positionAmt) {
            repository.cancelOpenOrders(position.symbol)
            setTwoTakeProfits(position)
        }
    }

    private suspend fun setTwoTakeProfits(position: BinancePositionResponse) {
        val firstTakeProfit = state.currentAverageEntryPrice * (1 + FIRST_TAKE_PROFIT_PERCENT)
        val secondTakeProfit = state.currentAverageEntryPrice * (1 + SECOND_TAKE_PROFIT_PERCENT)
        val halfQuantity = position.positionAmt.toDouble() / 2
        repository.setTP(
            symbol = position.symbol,
            side = "SELL",
            quantity = halfQuantity.toString(),
            closePrice = firstTakeProfit.toString()
        )
        repository.setTP(
            symbol = position.symbol,
            side = "SELL",
            quantity = halfQuantity.toString(),
            closePrice = secondTakeProfit.toString()
        )
    }

    fun stop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1)

        stopSelf()
    }

    private fun handleError(throwable: Throwable) {
        throwable.printStackTrace()
        if (throwable.message?.contains("HTTP 401") == true) {
            error.value = "API keys are invalid or expired. Please re-enter your API key and secret key."
        } else {
            error.value = throwable.message
        }
    }


    private fun calculateSMA(candles: List<Candle>, period: Int): Double {
        if (candles.size < period) {
            return 0.0
        }
        val closes = candles.takeLast(period).map { it.closePrice }
        return closes.average()
    }

    // Основная функция для запуска торговой логики
    private suspend fun runTradingLogic(candles: List<Candle>) {
        if (!state.isActive) {
            Log.e("botservice", "Bot is not active. Exiting runTradingLogic.")
            return
        }

        try {
            if (candles.isEmpty()) return

            // Убедимся, что мы обрабатываем только новые свечи
            val latestCandle = candles.last()
            val latestCandleStartTimeMillis = latestCandle.startTime.toLong()
            if (latestCandleStartTimeMillis <= state.lastCandleTime) {
                Log.e(
                    "botservice",
                    "No new candle or already processed. Last processed: ${state.lastCandleTime}, Current: $latestCandleStartTimeMillis"
                )
                return
            }
            state = state.copy(lastCandleTime = latestCandleStartTimeMillis)

            val currentPrice = latestCandle.closePrice
            val sma = calculateSMA(
                candles.dropLast(1),
                SMA_PERIOD
            ) // SMA рассчитываем на предыдущих 15 свечах, исключая текущую формирующуюся
            Log.e("botservice", "Current Price: $currentPrice, 15-bar SMA: $sma")

            if (!state.isInPosition) {
                // Логика для открытия первой позиции LONG
                if (currentPrice > sma) {
                    openInitialLongPosition(currentPrice)
                } else {
                    Log.e("botservice", "Price is not above SMA. Waiting for entry signal.")
                }
            }
        } catch (e: Exception) {
            Log.e("botservice", "Error in runTradingLogic: ${e.message}")
            e.printStackTrace()
        }
    }

    private suspend fun openInitialLongPosition(currentPrice: Double) {
        Log.e("botservice", "Signal to open initial LONG position detected. Current Price: $currentPrice")

        val orderResult =
            repository.placeMarketOrder(cryptoData?.symbol.orEmpty(), "Buy", cryptoData?.qty.orEmpty())

        if (orderResult?.orderId != null) {
            state = state.copy(
                isInPosition = true,
                initialEntryPrice = currentPrice,
                currentAverageEntryPrice = currentPrice,
                totalPositionQuantity = cryptoData?.qty?.toDouble() ?: 0.0,
                baseOrderQuantity = cryptoData?.qty?.toDouble() ?: 0.0,
            )
        } else {
            Log.e("botservice", "Failed to open initial LONG position. Status: $orderResult")
        }
    }

    private suspend fun addPosition(position: BinancePositionResponse) {
        // Здесь мы добавляем позицию с половиной объема от базового ордера
        val orderQuantity = state.baseOrderQuantity / 2
        val markPrice = position.markPrice.toDouble()
        val entryPrice = position.entryPrice.toDouble()

        val numberOfInputs = state.totalPositionQuantity / state.baseOrderQuantity
        val percentageOfEntryPrice = if (numberOfInputs > 10) 0.03 else 0.02
        val differenceForEntry = entryPrice * percentageOfEntryPrice
        if (entryPrice - markPrice < differenceForEntry) {
            Log.e(
                "botservice",
                "Not adding to position. entryPrice: $entryPrice, markPrice: $markPrice, differenceForEntry: $differenceForEntry"
            )
            return
        }
        if (orderQuantity <= 0) {
            Log.e("botservice", "Base order quantity is zero or negative. Cannot add to position.")
            return
        }

        Log.e("botservice", "Adding to position. Quantity: $orderQuantity, Current Price: $position")
        repository.placeMarketOrder(cryptoData?.symbol.orEmpty(), "Buy", orderQuantity.toString())
    }
}