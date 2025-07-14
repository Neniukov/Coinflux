package com.neniukov.tradebot.ui.screen.main.widget

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.neniukov.tradebot.R
import com.neniukov.tradebot.data.model.response.PositionResponse
import com.neniukov.tradebot.domain.model.Side
import com.neniukov.tradebot.ui.theme.EndGradient
import com.neniukov.tradebot.ui.theme.Gray
import com.neniukov.tradebot.ui.theme.Green
import com.neniukov.tradebot.ui.theme.LightGray
import com.neniukov.tradebot.ui.theme.Red

fun LazyListScope.positionsView(
    positions: State<List<PositionResponse>?>,
    onClosePosition: (PositionResponse) -> Unit,
    onStatisticsClick: () -> Unit
) {
    item(key = "positions_title") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .animateItem()
                .padding(top = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = R.string.positions),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 16.sp,
                color = Color.White
            )

            Icon(
                modifier = Modifier.size(20.dp).clickable { onStatisticsClick() },
                imageVector = ImageVector.vectorResource(R.drawable.ic_statistics),
                contentDescription = "Statistics",
                tint = Color.White
            )
        }
    }

    if (positions.value == null) {
        item {
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.searching_anim))
            val progress by animateLottieCompositionAsState(
                composition,
                iterations = LottieConstants.IterateForever
            )

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .size(200.dp, 40.dp)
                )
            }
        }
    } else {
        items(
            items = positions.value.orEmpty(),
            key = { it.symbol },
        ) {
            val animationSpec: FiniteAnimationSpec<IntSize> = remember { tween(700) }
            val fadeAnimationSpec: FiniteAnimationSpec<Float> = remember { tween(700) }

            AnimatedVisibility(
                modifier = Modifier
                    .animateItem(fadeInSpec = fadeAnimationSpec)
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                enter = expandVertically(animationSpec) + fadeIn(fadeAnimationSpec),
                exit = shrinkOut(animationSpec) + fadeOut(fadeAnimationSpec),
                visible = !positions.value.isNullOrEmpty()
            ) {
                PositionItemView(Modifier.animateItem(), it, onClosePosition)
            }
        }
    }
}

@Composable
private fun PositionItemView(
    modifier: Modifier,
    position: PositionResponse,
    onClosePosition: (PositionResponse) -> Unit
) {
    Column(
        modifier = modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .background(color = Color.White, shape = RoundedCornerShape(30.dp))
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = position.symbol.uppercase(),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.ExtraBold
            )

            PnlView(position.unrealisedPnl)
        }

        Row(
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val isLong = position.side == Side.Buy.name
            Image(
                painter = painterResource(id = if (isLong) R.drawable.ic_long else R.drawable.ic_short),
                contentDescription = ""
            )
            Text(
                text = stringResource(id = if (isLong) R.string.long_text else R.string.short_text),
                modifier = Modifier.padding(start = 4.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.sp,
                color = LightGray
            )

            Text(
                text = "(${position.leverage}x)",
                modifier = Modifier.padding(start = 8.dp),
                fontSize = 12.sp,
                color = LightGray
            )

            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(id = R.string.pnl),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.sp,
                color = LightGray
            )
        }

        Row(
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            val itemModifier = Modifier.weight(1f)

            ItemView(
                modifier = itemModifier,
                title = stringResource(id = R.string.size),
                value = position.size,
                horizontalAlignment = Alignment.Start
            )

            ItemView(
                modifier = itemModifier,
                title = stringResource(id = R.string.entry_price),
                value = "%.4f".format(position.avgPrice.toDouble()),
                horizontalAlignment = Alignment.CenterHorizontally
            )

            ItemView(
                modifier = Modifier.weight(1.4f),
                title = stringResource(id = R.string.mark_price),
                value = "%.4f".format(position.markPrice.toDouble()),
                horizontalAlignment = Alignment.CenterHorizontally,
                percentageChange = calculatePriceChangePercent(position.avgPrice, position.markPrice)
            )

            ItemView(
                modifier = Modifier.weight(0.5f),
                title = stringResource(id = R.string.im),
                value = position.positionIM.toDoubleOrNull()?.toInt()?.toString() + "$",
                horizontalAlignment = Alignment.End
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
            color = Gray
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                modifier = Modifier.padding(end = 4.dp),
                painter = painterResource(id = R.drawable.ic_long),
                contentDescription = ""
            )

            Text(
                text = stringResource(id = R.string.tp),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.sp,
                color = LightGray,
            )

            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = "${position.takeProfit} (${
                    calculateTakeProfitUsd(
                        position.avgPrice,
                        position.takeProfit,
                        position.size
                    )
                })",
                style = MaterialTheme.typography.bodyMedium,
                color = Green,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.width(16.dp))


            if (position.stopLoss.isNotBlank()) {
                Image(
                    modifier = Modifier.padding(end = 4.dp),
                    painter = painterResource(id = R.drawable.ic_short),
                    contentDescription = ""
                )

                Text(
                    text = stringResource(id = R.string.sl),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightGray,
                )

                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = position.stopLoss,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.sp,
                    color = Red,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onClosePosition(position) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EndGradient,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.close_position),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PnlView(unrealisedPnl: String) {
    val number = unrealisedPnl.toDouble()
    val pnl = String.format("%.2f", number)
    val previousBalance = remember { mutableStateOf(pnl) }
    val maxLength = maxOf(previousBalance.value.length, pnl.length)

    for (i in 0 until maxLength) {
        val oldChar = previousBalance.value.getOrNull(i) ?: ' '
        val newChar = pnl.getOrNull(i) ?: ' '

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
                    color = if (number >= 0) Green else Red,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }

    LaunchedEffect(pnl) {
        previousBalance.value = pnl
    }

    Text(
        text = " $",
        style = MaterialTheme.typography.bodyMedium,
        fontSize = 16.sp,
        color = if (number >= 0) Green else Red,
        fontWeight = FontWeight.ExtraBold
    )
}

@Composable
fun ItemView(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    horizontalAlignment: Alignment.Horizontal,
    percentageChange: Double? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = horizontalAlignment,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 12.sp,
            color = LightGray,
        )

        if (percentageChange != null) {
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    modifier = Modifier.padding(start = 4.dp),
                    text = "(%.2f".format(percentageChange) + "%)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.sp),
                    color = if (percentageChange >= 0) Green else Red,
                )
            }
        } else {
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

fun calculateTakeProfitUsd(
    avgPrice: String,
    tpPrice: String,
    size: String,
): String {
    if(tpPrice.isBlank()) return ""
    return "%.2f".format((tpPrice.toDouble() - avgPrice.toDouble()) * size.toDouble()) + "$"
}

fun calculatePriceChangePercent(
    avgPrice: String,
    currentPrice: String
): Double {
    return (currentPrice.toDouble() - avgPrice.toDouble()) / avgPrice.toDouble() * 100
}