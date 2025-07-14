package com.neniukov.tradebot.data.model.request

import com.google.gson.annotations.SerializedName

data class StopLossRequest(
    @SerializedName("category")
    val category: String = "linear",
    @SerializedName("symbol")
    val symbol: String,
    @SerializedName("takeProfit")
    val takeProfit: String,
    @SerializedName("stopLoss")
    val stopLoss: String? = null,
    @SerializedName("positionIdx")
    val positionIdx: Int? = 0,
    @SerializedName("tpslMode")
    val tpslMode: String? = null,
    @SerializedName("tpTriggerBy")
    val tpTriggerBy: String? = null,
    @SerializedName("tpOrderType")
    val tpOrderType: String? = null,
    @SerializedName("tpLimitPrice")
    val tpLimitPrice: String? = null,
    @SerializedName("tpSize")
    val tpSize: String? = null
)