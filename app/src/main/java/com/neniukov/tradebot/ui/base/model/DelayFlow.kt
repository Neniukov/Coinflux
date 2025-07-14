package com.neniukov.tradebot.ui.base.model

sealed class DelayFlow(val delay: kotlin.Long) {
    data object Short : DelayFlow(500)
    data object Long : DelayFlow(3000)
    data class Custom(val time: kotlin.Long) : DelayFlow(time)
}