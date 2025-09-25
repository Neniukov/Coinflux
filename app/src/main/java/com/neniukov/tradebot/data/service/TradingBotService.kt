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
import com.neniukov.tradebot.data.binance.model.response.OpenOrderResponse
import com.neniukov.tradebot.data.managers.OrderManager
import com.neniukov.tradebot.data.model.mapper.mapToCandle
import com.neniukov.tradebot.domain.model.Candle
import com.neniukov.tradebot.domain.model.CurrentPosition
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
    lateinit var repository: BinanceRepository

    @Inject
    lateinit var orderManager: OrderManager

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

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent

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
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (notificationManager.getNotificationChannel(channelId) == null) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setSilent(true)
            .setOngoing(true)
            .setContentIntent(pendingIntent)

        startForeground(1, notificationBuilder.setContentTitle(getString(R.string.bot_inactive)).build())
    }

    private fun updateNotification(title: String) {
        val notification = notificationBuilder.setContentTitle(title).build()
        notificationManager.notify(1, notification)
    }

    fun setData(data: Ticker) {
        cryptoData = data
    }

    fun startBot() {
        state = state.copy(isActive = true)
        updateNotification(getString(R.string.bot_active))
        job = scope.launch {
            while (isActive) {
                cryptoData?.let { cryptoData ->
                    try {
                        val list = repository.getMarkPriceKline(cryptoData.symbol, orderManager.interval,)
                        val candles = list.map { mapToCandle(it) }
                        runTradingLogic(candles)
                    } catch (e: Exception) {
                        error.value = e.message.orEmpty()
                        handleError(e)
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
            updateNotification(getString(R.string.bot_active))
        }
    }

    fun stopAutomatedBot() {
        isActiveAutomatedBot = false
        updateNotification(getString(R.string.bot_inactive))
    }

    fun stopBot() {
        state = state.copy(isActive = false)
        job?.cancel()
        job = null
        cryptoData = null
        updateNotification(getString(R.string.bot_inactive))
    }

    private fun loadPositions() {
        jobPositions = scope.launch {
            while (isActive) {
                try {
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
                            orderManager.setTP(state, firstPosition, openOrders)
                            orderManager.addPosition(state, firstPosition)

                            val title = "${firstPosition.symbol} Pnl: ${"%.2f".format(firstPosition.unRealizedProfit?.toDouble())}$"
                            updateNotification(title)
                        }

                        positions.emit(allPositions.map { position ->
                            openOrders.filter { it.symbol == position.symbol }
                                .sumOf { calculateTakeProfitUsd(position.entryPrice, it.price, it.origQty) }
                                .let { takeProfit -> position.toDomain("%.2f".format(takeProfit), cryptoData?.qty?.toDouble()) }
                        })

                        delay(orderManager.requestDelayForOpeningPosition)
                    } else {
                        positions.emit(emptyList())
                        state = state.copy(isInPosition = false)
                        updateNotification(getString(R.string.no_active_positions))
                        delay(orderManager.requestDelay)
                    }

                } catch (e: Exception) {
                    error.value = e.message.orEmpty()
                    handleError(e)
                    delay(2000)
                    error.value = null
                }
            }
        }

    }

    private fun calculateTakeProfitUsd(
        avgPrice: String,
        tpPrice: String,
        size: String,
    ): Double {
        if(tpPrice.isBlank()) return 0.0
        return (tpPrice.toDouble() - avgPrice.toDouble()) * size.toDouble()
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
            error.value = getString(R.string.api_keys_expired_or_invalid)
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
        try {
            if (candles.isEmpty()) return

            // Убедимся, что мы обрабатываем только новые свечи
            val latestCandle = candles.last()
            val latestCandleStartTimeMillis = latestCandle.startTime.toLong()
            if (latestCandleStartTimeMillis <= state.lastCandleTime) return
            state = state.copy(lastCandleTime = latestCandleStartTimeMillis)

            val currentPrice = latestCandle.closePrice
            val sma = calculateSMA(
                candles.dropLast(1),
                SMA_PERIOD
            ) // SMA рассчитываем на предыдущих 15 свечах, исключая текущую формирующуюся

            if (!state.isInPosition && currentPrice > sma) {
                openInitialLongPosition(currentPrice)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            handleError(e)
        }
    }

    private suspend fun openInitialLongPosition(currentPrice: Double) {
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
            Log.e("botservice", "Failed to open initial LONG position")
        }
    }

    companion object {
        private const val SMA_PERIOD = 15
    }
}