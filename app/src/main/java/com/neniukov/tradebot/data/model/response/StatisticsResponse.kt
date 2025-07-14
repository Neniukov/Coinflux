package com.neniukov.tradebot.data.model.response

import com.google.gson.annotations.SerializedName

data class ListStatisticsResponse(
    @SerializedName("list")
    val list: List<StatisticsResponse>
)

data class StatisticsResponse(
    @SerializedName("symbol")
    val symbol: String,
    @SerializedName("side")
    val side: String,
    @SerializedName("qty")
    val qty: String,
    @SerializedName("orderPrice")
    val orderPrice: String,
    @SerializedName("avgEntryPrice")
    val avgEntryPrice: String,
    @SerializedName("avgExitPrice")
    val avgExitPrice: String,
    @SerializedName("closedPnl")
    val closedPnl: String,
    @SerializedName("leverage")
    val leverage: String,
    @SerializedName("openFee")
    val openFee: String,
    @SerializedName("closeFee")
    val closeFee: String,
    @SerializedName("createdTime")
    val createdTime: String
)