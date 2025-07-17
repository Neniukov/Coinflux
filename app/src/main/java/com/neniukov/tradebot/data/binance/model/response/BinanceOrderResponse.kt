package com.neniukov.tradebot.data.binance.model.response

data class BinanceOrderResponse(
    val orderId: Long,
    val symbol: String,
    val status: String,
    val executedQty: String,
    val cumQuote: String,
    val avgPrice: String,
    val side: String,
    val type: String
)