package com.neniukov.tradebot.test.screens

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.neniukov.tradebot.data.model.response.PositionResponse
import com.neniukov.tradebot.data.service.TradingBotService
import com.neniukov.tradebot.domain.model.defaultData
import com.neniukov.tradebot.domain.usecase.GetAllTickersUseCase
import com.neniukov.tradebot.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import javax.inject.Inject

@HiltViewModel
class TradingViewModel @Inject constructor(
    private val application: Application,
    private val getAllTickersUseCase: GetAllTickersUseCase
) : BaseViewModel() {

    private val ticker = MutableStateFlow(defaultData())
    val tickerFlow = ticker.asStateFlow()

    private val positions = MutableStateFlow<List<PositionResponse>?>(listOf())
    val positionsFlow = positions.asStateFlow()

    private val errors = MutableStateFlow<List<String>>(listOf())
    val errorsFlow = errors.asStateFlow()

    private val currentStatus = MutableStateFlow("")
    val currentStatusFlow = currentStatus.asStateFlow()

    private val isWorking = MutableStateFlow(false)
    val isWorkingFlow = isWorking.asStateFlow()

    private val tickers = MutableStateFlow<List<String>>(listOf())
    val tickersFlow = tickers.asStateFlow()

    private val walletBalance = MutableStateFlow<String?>(null)
    val walletBalanceFlow = walletBalance.asStateFlow()

    private var botService: WeakReference<TradingBotService>? = null
    private var isServiceBound = false

    private var allTickers = listOf<String>()

    init {
        loadTickers()
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
                botService?.get()?.errorsFlow?.collectLatest {
//                    errors.value = it
                }
            }

            launch {
                botService?.get()?.currentPositionFlow?.collectLatest {
                    currentStatus.value = it.name
                }
            }

            launch {
                botService?.get()?.walletBalanceFlow?.collectLatest {
                    walletBalance.value = it
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
        this.ticker.value = this.ticker.value.copy(symbol = ticker)
    }

    fun updateQty(qty: String) {
        this.ticker.value = this.ticker.value.copy(qty = qty)
    }

    fun startBot() {
        if (isServiceBound) {
            botService?.get()?.setData(ticker.value.copy(symbol = ticker.value.symbol.uppercase()))
            botService?.get()?.startBot()
            isWorking.value = true
        }
    }

    fun updateDataBot() {
        if (isServiceBound) {
            botService?.get()?.setData(ticker.value.copy(symbol = ticker.value.symbol.uppercase()))
        }
    }

    fun startService() {
        Intent(application, TradingBotService::class.java).also {
            application.applicationContext.startService(it)
            application.applicationContext.bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    fun searchTicker(text: String) {
        tickers.value = allTickers.filter { it.contains(text, true) }
    }

    private fun loadTickers() {
        doLaunch(
            job = { getAllTickersUseCase() },
            onSuccess = {
                tickers.value = it
                allTickers = it
            }
        )
    }
}