package com.neniukov.tradebot.ui.screen.main

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.neniukov.tradebot.data.local.SharedPrefs
import com.neniukov.tradebot.data.model.response.PositionResponse
import com.neniukov.tradebot.data.service.TradingBotService
import com.neniukov.tradebot.domain.model.CurrentPosition
import com.neniukov.tradebot.domain.model.defaultData
import com.neniukov.tradebot.domain.usecase.ConnectToExchangeUseCase
import com.neniukov.tradebot.domain.usecase.GetAllTickersUseCase
import com.neniukov.tradebot.domain.usecase.GetTickerInfoUseCase
import com.neniukov.tradebot.domain.usecase.LeverageUseCase
import com.neniukov.tradebot.domain.usecase.PlaceOrderUseCase
import com.neniukov.tradebot.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import javax.inject.Inject

@HiltViewModel
class BotViewModel @Inject constructor(
    private val application: Application,
    private val getAllTickersUseCase: GetAllTickersUseCase,
    private val prefs: SharedPrefs,
    private val connectBybitUseCase: ConnectToExchangeUseCase,
    private val getTickerInfoUseCase: GetTickerInfoUseCase,
    private val leverageUseCase: LeverageUseCase,
    private val placeOrderUseCase: PlaceOrderUseCase
) : BaseViewModel() {

    private val ticker = MutableStateFlow(defaultData())
    val tickerFlow = ticker.asStateFlow()

    private val positions = MutableStateFlow<List<CurrentPosition>?>(null)
    val positionsFlow = positions.asStateFlow()

    private val isWorking = MutableStateFlow(false)
    val isWorkingFlow = isWorking.asStateFlow()

    private val tickers = MutableStateFlow<List<String>>(listOf())
    val tickersFlow = tickers.asStateFlow()

    private val walletBalance = MutableStateFlow<String?>(null)
    val walletBalanceFlow = walletBalance.asStateFlow()

    private var botService: WeakReference<TradingBotService>? = null
    private var isServiceBound = false

    private var isLoggedIn = MutableStateFlow<Boolean?>(null)
    var isLoggedInFlow = isLoggedIn.asStateFlow()

    private var showLogIn = MutableStateFlow(false)
    var showLogInFlow = showLogIn.asStateFlow()

    private var socketConnection = MutableStateFlow<Boolean?>(null)
    val socketConnectionFlow = socketConnection.asStateFlow()

    private var allTickers = listOf<String>()

    init {
        checkLoggedIn()
        loadTickers()
        loadLastSettings()
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TradingBotService.LocalBinder
            botService = WeakReference(binder.getService())
            isServiceBound = true

            launch {
                botService?.get()?.positionsFlow?.collectLatest {
                    positions.value = it
                }
            }

            launch {
                botService?.get()?.errorsFlow?.collectLatest { error ->
                    error?.let(::handleError)
                }
            }

            launch {
                botService?.get()?.walletBalanceFlow?.collectLatest {
                    walletBalance.value = it?.toDoubleOrNull()
                        ?.let { balance -> String.format("%.2f", balance) }
                        ?: "-.-"
                }
            }

            launch {
                botService?.get()?.socketConnectionFlow?.collectLatest { connection ->
                    socketConnection.value = connection
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceBound = false
            botService = null
        }
    }

    override fun onCleared() {
        if (isServiceBound) {
            botService?.get()?.stop()
            application.applicationContext.unbindService(serviceConnection)
            isServiceBound = false
        }
        super.onCleared()
    }

    fun updateTicker(ticker: String) {
        getTickerInfo(ticker.uppercase())
    }

    fun updateQty(qty: String) {
        if (qty.matches(Regex("^\\d*(\\.\\d*)?$"))) {
            this.ticker.value = this.ticker.value.copy(qty = qty)
        }
    }

    fun startStopBot() {
        if (isServiceBound) {
            if (isWorking.value) {
                botService?.get()?.stopBot()
            } else {
                setLeverage()
                botService?.get()?.setData(ticker.value)
                doLaunch(
                    job = {
                        prefs.saveLastTicker(
                            ticker.value.symbol,
                            ticker.value.qty,
                            ticker.value.leverage
                        )
                    },
                    showProgress = false
                )
                botService?.get()?.startBot()
            }
            isWorking.value = !isWorking.value
        }
    }

    fun startAutomatedBot() {
        if (isServiceBound) {
            if (isWorking.value) {
                botService?.get()?.stopAutomatedBot()
            } else {
                botService?.get()?.startAutomatedBot(ticker.value.qty)
            }
            isWorking.value = !isWorking.value
        }
    }

    fun startService() {
        Intent(application, TradingBotService::class.java).also {
            application.applicationContext.startService(it)
            application.applicationContext.bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    fun searchTicker(text: String) {
        tickers.value = allTickers.filter { it.startsWith(text, true) }
    }

    fun connectToBybit(apikey: String, secretKey: String) {
        doLaunch(
            job = { connectBybitUseCase(apikey, secretKey) },
            onSuccess = { balance ->
                val userBalance = balance.toDoubleOrNull()
                Log.e("macligs", "User balance: $userBalance")
//                if (userBalance != null) {
                prefs.saveApiKey(apikey)
                prefs.saveSecretKey(secretKey)
                isLoggedIn.emit(true)
                showLogIn.emit(false)
//                }
            }
        )
    }

    fun logout() {
        doLaunch(
            job = {
                prefs.clear()
                isLoggedIn.emit(false)
            },
            onSuccess = {
                if (isServiceBound) {
                    isWorking.value = false
                    botService?.get()?.stopBot()
                }
            }
        )
    }

    fun updateLeverage(leverage: Float) {
        Log.e("macldaa", "Update leverage: $leverage")
        ticker.value = ticker.value.copy(leverage = leverage.toInt())
    }

    fun openLogin() {
        showLogIn.value = true
    }

    fun closePosition(position: CurrentPosition) {
        doLaunch(
            job = {
                placeOrderUseCase(position.symbol, position.side, position.size)
            },
            onSuccess = {
                positions.value = positions.value?.filter { it.symbol != position.symbol }
            },
            showProgress = false
        )
    }

    private fun getTickerInfo(symbol: String, showProgress: Boolean = true) {
        doLaunch(
            job = {
                ticker.value = ticker.value.copy(symbol = symbol)
                getTickerInfoUseCase(ticker.value.symbol)
            },
            onSuccess = {
                ticker.emit(
                    ticker.value.copy(
                        symbol = symbol,
                        minLeverage = it.minLeverage,
                        maxLeverage = it.maxLeverage,
                        leverage = it.leverage
                    )
                )
            },
            showProgress = showProgress
        )
    }

    private fun setLeverage() {
        doLaunch(
            job = {
                leverageUseCase.setLeverage(ticker.value.symbol, ticker.value.leverage.toString())
            },
            onSuccess = {},
            showProgress = false
        )
    }

    private fun checkLoggedIn() {
        launch(Dispatchers.IO) {
            val result = prefs.getApiKey() != null && prefs.getSecretKey() != null
            isLoggedIn.emit(result)
        }
    }

    private fun loadTickers() {
        doLaunch(
            job = { getAllTickersUseCase() },
            onSuccess = {
                tickers.value = it
                allTickers = it
            },
            showProgress = false
        )
    }

    private fun loadLastSettings() {
        launch {
            prefs.getLastTicker()?.let { (symbol, qty, leverage) ->
                ticker.value = ticker.value.copy(symbol = symbol, qty = qty, leverage = leverage)
            }
        }
    }
}