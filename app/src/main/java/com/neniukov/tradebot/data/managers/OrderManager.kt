package com.neniukov.tradebot.data.managers

import com.neniukov.tradebot.data.model.response.PositionResponse
import com.neniukov.tradebot.domain.model.Candle
import com.neniukov.tradebot.domain.model.Side
import kotlin.math.abs

interface OrderManager {
    val interval: Int
        get() = 5
    val requestDelay: Long
        get() = 10_000
    val requestDelayForOpeningPosition: Long
        get() = 5_000

    fun getSideForOpeningOrder(candles: List<Candle>): Side
    fun getTPAndSL(candles: List<Candle>, currentPosition: PositionResponse): Pair<Double, Double>
    fun calculateATR(candles: List<Candle>, period: Int = 14): Double {
        if (candles.size < period + 1) return 0.0

        val trList = mutableListOf<Double>()
        for (i in 1 until candles.size) {
            val high = candles[i].highPrice
            val low = candles[i].lowPrice
            val prevClose = candles[i - 1].closePrice

            val tr = maxOf(
                high - low,
                abs(high - prevClose),
                abs(low - prevClose)
            )
            trList.add(tr)
        }

        return trList.takeLast(period).average()
    }
}