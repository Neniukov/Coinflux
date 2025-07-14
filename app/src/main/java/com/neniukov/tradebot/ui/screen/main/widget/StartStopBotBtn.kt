package com.neniukov.tradebot.ui.screen.main.widget

import android.graphics.Paint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neniukov.tradebot.R
import com.neniukov.tradebot.ui.theme.Dark
import com.neniukov.tradebot.ui.theme.Dark40
import com.neniukov.tradebot.ui.theme.Green
import com.neniukov.tradebot.ui.theme.LightGreenGray
import com.neniukov.tradebot.ui.theme.Red


@Composable
fun StartStopBotBtn(
    isWorking: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    var currentState by remember { mutableStateOf(isWorking) }

    val transition = updateTransition(targetState = isWorking, label = "BotStateTransition")

    val animatedProgress by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 2000) },
        label = "ColorFillProgress"
    ) { target -> if (target != currentState) 0f else 1f }

    // Обновление содержимого на середине анимации заливки
    LaunchedEffect(animatedProgress, isWorking) {
        if (animatedProgress >= 0.5f) currentState = isWorking
    }

    val cornerRadius = with(LocalDensity.current) { 20.dp.toPx() }
    val blurRadius = with(LocalDensity.current) { 4.dp.toPx() }

    val fromColor = if (currentState) Red else Green
    val toColor = if (isWorking) Red else Green

    Button(
        enabled = enabled,
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White,
        ),
        modifier = Modifier
            .alpha(if (enabled) 1f else 0.5f)
            .padding(8.dp)
            .height(50.dp)
            .fillMaxWidth()
            .drawBehind {
//                if (enabled) {
//                    // Тень
//                    drawIntoCanvas { canvas ->
//                        val paint = Paint()
//                            .apply {
//                                color = Green.toArgb()
//                                setShadowLayer(blurRadius, 0f, 16f, color)
//                            }
//                        canvas.nativeCanvas.drawRoundRect(
//                            0f,
//                            0f,
//                            size.width,
//                            size.height,
//                            cornerRadius,
//                            cornerRadius,
//                            paint
//                        )
//                    }
//                }
                    // Фоновый цвет
                    drawRoundRect(
                        color = if (enabled) fromColor else LightGreenGray,
                        topLeft = Offset.Zero,
                        size = size,
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                    )

                    // Градиент заливки сверху вниз
                    val gradientHeight = size.height * animatedProgress
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(toColor, fromColor),
                            startY = 0f,
                            endY = gradientHeight
                        ),
                        topLeft = Offset(0f, 0f),
                        size = Size(size.width, gradientHeight),
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                    )

            }
    ) {
        AnimatedContent(
            targetState = currentState,
            transitionSpec = {
                scaleIn(tween(400), initialScale = 0.0f) togetherWith scaleOut(tween(400), targetScale = 0.0f)
            },
            label = "IconAnimation"
        ) { state ->
            Image(
                painter = painterResource(
                    id = if (state) R.drawable.ic_stop else R.drawable.ic_start
                ),
                contentDescription = null
            )
        }

        Text(
            text = stringResource(
                id = if (currentState) R.string.stop_bot else R.string.start_bot
            ),
            modifier = Modifier.padding(start = 10.dp),
            color = Color.White,
            fontSize = 16.sp,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}