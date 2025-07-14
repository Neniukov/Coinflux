package com.neniukov.tradebot.data.model.response

data class OrderResponse(
    val retCode: Int,
    val retMsg: String,
    val result: Any
)