package com.neniukov.tradebot.domain.usecase

import com.neniukov.tradebot.data.repository.BybitRepository
import com.neniukov.tradebot.domain.model.Side
import javax.inject.Inject

class PlaceOrderUseCase @Inject constructor(
    private val repository: BybitRepository,
) {

    suspend operator fun invoke(symbol: String, side: String, qty: String) {
        repository.placeMarketOrder(
            symbol = symbol,
            side = if (side == Side.Buy.name) Side.Sell.name else Side.Buy.name,
            qty = qty,
        )
    }
}