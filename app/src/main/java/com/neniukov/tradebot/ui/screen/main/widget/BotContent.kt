package com.neniukov.tradebot.ui.screen.main.widget

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neniukov.tradebot.ui.screen.main.BotViewModel

@Composable
fun BotContent(viewModel: BotViewModel, onStatisticsClick: () -> Unit) {

    val isLoggedIn = viewModel.isLoggedInFlow.collectAsStateWithLifecycle()
    if (isLoggedIn.value == false) {
        BotContentWithoutKeys(viewModel)
    } else if (isLoggedIn.value == true) {
        BotContentWithKeys(viewModel, onStatisticsClick)
    }
}