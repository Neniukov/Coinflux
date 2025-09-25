package com.neniukov.tradebot.data.managers.impl

import com.neniukov.tradebot.data.binance.BinanceRepository
import com.neniukov.tradebot.data.binance.model.response.BinancePositionResponse
import com.neniukov.tradebot.data.binance.model.response.OpenOrderResponse
import com.neniukov.tradebot.data.managers.OrderManager
import com.neniukov.tradebot.domain.model.TradingState
import kotlinx.coroutines.delay
import java.util.Locale
import javax.inject.Inject

class EmaLongOrderManager @Inject constructor(private val repository: BinanceRepository) : OrderManager {

    override val interval: Int
        get() = 15

    override val requestDelay: Long = 30_000

    override val requestDelayForOpeningPosition: Long
        get() = 20_000

    override suspend fun setTP(
        state: TradingState,
        position: BinancePositionResponse,
        openOrders: List<OpenOrderResponse>
    ) {
        if (state.baseOrderQuantity == 0.0) return

        if (openOrders.isEmpty()) {
            setTwoTakeProfits(state, position)
            return
        }

        if (openOrders.size == 2 && openOrders.sumOf { it.origQty.toDouble()  } != position.positionAmt.toDouble()) {
            repository.cancelOpenOrders(position.symbol)
            setTwoTakeProfits(state, position)
            return
        }

        if (openOrders.size == 1 && openOrders.first().origQty != position.positionAmt) {
            repository.cancelOpenOrders(position.symbol)
            setTwoTakeProfits(state, position)
        }
    }

    override suspend fun addPosition(state: TradingState, position: BinancePositionResponse) {
        val markPrice = position.markPrice.toDoubleOrNull()
        val entryPrice = position.entryPrice.toDoubleOrNull()
        if (markPrice == null || entryPrice == null || state.baseOrderQuantity == 0.0) return

        val orderQuantity = state.baseOrderQuantity * 0.4

        val numberOfInputs = state.totalPositionQuantity / orderQuantity
        val percentageOfEntryPrice = if(numberOfInputs < 6) 0.01 else if (numberOfInputs > 12) 0.03 else 0.02
        val differenceForEntry = entryPrice * percentageOfEntryPrice
        if (entryPrice - markPrice < differenceForEntry) {
            return
        }
        if (orderQuantity <= 0) {
            return
        }
        repository.placeMarketOrder(position.symbol, "Buy", orderQuantity.toInt().toString())
    }

    private suspend fun setTwoTakeProfits(state: TradingState, position: BinancePositionResponse) {
        val orderQuantity = state.baseOrderQuantity * 0.4
        val numberOfInputs = state.totalPositionQuantity / orderQuantity

        val firstTakeProfit = if (numberOfInputs > 32) {
            state.currentAverageEntryPrice * (1 + FIRST_TP_PERCENT_FOR_BAD_POSITION)
        } else {
            state.currentAverageEntryPrice * (1 + FIRST_TP_PERCENT)
        }
        val secondTakeProfit = if (numberOfInputs > 32) {
            state.currentAverageEntryPrice * (1 + SECOND_TP_PERCENT_FOR_BAD_POSITION)
        } else {
            state.currentAverageEntryPrice * (1 + SECOND_TP_PERCENT)
        }

        val halfQuantity = (position.positionAmt.toDouble() / 2).toInt()

        repository.setTP(
            symbol = position.symbol,
            side = "SELL",
            quantity = halfQuantity.toString(),
            closePrice =  String.format(Locale.US, "%.5f", firstTakeProfit)
        )
        delay(2000)
        repository.setTP(
            symbol = position.symbol,
            side = "SELL",
            quantity = halfQuantity.toString(),
            closePrice = String.format(Locale.US, "%.5f", secondTakeProfit)
        )
    }

    companion object {
       private const val FIRST_TP_PERCENT = 0.012 // 1%
       private const val SECOND_TP_PERCENT = 0.012 // 1.5%
       private const val FIRST_TP_PERCENT_FOR_BAD_POSITION = 0.003 // 0.3%
       private const val SECOND_TP_PERCENT_FOR_BAD_POSITION = 0.005 // 0.5%
    }
}