package com.neniukov.tradebot.data.model.request

data class LeverageRequest(
    val symbol: String,
    val buyLeverage: String,
    val sellLeverage: String,
    val category: String = "linear",
)