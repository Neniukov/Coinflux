package com.neniukov.tradebot.domain.model

data class CurrentPosition(
    val avgPrice: String,
    val leverage: String,
    val markPrice: String,
    val positionIM: String,
    val side: String,
    val size: String,
    val stopLoss: String,
    val symbol: String,
    val takeProfit: String,
    val unrealisedPnl: String
)