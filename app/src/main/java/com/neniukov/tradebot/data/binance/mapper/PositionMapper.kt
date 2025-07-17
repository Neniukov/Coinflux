package com.neniukov.tradebot.data.binance.mapper

import com.neniukov.tradebot.data.binance.model.response.BinancePositionResponse
import com.neniukov.tradebot.data.model.response.PositionResponse
import com.neniukov.tradebot.domain.model.CurrentPosition
import com.neniukov.tradebot.domain.model.Side

fun BinancePositionResponse.toDomain(takeProfit: String? = null): CurrentPosition {
    val positionIM = entryPrice.toDouble() * positionAmt.toDouble() / leverage.toDouble()
    return CurrentPosition(
        avgPrice = entryPrice,
        leverage = leverage,
        markPrice = markPrice,
        positionIM = positionIM.toString(),
        side = if (positionAmt.toDouble() > 0) Side.Buy.name else Side.Sell.name,
        size = positionAmt,
        stopLoss = "",
        symbol = symbol,
        takeProfit = takeProfit.orEmpty(),
        unrealisedPnl = unRealizedProfit.orEmpty()
    )
}