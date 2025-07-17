package com.neniukov.tradebot.domain.usecase

import com.neniukov.tradebot.data.binance.BinanceRepository
import com.neniukov.tradebot.data.repository.BybitRepository
import javax.inject.Inject

class ConnectToExchangeUseCase @Inject constructor(
    private val bybitRepository: BybitRepository,
    private val binanceRepository: BinanceRepository
) {

    suspend operator fun invoke(apiKey: String, secretKey: String): String {
        return binanceRepository.connectToBinance(apiKey, secretKey)
    }
}