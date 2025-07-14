package com.neniukov.tradebot.data.repository

import android.util.Log
import com.google.gson.Gson
import com.neniukov.tradebot.data.local.SharedPrefs
import com.neniukov.tradebot.data.managers.OrderManager
import com.neniukov.tradebot.data.model.request.LeverageRequest
import com.neniukov.tradebot.data.model.request.OrderRequest
import com.neniukov.tradebot.data.model.request.StopLossRequest
import com.neniukov.tradebot.data.model.request.WebSocketSubscribeRequest
import com.neniukov.tradebot.data.model.response.BybitResponse
import com.neniukov.tradebot.data.model.response.KLineSocketWrapper
import com.neniukov.tradebot.data.model.response.KlineResponse
import com.neniukov.tradebot.data.model.response.ListPositionResponse
import com.neniukov.tradebot.data.model.response.OrderResponse
import com.neniukov.tradebot.data.model.response.PositionResponse
import com.neniukov.tradebot.data.model.response.TickerUpdateResponse
import com.neniukov.tradebot.data.model.response.TickerWrapper
import com.neniukov.tradebot.data.service.BybitApiService
import com.neniukov.tradebot.data.socket.WebSocketService
import com.neniukov.tradebot.data.utils.SignatureUtils.generateSignature
import com.neniukov.tradebot.domain.model.Candle
import com.neniukov.tradebot.domain.model.Ticker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class BybitRepository @Inject constructor(
    private val bybitService: BybitApiService,
    private val orderManager: OrderManager,
    private val prefs: SharedPrefs,
    private val webSocket: WebSocketService
) {

    init {
        webSocket.connect()
    }

    val events: Flow<KLineSocketWrapper> = webSocket.events
    val connectionState: Flow<Boolean?> = webSocket.connectionState

    suspend fun getTickers(): List<String> {
        return withContext(Dispatchers.IO) {
            val tickers = bybitService.getTickers().result?.list?.map { it.symbol }.orEmpty()
            prefs.saveTickers(tickers)
            tickers
        }
    }

    suspend fun getAllPositions(): List<PositionResponse> {
        val apiKey = prefs.getApiKey()
        val apiSecretKey = prefs.getSecretKey()
        if (apiKey.isNullOrEmpty() || apiSecretKey.isNullOrEmpty()) {
            return emptyList()
        }
        val serverTime = getServerTime()
        val localTime = System.currentTimeMillis()
        val timeOffset = (serverTime * 1000) - localTime
        val correctedTime = System.currentTimeMillis() + timeOffset

        val apiTimestamp = correctedTime.toString()
        val recvWindow = "10000"

        val stringToSign =
            apiTimestamp + apiKey + recvWindow + "category=linear&settleCoin=USDT"
        val signature = generateSignature(stringToSign, apiSecretKey)

        val response = bybitService.getPositions(
            apiKey = apiKey,
            timestamp = apiTimestamp,
            signature = signature,
            recvWindow = recvWindow,
        )

        return if (response.retCode == 0) {
            response.result?.list.orEmpty()
        } else {
            Log.e("BybitRepository", "Error fetching positions: ${response.retMsg}")
            emptyList()
        }
    }

    suspend fun setTPAndSL(currentPosition: PositionResponse, candles: List<Candle>) {
        val apiKey = prefs.getApiKey()
        val apiSecretKey = prefs.getSecretKey()
        if (apiKey.isNullOrEmpty() || apiSecretKey.isNullOrEmpty()) {
            return
        }
        val serverTime = getServerTime()
        val localTime = System.currentTimeMillis()
        val timeOffset = (serverTime * 1000) - localTime
        val correctedTime = System.currentTimeMillis() + timeOffset

        val apiTimestamp = correctedTime.toString()
        val recvWindow = "10000"

        val (tp, sl) = orderManager.getTPAndSL(candles, currentPosition)

        val body = StopLossRequest(
            symbol = currentPosition.symbol,
            takeProfit = tp.toString(),
            stopLoss = sl.toString(),
            positionIdx = currentPosition.positionIdx,
        )

        val jsonBody = Gson().toJson(body)

        val stringToSign = apiTimestamp + apiKey + recvWindow + jsonBody
        val signature = generateSignature(stringToSign, apiSecretKey)

        bybitService.setTPAndSL(
            apiKey = apiKey,
            timestamp = apiTimestamp,
            signature = signature,
            recvWindow = recvWindow,
            body = body
        )
    }

    suspend fun setTP(currentPosition: PositionResponse, tp: Double) {
        val apiKey = prefs.getApiKey()
        val apiSecretKey = prefs.getSecretKey()
        if (apiKey.isNullOrEmpty() || apiSecretKey.isNullOrEmpty()) {
            return
        }
        val serverTime = getServerTime()
        val localTime = System.currentTimeMillis()
        val timeOffset = (serverTime * 1000) - localTime
        val correctedTime = System.currentTimeMillis() + timeOffset

        val apiTimestamp = correctedTime.toString()
        val recvWindow = "10000"

        val body = StopLossRequest(
            symbol = currentPosition.symbol,
            takeProfit = tp.toString(),
            positionIdx = currentPosition.positionIdx,
        )

        val jsonBody = Gson().toJson(body)

        val stringToSign = apiTimestamp + apiKey + recvWindow + jsonBody
        val signature = generateSignature(stringToSign, apiSecretKey)

        bybitService.setTPAndSL(
            apiKey = apiKey,
            timestamp = apiTimestamp,
            signature = signature,
            recvWindow = recvWindow,
            body = body
        )
    }

    suspend fun setTPAndSL(symbol: String, tp: String, sl: String) {
        val apiKey = prefs.getApiKey()
        val apiSecretKey = prefs.getSecretKey()
        if (apiKey.isNullOrEmpty() || apiSecretKey.isNullOrEmpty()) {
            return
        }
        val serverTime = getServerTime()
        val localTime = System.currentTimeMillis()
        val timeOffset = (serverTime * 1000) - localTime
        val correctedTime = System.currentTimeMillis() + timeOffset

        val apiTimestamp = correctedTime.toString()
        val recvWindow = "10000"

        val body = StopLossRequest(
            symbol = symbol,
            takeProfit = tp,
            stopLoss = sl,
            positionIdx = null
        )

        val jsonBody = Gson().toJson(body)

        val stringToSign = apiTimestamp + apiKey + recvWindow + jsonBody
        val signature = generateSignature(stringToSign, apiSecretKey)

        bybitService.setTPAndSL(
            apiKey = apiKey,
            timestamp = apiTimestamp,
            signature = signature,
            recvWindow = recvWindow,
            body = body
        )
    }

    suspend fun getBalance(): String? {
        return withContext(Dispatchers.IO) {
            val apiKey = prefs.getApiKey()
            val apiSecretKey = prefs.getSecretKey()
            if (apiKey.isNullOrEmpty() || apiSecretKey.isNullOrEmpty()) {
                return@withContext null
            }
            val serverTime = getServerTime()
            val localTime = System.currentTimeMillis()
            val timeOffset = (serverTime * 1000) - localTime
            val correctedTime = System.currentTimeMillis() + timeOffset

            val apiTimestamp = correctedTime.toString()
            val recvWindow = "10000"

            val accountType = "UNIFIED"
            val stringToSign = apiTimestamp + apiKey + recvWindow + "accountType=$accountType"
            val signature = generateSignature(stringToSign, apiSecretKey)

            val response = bybitService.getBalance(
                apiKey = apiKey,
                timestamp = apiTimestamp,
                signature = signature,
                recvWindow = recvWindow,
                accountType = accountType
            )

            if (response.retCode == 0) {
                val balance = response.result?.list?.firstOrNull()?.totalEquity.orEmpty()
                if (balance.isNotEmpty()) {
                    val number = balance.toDouble()
                    String.format("%.2f", number)
                } else {
                    "0.0"
                }
            } else {
                Log.e("BybitRepository", "Error fetching balance: ${response.retMsg}")
                "-.-"
            }
        }
    }

    suspend fun connectToBybit(apiKey: String, secretKey: String): String {
        return withContext(Dispatchers.IO) {
            val serverTime = getServerTime()
            val localTime = System.currentTimeMillis()
            val timeOffset = (serverTime * 1000) - localTime
            val correctedTime = System.currentTimeMillis() + timeOffset

            val apiTimestamp = correctedTime.toString()
            val recvWindow = "10000"

            val accountType = "UNIFIED"
            val stringToSign = apiTimestamp + apiKey + recvWindow + "accountType=$accountType"
            val signature = generateSignature(stringToSign, secretKey)

            val response = bybitService.getBalance(
                apiKey = apiKey,
                timestamp = apiTimestamp,
                signature = signature,
                recvWindow = recvWindow,
                accountType = accountType
            )

            if (response.retCode == 0) {
                val balance = response.result?.list?.firstOrNull()?.totalEquity.orEmpty()
                if (balance.isNotEmpty()) {
                    val number = balance.toDouble()
                    String.format("%.2f", number)
                } else {
                    "0.0"
                }
            } else {
                Log.e("BybitRepository", "Error fetching balance: ${response.retMsg}")
                response.retMsg
            }
        }
    }

    suspend fun getTickerInfo(symbol: String): Ticker {
        return withContext(Dispatchers.IO) {
            val response = bybitService.getTickerInfo(symbol)
            if (response.retCode == 0) {
                val responseTicker = response.result?.list?.firstOrNull { it.symbol == symbol }
                if (responseTicker != null) {
                    Ticker(
                        responseTicker.symbol,
                        "",
                        responseTicker.leverageFilter?.minLeverage ?: "1",
                        responseTicker.leverageFilter?.maxLeverage ?: "10",
                    )
                } else {
                    Ticker(symbol, "")
                }
            } else {
                Log.e("BybitRepository", "Error fetching ticker info: ${response.retMsg}")
                Ticker(symbol, "")
            }
        }
    }

    suspend fun setLeverage(symbol: String, leverage: String) {
        val apiKey = prefs.getApiKey()
        val apiSecretKey = prefs.getSecretKey()
        if (apiKey.isNullOrEmpty() || apiSecretKey.isNullOrEmpty()) {
            return
        }
        val serverTimeResponse = bybitService.getServerTime()
        val localTime = System.currentTimeMillis()
        val timeOffset = (serverTimeResponse.result.timeSecond * 1000) - localTime
        val correctedTime = System.currentTimeMillis() + timeOffset

        val apiTimestamp = correctedTime.toString()
        val recvWindow = "10000"

        val body = LeverageRequest(
            symbol = symbol,
            buyLeverage = leverage,
            sellLeverage = leverage,
        )

        val jsonBody = Gson().toJson(body)

        val stringToSign = apiTimestamp + apiKey + recvWindow + jsonBody
        val signature = generateSignature(stringToSign, apiSecretKey)

        bybitService.setLeverage(
            apiKey = apiKey,
            timestamp = apiTimestamp,
            signature = signature,
            recvWindow = recvWindow,
            body = body
        )
    }

    suspend fun getMarkPriceKline(symbol: String, interval: Int): KlineResponse? {
        return bybitService.getMarkPriceKline(symbol, interval).result
    }

    suspend fun placeMarketOrder(symbol: String, side: String, qty: String, leverage: String = "10"): OrderResponse? {
        setLeverage(symbol, leverage)
        val apiKey = prefs.getApiKey()
        val apiSecretKey = prefs.getSecretKey()
        if (apiKey.isNullOrEmpty() || apiSecretKey.isNullOrEmpty()) {
            return null
        }
        val serverTime = getServerTime()
        val timeOffset = (serverTime * 1000) - System.currentTimeMillis()
        val apiTimestamp = (System.currentTimeMillis() + timeOffset).toString()
        val recvWindow = "10000"

        val request = OrderRequest(
            symbol = symbol,
            side = side,
            qty = qty,
        )

        val jsonBody = Gson().toJson(request)

        val stringToSign = apiTimestamp + apiKey + recvWindow + jsonBody
        val signature = generateSignature(stringToSign, apiSecretKey)

        return bybitService.placeOrder(
            apiKey = apiKey,
            timestamp = apiTimestamp,
            signature = signature,
            recvWindow = recvWindow,
            body = request
        )
    }

    suspend fun placeLimitOrder(request: OrderRequest): OrderResponse? {
        val apiKey = prefs.getApiKey()
        val apiSecretKey = prefs.getSecretKey()
        if (apiKey.isNullOrEmpty() || apiSecretKey.isNullOrEmpty()) {
            return null
        }
        val serverTime = getServerTime()
        val timeOffset = (serverTime * 1000) - System.currentTimeMillis()
        val apiTimestamp = (System.currentTimeMillis() + timeOffset).toString()
        val recvWindow = "10000"

        val jsonBody = Gson().toJson(request)

        val stringToSign = apiTimestamp + apiKey + recvWindow + jsonBody
        val signature = generateSignature(stringToSign, apiSecretKey)

        return bybitService.placeOrder(
            apiKey = apiKey,
            timestamp = apiTimestamp,
            signature = signature,
            recvWindow = recvWindow,
            body = request
        )
    }

    suspend fun loadCurrentPosition(symbol: String): BybitResponse<ListPositionResponse>? {
        val apiKey = prefs.getApiKey()
        val apiSecretKey = prefs.getSecretKey()
        if (apiKey.isNullOrEmpty() || apiSecretKey.isNullOrEmpty()) {
            return null
        }
        val serverTime = bybitService.getServerTime().result
        val localTime = System.currentTimeMillis()
        val timeOffset = (serverTime.timeSecond * 1000) - localTime
        val correctedTime = System.currentTimeMillis() + timeOffset

        val apiTimestamp = correctedTime.toString()
        val recvWindow = "10000"

        val stringToSign = apiTimestamp + apiKey + recvWindow + "category=linear&symbol=$symbol"
        val signature = generateSignature(stringToSign, apiSecretKey)

        return bybitService.getPosition(
            apiKey = apiKey,
            timestamp = apiTimestamp,
            signature = signature,
            recvWindow = recvWindow,
            symbol = symbol
        )
    }

    fun connectToWebSocket() {
        webSocket.connect()
    }

    suspend fun loadPriceForTickers() {
        loadKline()
//        val savedTickers = prefs.getTickers()
//        val args = savedTickers.map { "tickers.$it" }
//        val request = WebSocketSubscribeRequest(op = "subscribe", args = args)
//        val json = Json.encodeToString(WebSocketSubscribeRequest.serializer(), request)
//        webSocket.send(json)
    }

    suspend fun loadKline() {
        val savedTickers = prefs.getTickers()
        val args = savedTickers.map { "kline.1.$it" }
        val request = WebSocketSubscribeRequest(op = "subscribe", args = args)
        val json = Json.encodeToString(WebSocketSubscribeRequest.serializer(), request)
        webSocket.send(json)
    }

    private suspend fun getServerTime(): Long {
        return bybitService.getServerTime().result.timeSecond
    }
}