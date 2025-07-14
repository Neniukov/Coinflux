package com.neniukov.tradebot.data.model.response


import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class TickerUpdateResponse(
    @SerializedName("ask1Price")
    val ask1Price: String?,
    @SerializedName("ask1Size")
    val ask1Size: String?,
    @SerializedName("bid1Price")
    val bid1Price: String?,
    @SerializedName("bid1Size")
    val bid1Size: String?,
    @SerializedName("curPreListingPhase")
    val curPreListingPhase: String?,
    @SerializedName("fundingRate")
    val fundingRate: String?,
    @SerializedName("highPrice24h")
    val highPrice24h: String?,
    @SerializedName("indexPrice")
    val indexPrice: String?,
    @SerializedName("lastPrice")
    val lastPrice: String?,
    @SerializedName("lowPrice24h")
    val lowPrice24h: String?,
    @SerializedName("markPrice")
    val markPrice: String?,
    @SerializedName("nextFundingTime")
    val nextFundingTime: String?,
    @SerializedName("openInterest")
    val openInterest: String?,
    @SerializedName("openInterestValue")
    val openInterestValue: String?,
    @SerializedName("preOpenPrice")
    val preOpenPrice: String?,
    @SerializedName("preQty")
    val preQty: String?,
    @SerializedName("prevPrice1h")
    val prevPrice1h: String?,
    @SerializedName("prevPrice24h")
    val prevPrice24h: String?,
    @SerializedName("price24hPcnt")
    val price24hPcnt: String?,
    @SerializedName("symbol")
    val symbol: String?,
    @SerializedName("tickDirection")
    val tickDirection: String?,
    @SerializedName("turnover24h")
    val turnover24h: String?,
    @SerializedName("volume24h")
    val volume24h: String?,
    @SerializedName("deliveryTime")
    val deliveryTime: String?,
    @SerializedName("basisRate")
    val basisRate: String?,
    @SerializedName("deliveryFeeRate")
    val deliveryFeeRate: String?,
    @SerializedName("predictedDeliveryPrice")
    val predictedDeliveryPrice: String?,
    @SerializedName("basis")
    val basis: String?,
)