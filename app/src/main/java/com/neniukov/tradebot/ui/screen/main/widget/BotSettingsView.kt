package com.neniukov.tradebot.ui.screen.main.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neniukov.tradebot.R
import com.neniukov.tradebot.ui.dialog.SearchableListBottomSheet
import com.neniukov.tradebot.ui.screen.main.BotViewModel
import com.neniukov.tradebot.ui.theme.Gray
import com.neniukov.tradebot.ui.theme.SetupColor
import com.neniukov.tradebot.ui.theme.TextFieldColor

@Composable
fun BotSettingsView(isManualBot: Boolean, modifier: Modifier, viewModel: BotViewModel) {
    var showTickersDialog by remember { mutableStateOf(false) }
    val tickers by viewModel.tickersFlow.collectAsState()
    val isLoggedIn by viewModel.isLoggedInFlow.collectAsState()

    val scaleXManualBot = remember { Animatable(1f) }

    LaunchedEffect(isManualBot) {
        if (!isManualBot) {
            scaleXManualBot.animateTo(0f, animationSpec = tween(400))
        } else {
            scaleXManualBot.animateTo(1f, animationSpec = tween(400))
        }
    }

    Column(
        modifier = modifier
            .padding(top = 16.dp)
            .fillMaxWidth()
            .background(color = SetupColor.copy(0.6f), shape = RoundedCornerShape(38.dp))
            .padding(bottom = 12.dp)
    ) {
        val isWorking by viewModel.isWorkingFlow.collectAsState()
        val ticker by viewModel.tickerFlow.collectAsState()
        val startIsEnabled by remember {
            derivedStateOf {
                if (scaleXManualBot.value == 1f) {
                    ticker.qty.isNotBlank() && isLoggedIn == true
                } else {
                    val balance =
                        viewModel.walletBalanceFlow.value?.replace(',', '.')?.toDoubleOrNull() ?: 0.0
                    val qty = ticker.qty.toDoubleOrNull() ?: -1.0
                    ticker.qty.isNotBlank() && isLoggedIn == true && balance >= qty
                }
            }
        }
        // Ticker, amount, leverage section
        Box(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .background(
                    color = SetupColor.copy(alpha = if (isWorking) 0.5f else 1f),
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(horizontal = 8.dp, vertical = 24.dp)
        ) {

            Column {
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(if (isManualBot) 8.dp else 0.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(scaleXManualBot.value.coerceAtLeast(0.001f))
                    ) {

                        AnimatedVisibility(
                            visible = scaleXManualBot.value > 0.5f,
                            enter = expandHorizontally(tween(50)),
                            exit = shrinkHorizontally(tween(50))
                        ) {
                            Text(
                                modifier = Modifier.padding(start = 12.dp),
                                text = stringResource(id = R.string.ticket),
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 12.sp,
                                color = Gray.copy(alpha = if (isWorking) 0.5f else 1f)
                            )

                            Text(
                                modifier = Modifier
                                    .alpha(scaleXManualBot.value)
                                    .padding(top = 16.dp)
                                    .fillMaxWidth()
                                    .background(
                                        color = TextFieldColor.copy(alpha = if (isWorking) 0.5f else 1f),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 18.dp)
                                    .clickable(
                                        enabled = !isWorking,
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { showTickersDialog = true }),
                                text = ticker.symbol.uppercase(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 16.sp,
                                color = White.copy(alpha = if (isWorking) 0.5f else 1f),
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .fillMaxWidth(),
                            text = stringResource(id = if (isManualBot) R.string.amount else R.string.amount_in_usdt),
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 12.sp,
                            color = Gray.copy(alpha = if (isWorking) 0.5f else 1f)
                        )

                        val fieldColor = TextFieldColor.copy(alpha = if (isWorking) 0.5f else 1f)

                        OutlinedTextField(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .fillMaxWidth()
                                .background(
                                    color = TextFieldColor.copy(alpha = if (isWorking) 0.5f else 1f),
                                    shape = RoundedCornerShape(20.dp)
                                ),
                            value = ticker.qty,
                            onValueChange = if (!isWorking) viewModel::updateQty else { _ -> },
                            enabled = !isWorking,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = White.copy(alpha = if (isWorking) 0.5f else 1f)),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = fieldColor,
                                unfocusedContainerColor = fieldColor,
                                disabledContainerColor = fieldColor,
                                focusedBorderColor = fieldColor,
                                unfocusedBorderColor = fieldColor,
                            ),
                            shape = RoundedCornerShape(20.dp),
                            maxLines = 1,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                        )
                    }
                }


                LeverageSlider(
                    !isWorking,
                    ticker,
                    scaleXManualBot.value,
                    viewModel::updateLeverage
                )
            }
        }

        StartStopBotBtn(
            isWorking,
            startIsEnabled,
            if (isManualBot) viewModel::startStopBot else viewModel::startAutomatedBot
        )
    }

    SearchableListBottomSheet(
        isVisible = showTickersDialog,
        onDismiss = { showTickersDialog = false },
        onItemClick = {
            viewModel.updateTicker(it)
            showTickersDialog = false
        },
        items = tickers,
        onSearchText = viewModel::searchTicker,
    )
}