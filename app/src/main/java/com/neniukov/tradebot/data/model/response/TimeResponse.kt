package com.neniukov.tradebot.data.model.response

import com.google.gson.annotations.SerializedName

data class TimeResponse(
    @SerializedName("retCode") val retCode: Int,
    @SerializedName("retMsg") val retMsg: String,
    @SerializedName("result") val result: TimeSeconds
)

data class TimeSeconds(
    @SerializedName("timeSecond") val timeSecond: Long
)