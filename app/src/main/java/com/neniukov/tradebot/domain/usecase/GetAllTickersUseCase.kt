package com.neniukov.tradebot.domain.usecase

import com.neniukov.tradebot.data.repository.BybitRepository
import javax.inject.Inject

class GetAllTickersUseCase @Inject constructor(
    private val bybitRepository: BybitRepository,
) {

    suspend operator fun invoke(): List<String> {
        return bybitRepository.getTickers()
    }
}