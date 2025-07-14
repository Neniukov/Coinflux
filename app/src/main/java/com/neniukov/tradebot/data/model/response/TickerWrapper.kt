package com.neniukov.tradebot.data.model.response

import kotlinx.serialization.Serializable

@Serializable
data class TickerWrapper(
    val topic: String,
    val type: String,
    val data: TickerUpdateResponse,
    val cs: Long,
    val ts: Long
)

