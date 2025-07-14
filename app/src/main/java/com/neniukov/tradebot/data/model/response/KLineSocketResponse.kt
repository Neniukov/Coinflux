package com.neniukov.tradebot.data.model.response


import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class KLineSocketResponse(
    @SerializedName("close")
    val close: String,
    @SerializedName("confirm")
    val confirm: Boolean,
    @SerializedName("end")
    val end: Long,
    @SerializedName("high")
    val high: String,
    @SerializedName("interval")
    val interval: String,
    @SerializedName("low")
    val low: String,
    @SerializedName("open")
    val open: String,
    @SerializedName("start")
    val start: Long,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("turnover")
    val turnover: String,
    @SerializedName("volume")
    val volume: String
)