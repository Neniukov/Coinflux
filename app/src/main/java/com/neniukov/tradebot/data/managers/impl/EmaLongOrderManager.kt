package com.neniukov.tradebot.data.managers.impl

import com.neniukov.tradebot.data.binance.BinanceRepository
import com.neniukov.tradebot.data.binance.model.response.BinancePositionResponse
import com.neniukov.tradebot.data.binance.model.response.OpenOrderResponse
import com.neniukov.tradebot.data.managers.OrderManager
import com.neniukov.tradebot.domain.model.TradingState
import kotlinx.coroutines.delay
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
        val numberOfInputs = position.positionAmt.toDouble() / (state.baseOrderQuantity / 2)
        if (numberOfInputs > 30) {
            if (openOrders.size == 1 && openOrders.first().origQty == position.positionAmt) return
            if (openOrders.isNotEmpty()) {
                repository.cancelOpenOrders(position.symbol)
            }

            val takeProfitPrice = state.currentAverageEntryPrice * (1 + TAKE_PROFIT_PERCENT_FOR_BAD_POSITION)
            repository.setTP(
                symbol = position.symbol,
                side = "SELL",
                quantity = position.positionAmt,
                closePrice = takeProfitPrice.toString()
            )
            return
        }

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

        // Здесь мы добавляем позицию с половиной объема от базового ордера
        val orderQuantity = state.baseOrderQuantity / 2

        val numberOfInputs = state.totalPositionQuantity / orderQuantity
        val percentageOfEntryPrice = if (numberOfInputs > 10) 0.03 else 0.02
        val differenceForEntry = entryPrice * percentageOfEntryPrice
        if (entryPrice - markPrice < differenceForEntry) {
            return
        }
        if (orderQuantity <= 0) {
            return
        }
        repository.placeMarketOrder(position.symbol, "Buy", orderQuantity.toString())
    }

    private suspend fun setTwoTakeProfits(state: TradingState, position: BinancePositionResponse) {
        val firstTakeProfit =
            state.currentAverageEntryPrice * (1 + FIRST_TAKE_PROFIT_PERCENT)
        val secondTakeProfit =
            state.currentAverageEntryPrice * (1 + SECOND_TAKE_PROFIT_PERCENT)
        val halfQuantity = position.positionAmt.toDouble() / 2
        repository.setTP(
            symbol = position.symbol,
            side = "SELL",
            quantity = halfQuantity.toString(),
            closePrice = firstTakeProfit.toString()
        )
        delay(2000)
        repository.setTP(
            symbol = position.symbol,
            side = "SELL",
            quantity = halfQuantity.toString(),
            closePrice = secondTakeProfit.toString()
        )
    }

    companion object {
       private const val FIRST_TAKE_PROFIT_PERCENT = 0.01 // 1%
       private const val SECOND_TAKE_PROFIT_PERCENT = 0.015 // 1.5%
       private const val TAKE_PROFIT_PERCENT_FOR_BAD_POSITION = 0.003 // 0.3%
    }
}