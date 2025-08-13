package com.neniukov.tradebot.data.binance.model.response

data class BinanceLeverageBracketResponse(
    val symbol: String,
    val brackets: List<Bracket>
)

data class Bracket(
    val bracket: Int,
    val initialLeverage: Int,
    val notionalCap: Double,
    val maintMarginRatio: Double
)