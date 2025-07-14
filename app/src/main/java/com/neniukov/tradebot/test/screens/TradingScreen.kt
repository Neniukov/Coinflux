package com.neniukov.tradebot.test.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neniukov.tradebot.data.model.response.PositionResponse

@Composable
fun TradingScreen(
    viewModel: TradingViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.startService()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF040509))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp)
                .padding(16.dp)
        ) {
            BalanceSection(viewModel)
            TradingCard(viewModel)
            PositionsSection(viewModel)
        }
    }
}

@Composable
private fun BalanceSection(viewModel: TradingViewModel) {
    val balance by viewModel.walletBalanceFlow.collectAsStateWithLifecycle()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Your balance",
                color = Color(0xFF6C85D6),
                fontSize = 12.sp,
                fontWeight = FontWeight.Light
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = balance.orEmpty(),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Light
                )
                Text(
                    text = " USDT",
                    color = Color(0xFF6C85D6),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Light
                )
            }
        }
        IconButton(
            onClick = { /* TODO: Implement logout logic */ }
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Logout",
                tint = Color(0xFF6C85D6)
            )
        }
    }
}

@Composable
private fun TradingCard(viewModel: TradingViewModel) {
    val ticker by viewModel.tickerFlow.collectAsStateWithLifecycle()
    var leverage by remember { mutableStateOf(43f) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .drawBehind {
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFBB3660),
                            Color(0xFFE6CD2F)
                        )
                    ),
                    cornerRadius = CornerRadius(20.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                )
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0B13))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            TicketAndAmountSection(
                symbol = ticker.symbol,
                amount = ticker.qty,
                onSymbolChange = viewModel::updateTicker,
                onAmountChange = viewModel::updateQty
            )
            LeverageSection(leverage) { leverage = it }
            StartBotButton(
                isWorking = viewModel.isWorkingFlow.collectAsStateWithLifecycle().value,
                onStart = viewModel::startBot
            )
        }
    }
}

@Composable
private fun TicketAndAmountSection(
    symbol: String,
    amount: String,
    onSymbolChange: (String) -> Unit,
    onAmountChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Ticket",
                color = Color(0xFF6C85D6),
                fontSize = 12.sp,
                fontWeight = FontWeight.Light
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent,
                border = BorderStroke(1.dp, Color(0xFF353535))
            ) {
                BasicTextField(
                    value = symbol,
                    onValueChange = onSymbolChange,
                    textStyle = LocalTextStyle.current.copy(
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Light
                    ),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Amount",
                color = Color(0xFF6C85D6),
                fontSize = 12.sp,
                fontWeight = FontWeight.Light
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent,
                border = BorderStroke(1.dp, Color(0xFF353535))
            ) {
                BasicTextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    textStyle = LocalTextStyle.current.copy(
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Light
                    ),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LeverageSection(leverage: Float, onLeverageChange: (Float) -> Unit) {
    Column(
        modifier = Modifier.padding(top = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Leverage",
                color = Color(0xFF6C85D6),
                fontSize = 12.sp,
                fontWeight = FontWeight.Light
            )
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFBB3660),
                                Color(0xFFE6CD2F)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${leverage.toInt()}x",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Light
                )
            }
        }
        
        Slider(
            value = leverage,
            onValueChange = onLeverageChange,
            valueRange = 1f..100f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFBB3660),
                activeTrackColor = Color(0xFFE6CD2F),
                inactiveTrackColor = Color(0xFF353535)
            ),
            modifier = Modifier
                .padding(top = 24.dp)
                .fillMaxWidth()
                .height(48.dp),
        )
    }
}

@Composable
private fun StartBotButton(
    isWorking: Boolean,
    onStart: () -> Unit
) {
    Button(
        onClick = onStart,
        enabled = !isWorking,
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(30.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF3658BB),
                            Color(0xFF0A1C5A)
                        )
                    ),
                    shape = RoundedCornerShape(30.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isWorking) "Bot is running..." else "Start bot",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Light
                )
            }
        }
    }
}

@Composable
private fun PositionsSection(viewModel: TradingViewModel) {
    val positions by viewModel.positionsFlow.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "Positions",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Light
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(positions.orEmpty()) { position ->
                PositionCard(position)
            }
        }
    }
}

@Composable
private fun PositionCard(position: PositionResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = Color(0xFF3658BB),
                    style = Stroke(width = 1.dp.toPx()),
                    cornerRadius = CornerRadius(30.dp.toPx())
                )
            },
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0B13))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Entry Price",
                        color = Color(0xFF6C85D6),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light
                    )
                    Text(
                        text = position.avgPrice,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Light
                    )
                }
                Column {
                    Text(
                        text = "Size",
                        color = Color(0xFF6C85D6),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light
                    )
                    Text(
                        text = position.size,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Light
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "p&l",
                        color = Color(0xFF6C85D6),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light
                    )
                    Text(
                        text = position.unrealisedPnl,
                        color = if (position.unrealisedPnl.toDouble() > 0) Color(0xFF51E6BA) else Color.Red,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Text(
                    text = "USDT",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "SL",
                        color = Color(0xFF6C85D6),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light
                    )
                    Text(
                        text = position.stopLoss,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Light
                    )
                }
                Column {
                    Text(
                        text = "TP",
                        color = Color(0xFF6C85D6),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light
                    )
                    Text(
                        text = position.takeProfit,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Light
                    )
                }
            }
        }
    }
} 