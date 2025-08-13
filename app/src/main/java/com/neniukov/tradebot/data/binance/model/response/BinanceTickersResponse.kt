package com.neniukov.tradebot.data.binance.model.response

import com.google.gson.annotations.SerializedName

data class BinanceTickersResponse(
    @SerializedName("symbols")
    val symbols: List<BinanceTickerResponse>?
)

data class BinanceTickerResponse(
    @SerializedName("symbol")
    val symbol: String,
)