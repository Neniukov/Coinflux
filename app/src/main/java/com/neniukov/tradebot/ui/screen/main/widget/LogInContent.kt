package com.neniukov.tradebot.ui.screen.main.widget

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neniukov.tradebot.R
import com.neniukov.tradebot.ui.screen.main.BotViewModel
import com.neniukov.tradebot.ui.theme.Dark40
import com.neniukov.tradebot.ui.theme.Gray
import com.neniukov.tradebot.ui.theme.LightGray
import com.neniukov.tradebot.ui.theme.LightGreenGray
import com.neniukov.tradebot.ui.theme.LightYellow
import com.neniukov.tradebot.ui.theme.Orange

@Composable
fun LogInContent(viewModel: BotViewModel) {

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Image(
            modifier = Modifier.padding(top = 80.dp),
            painter = painterResource(id = R.drawable.ic_security),
            contentDescription = ""
        )

        Row(
            modifier = Modifier
                .padding(top = 32.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = buildAnnotatedString {
                    append("Enter your ")
                    withStyle(style = SpanStyle(color = Orange, fontWeight = FontWeight.SemiBold)) {
                        append("Bybit")
                    }
                    append(" keys")
                },
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 22.sp),
                color = DarkGray
            )
            val context = LocalContext.current
            IconButton(
                modifier = Modifier.size(30.dp),
                onClick = { openLink(context) }) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(id = R.drawable.ic_question),
                    contentDescription = "",
                    tint = Color.Black
                )
            }
        }


        var apikey by remember { mutableStateOf("") }
        ApiKeyField(apikey, onValueChange = { apikey = it })

        var secretKey by remember { mutableStateOf("") }
        SecretKeyField(secretKey, onValueChange = { secretKey = it })

        val isEnabled by remember {
            derivedStateOf { apikey.isNotEmpty() && secretKey.isNotEmpty() }
        }

        ConnectBtn(enabled = isEnabled) {
            viewModel.connectToBybit(
                apikey = apikey,
                secretKey = secretKey,
            )
        }

        Spacer(modifier = Modifier.weight(1f))


        Text(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 60.dp),
            text = stringResource(id = R.string.disclaimer),
            style = MaterialTheme.typography.bodyMedium,
            color = White,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

@Composable
private fun ApiKeyField(apikey: String, onValueChange: (String) -> Unit) {
    Text(
        modifier = Modifier
            .padding(top = 32.dp, start = 16.dp)
            .fillMaxWidth(),
        text = stringResource(id = R.string.api_key),
        style = MaterialTheme.typography.bodyMedium,
        fontSize = 12.sp,
        color = Gray
    )

    OutlinedTextField(
        modifier = Modifier
            .padding(top = 4.dp)
            .fillMaxWidth()
            .background(
                color = LightGreenGray,
                shape = RoundedCornerShape(20.dp)
            ),
        value = apikey,
        onValueChange = onValueChange,
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = LightGreenGray,
            unfocusedContainerColor = LightGreenGray,
            disabledContainerColor = LightGreenGray,
            focusedBorderColor = LightGreenGray,
            unfocusedBorderColor = LightGreenGray,
            cursorColor = LightGray
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        shape = RoundedCornerShape(20.dp),
        maxLines = 1
    )
}

@Composable
private fun SecretKeyField(secretKey: String, onValueChange: (String) -> Unit) {
    Text(
        modifier = Modifier
            .padding(top = 24.dp, start = 16.dp)
            .fillMaxWidth(),
        text = stringResource(id = R.string.secret_key),
        style = MaterialTheme.typography.bodyMedium,
        fontSize = 12.sp,
        color = Gray
    )

    OutlinedTextField(
        modifier = Modifier
            .padding(top = 4.dp)
            .fillMaxWidth()
            .background(
                color = LightGreenGray,
                shape = RoundedCornerShape(20.dp)
            ),
        value = secretKey,
        onValueChange = onValueChange,
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = LightGreenGray,
            unfocusedContainerColor = LightGreenGray,
            disabledContainerColor = LightGreenGray,
            focusedBorderColor = LightGreenGray,
            unfocusedBorderColor = LightGreenGray,
            cursorColor = LightGray
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        shape = RoundedCornerShape(20.dp),
        singleLine = true
    )
}

@Composable
private fun ConnectBtn(enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Orange,
            disabledContainerColor = LightYellow.copy(alpha = 0.5f),
        ),
        enabled = enabled,
        modifier = Modifier
            .padding(top = 36.dp)
            .height(50.dp)
            .fillMaxWidth()
            .drawBehind {
                drawIntoCanvas { canvas ->
                    val paint = Paint()
                        .apply {
                            color = Dark40.toArgb()
                            setShadowLayer(16.dp.toPx(), 0f, 16f, color)
                        }
                    canvas.nativeCanvas.drawRoundRect(
                        0f,
                        0f,
                        size.width,
                        size.height,
                        20.dp.toPx(),
                        20.dp.toPx(),
                        paint
                    )
                }
            }
    ) {

        Text(
            text = stringResource(id = R.string.connect),
            color = LightYellow.copy(alpha = if (enabled) 1f else 0.5f),
            fontSize = 16.sp,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun openLink(context: Context) {
    val url = "https://www.bybit.com/app/user/api-management"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}