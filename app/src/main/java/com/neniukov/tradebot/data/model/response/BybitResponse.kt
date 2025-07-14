package com.neniukov.tradebot.data.model.response

import com.google.gson.annotations.SerializedName

data class BybitResponse<T>(
    @SerializedName("retCode") val retCode: Int,
    @SerializedName("retMsg") val retMsg: String,
    @SerializedName("result") val result: T?
)