package com.neniukov.tradebot.data.binance.model.response

data class BinanceBalance(
    val accountAlias: String,
    val asset: String,
    val balance: String,
    val crossWalletBalance: String,
    val crossUnPnl: String,
    val availableBalance: String,
    val updateTime: Long
)