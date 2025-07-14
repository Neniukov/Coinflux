package com.neniukov.tradebot.data.model.response

import com.google.gson.annotations.SerializedName

data class ListPositionResponse(
    @SerializedName("nextPageCursor")
    val nextPageCursor: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("list")
    val list: List<PositionResponse>,
)
