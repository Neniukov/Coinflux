package com.neniukov.tradebot.data.managers.impl

import com.neniukov.tradebot.data.managers.OrderManager
import com.neniukov.tradebot.data.model.response.PositionResponse
import com.neniukov.tradebot.domain.model.Candle
import com.neniukov.tradebot.domain.model.Side
import javax.inject.Inject
import kotlin.math.abs


/*
rsi < 40
rsi > 60
если EMA7 просто выше EMA14 и RSI подтверждает тренд
если последняя свеча больше средней за 10 свечей
 */
class EmaRsiOrderManager @Inject constructor(): OrderManager {

    override val interval: Int
        get() = 3
    override val requestDelay: Long = 20_000

    override fun getSideForOpeningOrder(candles: List<Candle>): Side {
        if (candles.size < 15) return Side.None

        val closePrices = candles.map { it.closePrice }

        val ema7 = calculateEMA(closePrices, 7)
        val ema14 = calculateEMA(closePrices, 14)
        val rsi = calculateRSI(closePrices, 14)

        val candleBodies = candles.map { abs(it.openPrice - it.closePrice) }
        val avgBody = candleBodies.takeLast(10).average()
        val lastBody = candleBodies.last()

        val lastEma7 = ema7.lastOrNull() ?: return Side.None
        val lastEma14 = ema14.lastOrNull() ?: return Side.None
        val lastRsi = rsi.lastOrNull() ?: return Side.None

        val emaUp = lastEma7 > lastEma14
        val emaDown = lastEma7 < lastEma14

        val strongCandle = lastBody > avgBody * 1.2

        if (lastRsi < 35 && emaUp) return Side.Buy
        if (lastRsi > 65 && emaDown) return Side.Sell

        if (emaUp && strongCandle) return Side.Buy
        if (emaDown && strongCandle) return Side.Sell

        return Side.None
    }

    override fun getTPAndSL(candles: List<Candle>, currentPosition: PositionResponse): Pair<Double, Double> {
        val entryPrice = currentPosition.avgPrice.toDouble()
        val atr = calculateATR(candles)

        val tpMultiplier = 1.7
        val slMultiplier = 1.0

        val (tp, sl) = if (currentPosition.side == Side.Buy.name) {
            (entryPrice + atr * tpMultiplier) to (entryPrice - atr * slMultiplier)
        } else {
            (entryPrice - atr * tpMultiplier) to (entryPrice + atr * slMultiplier)
        }
        return tp to sl
    }

    private fun calculateRSI(prices: List<Double>, period: Int): List<Double> {
        val rsi = mutableListOf<Double>()
        var gainSum = 0.0
        var lossSum = 0.0

        for (i in 1 until period) {
            val delta = prices[i] - prices[i - 1]
            if (delta >= 0) gainSum += delta else lossSum -= delta
        }

        gainSum /= period
        lossSum /= period

        var rs = if (lossSum == 0.0) 100.0 else gainSum / lossSum
        rsi.add(100 - (100 / (1 + rs)))

        for (i in period until prices.size) {
            val delta = prices[i] - prices[i - 1]
            val gain = if (delta >= 0) delta else 0.0
            val loss = if (delta < 0) -delta else 0.0

            gainSum = (gainSum * (period - 1) + gain) / period
            lossSum = (lossSum * (period - 1) + loss) / period

            rs = if (lossSum == 0.0) 100.0 else gainSum / lossSum
            rsi.add(100 - (100 / (1 + rs)))
        }

        return rsi
    }


    private fun calculateEMA(prices: List<Double>, period: Int): List<Double> {
        if (prices.size < period) return emptyList()

        val ema = mutableListOf<Double>()
        val k = 2.0 / (period + 1) // Коэффициент сглаживания

        // Начинаем с простой SMA
        var prevEma = prices.take(period).average()
        ema.add(prevEma)

        for (i in period until prices.size) {
            val currentEma = (prices[i] - prevEma) * k + prevEma
            ema.add(currentEma)
            prevEma = currentEma
        }

        return ema
    }
}