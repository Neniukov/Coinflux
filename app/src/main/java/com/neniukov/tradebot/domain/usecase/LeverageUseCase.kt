package com.neniukov.tradebot.domain.usecase

import com.neniukov.tradebot.data.binance.BinanceRepository
import com.neniukov.tradebot.data.repository.BybitRepository
import javax.inject.Inject

class LeverageUseCase @Inject constructor(
    private val bybitRepository: BybitRepository,
    private val binanceRepository: BinanceRepository
) {

    suspend fun setLeverage(symbol: String, leverage: String) {
        binanceRepository.setLeverage(symbol, leverage)
    }
}