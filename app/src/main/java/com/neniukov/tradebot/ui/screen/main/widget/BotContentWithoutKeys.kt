package com.neniukov.tradebot.ui.screen.main.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neniukov.tradebot.R
import com.neniukov.tradebot.ui.screen.main.BotViewModel
import com.neniukov.tradebot.ui.theme.Dark
import com.neniukov.tradebot.ui.theme.Orange

@Composable
fun BotContentWithoutKeys(viewModel: BotViewModel) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Button(
            modifier = Modifier.height(50.dp),
            onClick = viewModel::openLogin,
            colors = ButtonDefaults.buttonColors(
                containerColor = Orange,
                contentColor = Color.Black,
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp)
        ) {
            Text(
                text = stringResource(id = R.string.connect_to_bybit),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 16.sp,
                color = Dark,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    BotSettingsView(true, Modifier, viewModel)
}