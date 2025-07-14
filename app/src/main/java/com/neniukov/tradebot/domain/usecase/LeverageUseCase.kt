package com.neniukov.tradebot.domain.usecase

import com.neniukov.tradebot.data.repository.BybitRepository
import javax.inject.Inject

class LeverageUseCase @Inject constructor(
    private val repository: BybitRepository,
) {

    suspend fun setLeverage(symbol: String, leverage: String) {
        repository.setLeverage(symbol, leverage)
    }
}