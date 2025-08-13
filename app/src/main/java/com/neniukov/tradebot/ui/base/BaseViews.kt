package com.neniukov.tradebot.ui.base

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neniukov.tradebot.ui.base.model.ProgressState

@Composable
fun BaseViews(
    viewModel: BaseViewModel,
    progressView: @Composable () -> Unit = { ProgressDialog() }
) {
    val visibleState = remember { MutableTransitionState(false).apply { targetState = false } }
    val errorState = viewModel.errorFlow.collectAsStateWithLifecycle()
    val progressState = viewModel.progressState.collectAsStateWithLifecycle()

    ErrorWidget(visibleState, errorState)

    if (progressState.value == ProgressState.Show) progressView()
}

@Composable
fun ProgressDialog() {
    Dialog(
        onDismissRequest = { },
        DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(100.dp)
                .background(White, shape = RoundedCornerShape(8.dp))
        ) {
            CircularProgressIndicator(color = Black)
        }
    }
}

@Composable
fun ErrorWidget(
    visibleState: MutableTransitionState<Boolean>,
    errorMessage: State<String?>
) {
    AnimatedVisibility(
        modifier = Modifier.statusBarsPadding(),
        visibleState = visibleState,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            colors = CardDefaults.cardColors(containerColor = Red),
        ) {
            Text(
                modifier = Modifier.padding(12.dp),
                text = errorMessage.value.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = White
            )
        }
    }
    visibleState.targetState = !errorMessage.value.isNullOrBlank()
}