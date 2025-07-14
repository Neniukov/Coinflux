package com.neniukov.tradebot.data.model.response


import com.google.gson.annotations.SerializedName

data class PositionResponse(
    @SerializedName("adlRankIndicator")
    val adlRankIndicator: Int,
    @SerializedName("autoAddMargin")
    val autoAddMargin: Int,
    @SerializedName("avgPrice")
    val avgPrice: String,
    @SerializedName("bustPrice")
    val bustPrice: String,
    @SerializedName("createdTime")
    val createdTime: String,
    @SerializedName("cumRealisedPnl")
    val cumRealisedPnl: String,
    @SerializedName("curRealisedPnl")
    val curRealisedPnl: String,
    @SerializedName("isReduceOnly")
    val isReduceOnly: Boolean,
    @SerializedName("leverage")
    val leverage: String,
    @SerializedName("leverageSysUpdatedTime")
    val leverageSysUpdatedTime: String,
    @SerializedName("liqPrice")
    val liqPrice: String,
    @SerializedName("markPrice")
    val markPrice: String,
    @SerializedName("mmrSysUpdatedTime")
    val mmrSysUpdatedTime: String,
    @SerializedName("positionBalance")
    val positionBalance: String,
    @SerializedName("positionIM")
    val positionIM: String,
    @SerializedName("positionIdx")
    val positionIdx: Int,
    @SerializedName("positionMM")
    val positionMM: String,
    @SerializedName("positionStatus")
    val positionStatus: String,
    @SerializedName("positionValue")
    val positionValue: String,
    @SerializedName("riskId")
    val riskId: Int,
    @SerializedName("riskLimitValue")
    val riskLimitValue: String,
    @SerializedName("seq")
    val seq: Long,
    @SerializedName("sessionAvgPrice")
    val sessionAvgPrice: String,
    @SerializedName("side")
    val side: String,
    @SerializedName("size")
    val size: String,
    @SerializedName("stopLoss")
    val stopLoss: String,
    @SerializedName("symbol")
    val symbol: String,
    @SerializedName("takeProfit")
    val takeProfit: String,
    @SerializedName("tpslMode")
    val tpslMode: String,
    @SerializedName("tradeMode")
    val tradeMode: Int,
    @SerializedName("trailingStop")
    val trailingStop: String,
    @SerializedName("unrealisedPnl")
    val unrealisedPnl: String,
    @SerializedName("updatedTime")
    val updatedTime: String
)