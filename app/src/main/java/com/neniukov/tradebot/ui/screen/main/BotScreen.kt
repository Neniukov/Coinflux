package com.neniukov.tradebot.ui.screen.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neniukov.tradebot.ui.base.BaseViews
import com.neniukov.tradebot.ui.screen.main.widget.BotContent
import com.neniukov.tradebot.ui.screen.main.widget.LogInContent
import com.neniukov.tradebot.ui.theme.EndGradient
import com.neniukov.tradebot.ui.theme.StartGradient

@Composable
fun BotScreen(
    onStatisticsClick: () -> Unit
) {

    val viewModel = hiltViewModel<BotViewModel>()

    Box(
        modifier = Modifier
            .background(brush = Brush.linearGradient(colors = listOf(StartGradient, EndGradient)))
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp)) {
            val showLogIn by viewModel.showLogInFlow.collectAsStateWithLifecycle()
            if (showLogIn) {
                LogInContent(viewModel)
            } else {
                BotContent(viewModel, onStatisticsClick)
            }
        }

        BaseViews(viewModel)
    }

    val context = LocalContext.current

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }
    val permissionRequest =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            hasNotificationPermission = result
        }

    LaunchedEffect(Unit) {
        if (!hasNotificationPermission) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.startService()
    }
}