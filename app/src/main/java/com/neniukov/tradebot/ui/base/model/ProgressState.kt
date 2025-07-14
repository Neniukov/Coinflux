package com.neniukov.tradebot.ui.base.model

sealed class ProgressState {
    data object Show : ProgressState()

    data object Hide : ProgressState()
}