package com.neniukov.tradebot.data.model.request

import kotlinx.serialization.Serializable

@Serializable
data class WebSocketSubscribeRequest(
    val op: String,
    val args: List<String>
)