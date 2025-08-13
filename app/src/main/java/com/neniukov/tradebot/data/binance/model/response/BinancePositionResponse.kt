package com.neniukov.tradebot.data.binance.model.response

data class BinancePositionResponse(
    val symbol: String,
    val positionAmt: String,    // >0 = лонг, <0 = шорт, "0" = нет позиции
    val entryPrice: String,
    val unRealizedProfit: String?,
    val leverage: String,
    val positionSide: String,
    val isolated: Boolean,
    val markPrice: String
)