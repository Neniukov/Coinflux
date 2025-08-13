package com.neniukov.tradebot.data.binance.model.response

data class OpenOrderResponse(
    val orderId: Long,
    val symbol: String,
    val side: String,
    val type: String,
    val price: String,
    val origQty: String,
    val executedQty: String,
    val status: String,
    val timeInForce: String,
    val stopPrice: String,
    val reduceOnly: Boolean
)