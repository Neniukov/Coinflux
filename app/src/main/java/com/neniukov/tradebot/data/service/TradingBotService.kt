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
import com.neniukov.tradebot.data.managers.OrderManager
import com.neniukov.tradebot.data.model.mapper.mapToCandle
import com.neniukov.tradebot.data.model.response.PositionResponse
import com.neniukov.tradebot.data.repository.BybitRepository
import com.neniukov.tradebot.domain.model.Candle
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

@AndroidEntryPoint
class TradingBotService : Service() {

    @Inject
    lateinit var repository: BybitRepository

    @Inject
    lateinit var orderManager: OrderManager

    private val currentPosition = MutableStateFlow(Side.None)
    val currentPositionFlow = currentPosition.asStateFlow()

    private val positions = MutableStateFlow<List<PositionResponse>?>(null)
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
    private val TAKE_PROFIT_PERCENT = 0.01 // 1%

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
                        val result = repository.getMarkPriceKline(
                            symbol = cryptoData.symbol,
                            interval = orderManager.interval,
                        )

                        val list = result?.list
                        val candles = list?.map { mapToCandle(it) }.orEmpty()
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
            repository.loadPriceForTickers()
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
                val allPositions = repository.getAllPositions()
                val firstPosition = allPositions.firstOrNull()
                if (firstPosition != null) {
                    state = state.copy(
                        isInPosition = true,
                        initialEntryPrice = firstPosition.avgPrice.toDouble(),
                        currentAverageEntryPrice = firstPosition.avgPrice.toDouble(),
                        totalPositionQuantity = firstPosition.size.toDouble(),
                        baseOrderQuantity = cryptoData?.qty?.toDouble() ?: 0.0
                    )
                    val takeProfitPrice = state.currentAverageEntryPrice * (1 + TAKE_PROFIT_PERCENT)
                    repository.setTP(firstPosition, takeProfitPrice)
                    val markPrice = firstPosition.markPrice.toDoubleOrNull()
                    val entryPrice = firstPosition.avgPrice.toDoubleOrNull()
                    if (markPrice != null && entryPrice != null) {
                        addPosition(firstPosition)
                    }
                    val title = "${firstPosition.symbol} Pnl: ${"%.2f".format(firstPosition.unrealisedPnl.toDouble())}$"
                    updateNotification(title)
                }
                val balance = repository.getBalance()
                positions.emit(allPositions)
                walletBalance.emit(balance)
                if (allPositions.isEmpty()) {
                    state = state.copy(isInPosition = false)
                }
                if (allPositions.isEmpty()) {
                    delay(orderManager.requestDelay)
                } else {
                    delay(orderManager.requestDelayForOpeningPosition)
                }
            }
        }
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
            return 0.0 // Недостаточно данных
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
            if (candles.isEmpty()) {
                Log.e("botservice", "No candles data received.")
                return
            }

            // Убедимся, что мы обрабатываем только новые свечи
            val latestCandle = candles.last()
            val latestCandleStartTimeMillis = latestCandle.startTime.toLong()
            if (latestCandleStartTimeMillis <= state.lastCandleTime) {
                Log.e("botservice", "No new candle or already processed. Last processed: ${state.lastCandleTime}, Current: $latestCandleStartTimeMillis")
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

        if (orderResult?.retCode == 0) {
            state = state.copy(
                isInPosition = true,
                initialEntryPrice = currentPrice,
                currentAverageEntryPrice = currentPrice,
                totalPositionQuantity = cryptoData?.qty?.toDouble() ?: 0.0,
                baseOrderQuantity = cryptoData?.qty?.toDouble() ?: 0.0,
            )
        } else {
            Log.e("botservice", "Failed to open initial LONG position. Status: ${orderResult?.retMsg}")
        }
    }

    private suspend fun addPosition(position: PositionResponse) {
        // Здесь мы добавляем позицию с тем же базовым объемом
        val orderQuantity = state.baseOrderQuantity
        val markPrice = position.markPrice.toDouble()
        val entryPrice = position.avgPrice.toDouble()

        val numberOfInputs = state.totalPositionQuantity / state.baseOrderQuantity
        val percentageOfEntryPrice = if (numberOfInputs > 10) 0.03 else 0.02
        val differenceForEntry = entryPrice * percentageOfEntryPrice
        if (entryPrice - markPrice < differenceForEntry) {
            Log.e("botservice", "Not adding to position. entryPrice: $entryPrice, markPrice: $markPrice, differenceForEntry: $differenceForEntry")
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