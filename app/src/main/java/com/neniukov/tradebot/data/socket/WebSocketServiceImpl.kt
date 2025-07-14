package com.neniukov.tradebot.data.socket

import android.util.Log
import com.neniukov.tradebot.data.model.response.KLineSocketResponse
import com.neniukov.tradebot.data.model.response.KLineSocketWrapper
import com.neniukov.tradebot.data.model.response.TickerWrapper
import com.neniukov.tradebot.data.network.RetrofitFactoryImpl.Companion.SOCKET_URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject

class WebSocketServiceImpl @Inject constructor() : WebSocketService {

    private val _events = MutableSharedFlow<KLineSocketWrapper>()
    override val events: Flow<KLineSocketWrapper> get() = _events.asSharedFlow()

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    private val scope = CoroutineScope(Dispatchers.IO)

    private val isConnected = MutableStateFlow<Boolean?>(null)
    override val connectionState = isConnected.asStateFlow()

    private val tickers = hashMapOf<String, MutableList<KLineSocketResponse>>()

    override fun connect() {
        val request = Request.Builder()
            .url("$SOCKET_URL/public/linear")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnected.value = true
                println("WebSocket Opened")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                if (text.contains("tickers.")) {
                    Log.e("webSocket", "tickers")
                    val wrapper = Json.decodeFromString<TickerWrapper>(text)
                    val price = wrapper.data.lastPrice?.toDoubleOrNull()
                        ?: wrapper.data.bid1Price?.toDoubleOrNull()
                        ?: wrapper.data.ask1Price?.toDoubleOrNull()
                    val volume = wrapper.data.turnover24h?.toDoubleOrNull() ?: 0.0
                    if (price != null && volume > 15_000_000) {
//                        scope.launch {
//                            _events.emit(wrapper)
//                        }
                    }
                } else if (text.contains("kline.")) {
                    val wrapper = Json.decodeFromString<KLineSocketWrapper>(text)
                    tickers[wrapper.topic]?.addAll(wrapper.data) ?: run {
                        tickers[wrapper.topic] = wrapper.data.toMutableList()
                    }
                    val volume = wrapper.data.firstOrNull()?.turnover?.toDoubleOrNull() ?: 0.0
//                    if (volume > 15_000_000) {
                        scope.launch {
                            val symbol = wrapper.topic.split(".").last()
                            _events.emit(wrapper.copy(topic = symbol))
                        }
//                        Log.e("webSocket", "wrapper ${tickers[wrapper.topic]?.size}")
//                    }
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                isConnected.value = false
                webSocket.close(code, reason)
                println("WebSocket Closing: $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("WebSocket Error: ${t.message} ${response?.body?.string()}}")
                disconnect()
                isConnected.value = false
            }
        })
    }

    override fun disconnect() {
        webSocket?.close(1000, "Normal Closure")
    }

    override fun send(message: String) {
        webSocket?.send(message)
    }
}