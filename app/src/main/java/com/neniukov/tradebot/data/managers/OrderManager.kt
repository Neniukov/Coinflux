package com.neniukov.tradebot.data.managers

import com.neniukov.tradebot.data.binance.model.response.BinancePositionResponse
import com.neniukov.tradebot.data.binance.model.response.OpenOrderResponse
import com.neniukov.tradebot.domain.model.TradingState

interface OrderManager {
    val interval: Int
        get() = 5
    val requestDelay: Long
        get() = 10_000
    val requestDelayForOpeningPosition: Long
        get() = 5_000
    suspend fun setTP(state: TradingState, position: BinancePositionResponse, openOrders: List<OpenOrderResponse>)
    suspend fun addPosition(state: TradingState, position: BinancePositionResponse)
}