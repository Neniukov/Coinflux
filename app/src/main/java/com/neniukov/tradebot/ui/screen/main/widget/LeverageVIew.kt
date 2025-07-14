package com.neniukov.tradebot.ui.screen.main.widget

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neniukov.tradebot.R
import com.neniukov.tradebot.domain.model.Ticker
import com.neniukov.tradebot.ui.theme.TextFieldColor

private const val DEFAULT_LEVERAGE = 10f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeverageSlider(
    enabled: Boolean,
    ticker: Ticker,
    scale: Float,
    onValueChanged: (Float) -> Unit
) {
    val minLeverage = ticker.minLeverage.toFloat()
    val maxLeverage = ticker.maxLeverage.toFloat()

    var sliderPosition by remember {
        mutableStateOf(SliderState(value = DEFAULT_LEVERAGE, valueRange = minLeverage..maxLeverage))
    }

    LaunchedEffect(ticker.symbol, ticker.maxLeverage) {
        sliderPosition = SliderState(value = ticker.leverage.toFloat(), valueRange = minLeverage..maxLeverage)
    }

    LaunchedEffect(scale) {
        if (scale > 0.5f) {
            sliderPosition = SliderState(value = DEFAULT_LEVERAGE, valueRange = minLeverage..maxLeverage)
        }
    }

    Row(
        modifier = Modifier
            .padding(top = 24.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = R.string.leverage),
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 12.sp,
            color = White.copy(alpha = if (enabled) 1f else 0.5f)
        )
        Text(
            modifier = Modifier.padding(start = 12.dp),
            text = "${sliderPosition.value.toInt()}x",
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 16.sp,
            color = White.copy(alpha = if (enabled) 1f else 0.5f)
        )
    }

    val time = 500

    AnimatedVisibility(
        visible = scale > 0.5f,
        enter = fadeIn(tween(350)) + expandVertically(tween(time)),
        exit = fadeOut(tween(350)) + shrinkVertically(tween(time)),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${ticker.minLeverage}x",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.sp,
                    color = White.copy(alpha = if (enabled) 1f else 0.5f)
                )
                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = "${String.format("%.0f", maxLeverage)}x",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.sp,
                    color = White.copy(alpha = if (enabled) 1f else 0.5f)
                )
            }

            GradientTrackSlider(
                slider = sliderPosition,
                minLeverage = minLeverage,
                maxLeverage = maxLeverage,
                enabled = enabled,
                onValueChange = {
                    if (enabled) {
                        sliderPosition = it
                        onValueChanged(it.value)
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradientTrackSlider(
    slider: SliderState,
    minLeverage: Float,
    maxLeverage: Float,
    enabled: Boolean = true,
    onValueChange: (SliderState) -> Unit
) {
    val fraction = ((slider.value - minLeverage) / (maxLeverage - minLeverage)).coerceIn(0f, 1f)
    val targetColor = lerp(
        start = TextFieldColor,
        stop = Color.Red,
        fraction = fraction
    )

    val animatedColor by animateColorAsState(targetColor, label = "CircleColor")

    Slider(
        modifier = Modifier
            .padding(top = 8.dp)
            .alpha(if (enabled) 1f else 0.5f),
        state = slider,
        enabled = enabled,
        colors = SliderDefaults.colors(
            activeTrackColor = Color.Transparent,
            inactiveTrackColor = TextFieldColor.copy(alpha = if (enabled) 1f else 0.5f),
            thumbColor = Color.Transparent,
            disabledActiveTrackColor = TextFieldColor.copy(alpha = 0.5f),
            disabledInactiveTrackColor = TextFieldColor.copy(alpha = 0.5f),
            disabledThumbColor = Color.Transparent
        ),
        thumb = {
            Canvas(
                modifier = Modifier
                    .size(20.dp)
                    .border(1.dp, Color.Gray, CircleShape),
                onDraw = {
                    drawCircle(color = animatedColor)
                })
        },
        track = { sliderPositions ->
            Log.e("kdddd", "sliderPositions: ${sliderPositions.value}")
            onValueChange(sliderPositions)
            val gradientBrush = Brush.linearGradient(
                colors = listOf(
                    TextFieldColor.copy(alpha = if (enabled) 1f else 0.5f),
                    animatedColor
                ),
                start = Offset.Zero,
                end = Offset.Infinite
            )

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(TextFieldColor.copy(alpha = if (enabled) 1f else 0.5f))
            ) {
                Box(
                    Modifier
                        .height(8.dp)
                        .fillMaxWidth(sliderPositions.value)
                        .background(brush = gradientBrush)
                )
            }
        }
    )
}