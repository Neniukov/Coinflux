package com.neniukov.tradebot.data.socket

import com.neniukov.tradebot.data.model.response.KLineSocketWrapper
import com.neniukov.tradebot.data.model.response.TickerWrapper
import kotlinx.coroutines.flow.Flow

interface WebSocketService {
    val events: Flow<KLineSocketWrapper>
    val connectionState: Flow<Boolean?>
    fun connect()
    fun disconnect()
    fun send(message: String)
}