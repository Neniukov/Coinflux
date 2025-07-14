package com.neniukov.tradebot.domain.usecase

import com.neniukov.tradebot.data.repository.BybitRepository
import com.neniukov.tradebot.domain.model.Ticker
import javax.inject.Inject

class GetTickerInfoUseCase @Inject constructor(
    private val repository: BybitRepository
) {

    suspend operator fun invoke(symbol: String): Ticker {
        return repository.getTickerInfo(symbol)
    }
}