package com.neniukov.tradebot.ui.screen.main.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neniukov.tradebot.R
import com.neniukov.tradebot.ui.screen.main.BotViewModel
import com.neniukov.tradebot.ui.theme.Gray
import com.neniukov.tradebot.ui.theme.LightGreenGray
import com.neniukov.tradebot.ui.theme.Yellow

@Composable
fun AutoBotSettingsViews(modifier: Modifier, viewModel: BotViewModel) {
    Column(
        modifier = modifier
            .padding(top = 16.dp)
            .fillMaxWidth()
            .background(color = Color.White, shape = RoundedCornerShape(38.dp))
            .padding(bottom = 12.dp)
    ) {
        val isWorking by viewModel.isWorkingFlow.collectAsState()
        val ticker by viewModel.tickerFlow.collectAsState()
        val startIsEnabled by remember {
            derivedStateOf { ticker.qty.isNotBlank() }
        }
        // Ticker, amount, leverage section
        Box(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .background(
                    color = Yellow.copy(alpha = if (isWorking) 0.5f else 1f),
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(horizontal = 8.dp, vertical = 24.dp)
        ) {

            Column {

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.amount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.sp,
                    color = Gray.copy(alpha = if (isWorking) 0.5f else 1f)
                )

                val fieldColor = LightGreenGray.copy(alpha = if (isWorking) 0.5f else 1f)

                OutlinedTextField(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .fillMaxWidth()
                        .background(
                            color = LightGreenGray.copy(alpha = if (isWorking) 0.5f else 1f),
                            shape = RoundedCornerShape(20.dp)
                        ),
                    value = ticker.qty,
                    onValueChange = if (!isWorking) viewModel::updateQty else { _ -> },
                    enabled = !isWorking,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black.copy(alpha = if (isWorking) 0.5f else 1f)),
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

        StartStopBotBtn(isWorking, startIsEnabled, viewModel::startAutomatedBot)
    }
}