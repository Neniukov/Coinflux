package com.neniukov.tradebot.data.managers.impl

import com.neniukov.tradebot.data.managers.OrderManager
import com.neniukov.tradebot.data.model.response.PositionResponse
import com.neniukov.tradebot.domain.model.Candle
import com.neniukov.tradebot.domain.model.Side
import javax.inject.Inject

class EmaLongOrderManager @Inject constructor(): OrderManager {

    override val interval: Int
        get() = 15

    override val requestDelay: Long = 20_000

    override val requestDelayForOpeningPosition: Long
        get() = 30_000

    override fun getSideForOpeningOrder(candles: List<Candle>): Side {
        TODO("Not yet implemented")
    }

    override fun getTPAndSL(candles: List<Candle>, currentPosition: PositionResponse): Pair<Double, Double> {
        TODO("Not yet implemented")
    }
}