package com.neniukov.tradebot.data.model.request

data class OrderRequest(
    val category: String = "linear",
    val symbol: String,
    val side: String, // "Buy" или "Sell"
    val orderType: String = "Market",
    val qty: String,
    val price: String? = null,
    val takeProfit: String? = null,
    val stopLoss: String? = null,
//    val positionIdx: Int = 0 // 0: one-way mode, 1: hedge-mode Buy side, 2: hedge-mode Sell side
//    val timeInForce: String = "GTC",
//    val price: String,
//    val orderLinkId: String
)
