package com.neniukov.tradebot.data.model.mapper

import com.neniukov.tradebot.domain.model.Candle

fun mapToCandle(list: List<String>): Candle {
    return Candle(
        startTime = list[0],
        openPrice = list[1].toDouble(),
        highPrice = list[2].toDouble(),
        lowPrice = list[3].toDouble(),
        closePrice = list[4].toDouble(),
        volume = list[5].toDouble(),
        turnover = list[6]
    )
}