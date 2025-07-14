package com.neniukov.tradebot.domain.usecase

import com.neniukov.tradebot.data.repository.BybitRepository
import javax.inject.Inject

class ConnectBybitUseCase @Inject constructor(
    private val repository: BybitRepository
) {

    suspend operator fun invoke(apiKey: String, secretKey: String): String {
        return repository.connectToBybit(apiKey, secretKey)
    }
}