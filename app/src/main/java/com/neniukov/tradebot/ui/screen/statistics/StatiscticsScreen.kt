package com.neniukov.tradebot.ui.screen.statistics

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neniukov.tradebot.R
import com.neniukov.tradebot.data.model.response.StatisticsResponse
import com.neniukov.tradebot.domain.model.Side
import com.neniukov.tradebot.ui.screen.main.widget.ItemView
import com.neniukov.tradebot.ui.screen.main.widget.PnlView
import com.neniukov.tradebot.ui.screen.main.widget.calculatePriceChangePercent
import com.neniukov.tradebot.ui.theme.EndGradient
import com.neniukov.tradebot.ui.theme.Green
import com.neniukov.tradebot.ui.theme.LightGray
import com.neniukov.tradebot.ui.theme.Red
import com.neniukov.tradebot.ui.theme.StartGradient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatisticsScreen(onBack: () -> Unit) {
    val viewModel = hiltViewModel<StatisticsViewModel>()
    StatisticsContent(viewModel, onBack)
    LaunchedEffect(Unit) {
        viewModel.loadStatistics()
    }
}

@Composable
fun StatisticsContent(viewModel: StatisticsViewModel, onBack: () -> Unit) {
    val statistics by viewModel.statisticsFlow.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .background(brush = Brush.linearGradient(colors = listOf(StartGradient, EndGradient)))
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Row {
            Icon(
                modifier = Modifier.clickable { onBack() },
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "back",
                tint = Color.White,
            )
            Text(
                text = "Trade Statistics",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {

            items(statistics) { stat ->
                val animationSpec: FiniteAnimationSpec<IntSize> = remember { tween(700) }
                val fadeAnimationSpec: FiniteAnimationSpec<Float> = remember { tween(700) }

                androidx.compose.animation.AnimatedVisibility(
                    modifier = Modifier
                        .animateItem(fadeInSpec = fadeAnimationSpec)
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    enter = expandVertically(animationSpec) + fadeIn(fadeAnimationSpec),
                    exit = shrinkOut(animationSpec) + fadeOut(fadeAnimationSpec),
                    visible = statistics.isNotEmpty()
                ) {
                    StatisticsCard(Modifier.animateItem(), stat)
                }
            }
        }
    }
}

@Composable
private fun StatisticsCard(modifier: Modifier, item: StatisticsResponse) {
    Column(
        modifier = modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .background(color = Color.White, shape = RoundedCornerShape(30.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = item.symbol.uppercase(),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                color = Color.Black,
                fontWeight = FontWeight.ExtraBold
            )

            Row(
                modifier = Modifier
                    .padding(start = 8.dp, bottom = 4.dp)
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val isLong = item.side != Side.Buy.name
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
                    text = "(${item.leverage}x)",
                    modifier = Modifier.padding(start = 8.dp),
                    fontSize = 12.sp,
                    color = LightGray
                )
            }

            PnlView(item.closedPnl)
        }

        Column(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
        ) {

            val itemModifier = Modifier.padding(top = 4.dp)
            val titleModifier = Modifier.weight(1f)

            ItemValueView(
                titleModifier = titleModifier,
                title = stringResource(id = R.string.size),
                value = item.qty,
            )

            ItemValueView(
                modifier = itemModifier,
                titleModifier = titleModifier,
                title = stringResource(id = R.string.entry_price),
                value = "%.4f".format(item.avgEntryPrice.toDouble()),
            )

            ItemValueView(
                modifier = itemModifier,
                titleModifier = titleModifier,
                title = stringResource(id = R.string.close_price),
                value = "%.4f".format(item.avgExitPrice.toDouble()),
                percentageChange = calculatePriceChangePercent(item.avgEntryPrice, item.avgExitPrice)
            )
        }

        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
        ) {
            ItemValueView(
                title = stringResource(id = R.string.open_fee),
                value = "%.2f".format(item.openFee.toDouble()) + "$",
                valueSize = 12
            )

            Spacer(modifier = Modifier.width(16.dp))

            ItemValueView(
                title = stringResource(id = R.string.close_fee),
                value = "%.2f".format(item.closeFee.toDouble()) + "$",
                valueSize = 12
            )

            Spacer(modifier = Modifier.weight(1f))

            val date = remember {
                formatTimestamp(item.createdTime.toLongOrNull())
            }

            DateView(
                modifier = Modifier.align(Alignment.Bottom),
                value = date,
                horizontalAlignment = Alignment.End
            )
        }
    }
}

@Composable
fun DateView(
    modifier: Modifier = Modifier,
    value: String,
    horizontalAlignment: Alignment.Horizontal,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = horizontalAlignment,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 12.sp,
            color = LightGray,
        )
    }
}


fun formatTimestamp(timestamp: Long?): String {
    if (timestamp == null) return ""
    val date = Date(timestamp)
    val format = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())
    return format.format(date)
}

@Composable
fun ItemValueView(
    modifier: Modifier = Modifier,
    titleModifier: Modifier = Modifier,
    title: String,
    value: String,
    percentageChange: Double? = null,
    valueSize: Int = 12,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = titleModifier,
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 12.sp,
            color = LightGray,
        )

        Text(
            modifier = Modifier.padding(start = 4.dp),
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black,
            fontWeight = FontWeight.SemiBold,
            fontSize = valueSize.sp,
        )

        if (percentageChange != null) {
            Text(
                modifier = Modifier.padding(start = 4.dp),
                text = "(%.2f".format(percentageChange) + "%)",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.sp),
                color = if (percentageChange >= 0) Green else Red,
            )
        }
    }
}