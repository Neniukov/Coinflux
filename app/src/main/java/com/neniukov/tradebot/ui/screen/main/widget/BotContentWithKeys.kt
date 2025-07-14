package com.neniukov.tradebot.ui.screen.main.widget

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.neniukov.tradebot.R
import com.neniukov.tradebot.data.model.response.PositionResponse
import com.neniukov.tradebot.ui.dialog.DialogData
import com.neniukov.tradebot.ui.dialog.ConfirmationDialog
import com.neniukov.tradebot.ui.screen.main.BotViewModel
import com.neniukov.tradebot.ui.theme.EndGradient
import com.neniukov.tradebot.ui.theme.Gray
import com.neniukov.tradebot.ui.theme.Green
import com.neniukov.tradebot.ui.theme.Red
import com.neniukov.tradebot.ui.theme.Yellow

@Composable
fun BotContentWithKeys(viewModel: BotViewModel, onStatisticsClick: () -> Unit) {
    val positions = viewModel.positionsFlow.collectAsState()
    var isChecked by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var clickedPosition by remember { mutableStateOf<PositionResponse?>(null) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item(key = "logout") {
            LogoutView(isChecked, viewModel, viewModel::logout)
        }

        item(key = "balance") {
            BalanceView(viewModel)
        }

        item(key = "bot settings") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(id = R.string.currency_mode),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    modifier = Modifier.padding(end = 8.dp),
                    text = stringResource(id = R.string.manual),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.sp,
                    color = Color.White
                )
                Switch(
                    checked = isChecked,
                    onCheckedChange = { isChecked = it },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = Gray,
                        uncheckedTrackColor = Gray,
                        checkedThumbColor = Yellow,
                        uncheckedThumbColor = Yellow,
                        uncheckedBorderColor = Gray,
                    )
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(id = R.string.auto),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.sp,
                    color = Yellow
                )
            }

            BotSettingsView(!isChecked, Modifier.animateItem(), viewModel)
        }

        item(key = "searching_positions" + positions.value?.size) {
            val isWorking by viewModel.isWorkingFlow.collectAsState()
            SearchAnimationView(Modifier.animateItem(), isWorking)
        }

        positionsView(
            positions = positions,
            onClosePosition = {
                clickedPosition = it
                showDialog = true
            },
            onStatisticsClick = onStatisticsClick,
        )

        item(key = "bottom_space") {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    val dialogData = remember {
        DialogData(
            title = R.string.close_position,
            message = R.string.close_position_message,
            confirmButtonText = R.string.close_position,
            dismissButtonText = R.string.cancel
        )
    }

    ConfirmationDialog(
        showDialog = showDialog,
        dialogData = dialogData,
        onDismiss = { showDialog = false },
        onConfirm = {
            showDialog = false
            clickedPosition?.let(viewModel::closePosition)
        })
}

@Composable
private fun LogoutView(showConnection: Boolean, viewModel: BotViewModel, onLogoutClick: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.your_balance),
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 12.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.weight(1f))

        if (showConnection) {
            val connection by viewModel.socketConnectionFlow.collectAsState()
            Canvas(modifier = Modifier
                .padding(top = 6.dp)
                .size(8.dp), onDraw = {
                drawCircle(
                    color = if (connection == true) Green else Red,
                    radius = 4.dp.toPx()
                )
            })

            Text(
                modifier = Modifier.padding(start = 4.dp, end = 16.dp, top = 4.dp),
                text = if (connection == true) stringResource(id = R.string.connected) else stringResource(id = R.string.disconnected),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.sp,
                color = Color.White
            )
        }

        Icon(
            modifier = Modifier.clickable { showDialog = true },
            painter = painterResource(id = R.drawable.ic_logout),
            contentDescription = "",
            tint = Color.White
        )
    }

    val dialogData = remember {
        DialogData(
            title = R.string.logout,
            message = R.string.logout_message,
            confirmButtonText = R.string.exit,
            dismissButtonText = R.string.cancel
        )
    }

    ConfirmationDialog(
        showDialog = showDialog,
        dialogData = dialogData,
        onDismiss = { showDialog = false },
        onConfirm = {
            showDialog = false
            onLogoutClick()
        })
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun BalanceView(viewModel: BotViewModel) {
    Row {
        val balance by viewModel.walletBalanceFlow.collectAsStateWithLifecycle()

        if (balance == null) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = EndGradient,
                strokeWidth = 2.dp
            )
        } else {
            val previousBalance = remember { mutableStateOf(balance) }
            val targetBalance = balance


            val maxLength = maxOf(previousBalance.value!!.length, targetBalance!!.length)

            for (i in 0 until maxLength) {
                val oldChar = previousBalance.value!!.getOrNull(i) ?: ' '
                val newChar = targetBalance.getOrNull(i) ?: ' '

                AnimatedContent(
                    targetState = newChar,
                    transitionSpec = {
                        if (oldChar < newChar) {
                            slideInVertically { height -> height } + fadeIn() with
                                    slideOutVertically { height -> -height } + fadeOut()
                        } else {
                            slideInVertically { height -> -height } + fadeIn() with
                                    slideOutVertically { height -> height } + fadeOut()
                        }.using(
                            SizeTransform(clip = false)
                        )
                    },
                    label = "digitAnimation"
                ) { char ->
                    Text(
                        text = char.toString(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 16.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            LaunchedEffect(balance) {
                previousBalance.value = balance
            }
        }
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = stringResource(id = R.string.usdt).uppercase(),
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 16.sp,
            color = Yellow
        )
    }
}

@Composable
fun SearchAnimationView(modifier: Modifier, isWorking: Boolean) {

    AnimatedVisibility(
        visible = isWorking,
        modifier = modifier
            .padding(top = 8.dp)
            .fillMaxWidth(),
        enter = fadeIn(tween(600)) + expandIn(tween(600)),
        exit = shrinkOut(tween(600)) + fadeOut(tween(600))
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                modifier = Modifier
                    .padding(top = 4.dp, start = 32.dp, end = 32.dp)
                    .fillMaxWidth(),
                text = stringResource(id = R.string.searching_positions),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
            )

            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.searching_anim))
            val progress by animateLottieCompositionAsState(
                composition,
                iterations = LottieConstants.IterateForever
            )

            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(80.dp, 16.dp)
            )
        }
    }
}