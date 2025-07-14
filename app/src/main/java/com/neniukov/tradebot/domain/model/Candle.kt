package com.neniukov.tradebot.domain.model

data class Candle(
    val startTime: String,
    val openPrice: Double,
    val highPrice: Double,
    val lowPrice: Double,
    val closePrice: Double,
    val volume: Double,
    val turnover: String,
)