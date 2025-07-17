package com.neniukov.tradebot.data.binance.model.response

data class LeverageResponse(
    val leverage: Int,
    val maxNotionalValue: String,
    val symbol: String
)