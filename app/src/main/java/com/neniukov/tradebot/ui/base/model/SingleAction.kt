package com.neniukov.tradebot.ui.base.model

interface SingleAction {
    object None : SingleAction
    object Done : SingleAction
    class Result(val data: Any) : SingleAction
}