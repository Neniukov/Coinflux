package com.neniukov.tradebot.data.managers

import android.util.Log
import com.neniukov.tradebot.data.model.request.OrderRequest
import com.neniukov.tradebot.data.model.response.KLineSocketResponse
import com.neniukov.tradebot.data.model.response.PositionResponse
import com.neniukov.tradebot.data.model.response.TickerUpdateResponse
import com.neniukov.tradebot.data.repository.BybitRepository
import com.neniukov.tradebot.domain.model.PriceSnapshot
import com.neniukov.tradebot.domain.model.Side
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.pow

class AutomatedBotManager @Inject constructor(
    private val repository: BybitRepository
) {

    private val positions = mutableListOf<String>()

    private val lastEntryTime = mutableMapOf<String, Long>()

    private val priceHistory = mutableMapOf<String, MutableList<PriceSnapshot>>()

    private var amountInUSD: Double = 0.0
    private val leverage = 10

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)


    fun handleTicker(ticker: TickerUpdateResponse, price: Double, timestamp: Long) {
        if (amountInUSD == 0.0) return
        val symbol = ticker.symbol ?: return

        // Не торговать тем, что уже в позиции
        if (positions.contains(symbol.lowercase()) || positions.size > 4) return

        // Объём: фильтрация слаболиквидных монет
        val turnover = ticker.turnover24h?.toDoubleOrNull() ?: 0.0
        if (turnover < 1_000_000) return

        // История цены
        val history = priceHistory.getOrPut(symbol) { mutableListOf() }
        history.add(PriceSnapshot(price, timestamp))

        // Обрезаем старые значения
        val cutoff = timestamp - 10_000
        history.removeAll { it.timestamp < cutoff }

        if (history.size < 5) return

        // Фильтр волатильности — нужен импульс
        val stdDev = history.map { it.price }.standardDeviation()
        if (stdDev < price * 0.0015) return  // менее 0.3% — пропускаем

        // Сравниваем текущее изменение
        val old = history.first()
        val changePercent = (price - old.price) / old.price * 100

        if (abs(changePercent) > 2) {
            val sma = calculateSMA(symbol) ?: return
            if (price > sma && changePercent > 0) {
                createOrder(price.toString(), symbol, Side.Buy)
            } else if (price < sma && changePercent < 0) {
                createOrder(price.toString(), symbol, Side.Sell)
            }
        }
    }

    private fun List<Double>.standardDeviation(): Double {
        val mean = average()
        return kotlin.math.sqrt(sumOf { (it - mean).pow(2) } / size)
    }

    private fun calculateSMA(symbol: String): Double? {
        val prices = priceHistory[symbol]?.map { it.price } ?: return null
        if (prices.size < 5) return null
        return prices.average()
    }

    private fun createOrder(price: String, symbol: String, side: Side) {
        scope.launch(Dispatchers.IO) {
            Log.e("AutomatedBot", "createOrder: $price $symbol $side")
            positions.add(symbol.lowercase())
            val entryPrice = price.toDouble()

            val riskPercent = 0.01  // 1% стоп-лосс
            val stopLoss = if (side == Side.Buy) {
                roundDown(entryPrice * (1 - riskPercent))
            } else {
                roundDown(entryPrice * (1 + riskPercent))
            }

            val takeProfit = if (side == Side.Buy) {
                roundDown(entryPrice * (1 + riskPercent * 2))  // RR = 2
            } else {
                roundDown(entryPrice * (1 - riskPercent * 2))
            }
            val qty = roundDown(amountInUSD * leverage / price.toDouble(), 4)

            val request = OrderRequest(
                symbol = symbol,
                side = side.name,
                orderType = "Limit",
                qty = (if (entryPrice > 1) qty else qty.toInt()).toString(),
                price = price,
                takeProfit = takeProfit.toString(),
                stopLoss = stopLoss.toString(),
            )
            repository.setLeverage(symbol, leverage.toString())
            val order = repository.placeLimitOrder(request)
            if (order?.retCode == 0) {
                Log.e("AutomatedBot", "Order placed: $side $qty of $symbol at $price")
            } else {
                Log.e("AutomatedBot", "Failed to place order: ${order?.retMsg}")
            }
        }
    }

    fun setAmountForTrading(amountInUSD: String) {
        this.amountInUSD = amountInUSD.toDouble()
    }

    fun setPositions(positions: List<PositionResponse>) {
        this.positions.clear()
        this.positions.addAll(positions.map { it.symbol.lowercase() })
    }

    private fun roundDown(value: Double, decimals: Int = 5): Double {
        val factor = 10.0.pow(decimals.toDouble())
        return floor(value * factor) / factor
    }

    fun setTPAndSL(positionResponse: PositionResponse) {

        val entryPrice = positionResponse.avgPrice.toDouble()

        val riskPercent = 0.01  // 1% стоп-лосс
        val stopLoss = if (positionResponse.side == Side.Buy.name) {
            roundDown(entryPrice * (1 - riskPercent))
        } else {
            roundDown(entryPrice * (1 + riskPercent))
        }

        val takeProfit = if (positionResponse.side == Side.Buy.name) {
            roundDown(entryPrice * (1 + riskPercent * 2))  // RR = 2
        } else {
            roundDown(entryPrice * (1 - riskPercent * 2))
        }

        scope.launch {
            repository.setTPAndSL(positionResponse.symbol, takeProfit.toString(), stopLoss.toString())
        }
    }
}