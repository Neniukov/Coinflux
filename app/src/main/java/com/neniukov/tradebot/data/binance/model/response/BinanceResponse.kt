package com.neniukov.tradebot.data.binance.model.response

import com.google.gson.annotations.SerializedName

data class BinanceResponse<T>(
    @SerializedName("retCode") val retCode: Int,
    @SerializedName("retMsg") val retMsg: String,
    @SerializedName("result") val result: T?
)