package com.neniukov.tradebot.data.model.response

import kotlinx.serialization.Serializable

@Serializable
data class KLineSocketWrapper(
    val topic: String,
    val type: String,
    val data: List<KLineSocketResponse>,
    val ts: Long
)