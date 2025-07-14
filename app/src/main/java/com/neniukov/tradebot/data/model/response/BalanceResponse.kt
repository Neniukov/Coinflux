package com.neniukov.tradebot.data.model.response

data class BalanceResponse(
    val totalEquity: String
)

data class ListBalanceResponse(
    val list: List<BalanceResponse>
)