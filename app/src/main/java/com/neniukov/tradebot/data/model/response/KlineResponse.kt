package com.neniukov.tradebot.data.model.response

import com.google.gson.annotations.SerializedName

data class KlineResponse(
    @SerializedName("symbol")
    val symbol: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("list")
    val list: List<List<String>>,
)