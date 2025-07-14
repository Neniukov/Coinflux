package com.neniukov.tradebot.domain.model

data class Ticker(
    val symbol: String,
    val qty: String,
    val minLeverage: String = "1",
    val maxLeverage: String = "100",
    val leverage: Int = 10,
)

fun defaultData() =
    Ticker(
        symbol = "BTCUSDT",
        qty = "0.007"
    )