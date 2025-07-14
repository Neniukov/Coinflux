package com.neniukov.tradebot.data.model.response

data class TickerResponse(
    val symbol: String,
    val leverageFilter: LeverageFilter?
)

data class TickersResponse(
    val list: List<TickerResponse>,
)
