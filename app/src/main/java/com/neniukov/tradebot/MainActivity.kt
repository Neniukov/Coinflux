package com.neniukov.tradebot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.neniukov.tradebot.ui.screen.main.BotScreen
import com.neniukov.tradebot.ui.theme.TradeBotTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.neniukov.tradebot.ui.screen.statistics.StatisticsScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            TradeBotTheme {
                val systemUiController = rememberSystemUiController()
                val navController = rememberNavController()

                SideEffect {
                    systemUiController.setStatusBarColor(
                        color = Color.Transparent,
                        darkIcons = false
                    )
                }
                NavHost(navController = navController, startDestination = "bot") {
                    composable("bot") {
                        BotScreen(
                            onStatisticsClick = { navController.navigate("statistics") }
                        )
                    }
                    composable("statistics") {
                        StatisticsScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}