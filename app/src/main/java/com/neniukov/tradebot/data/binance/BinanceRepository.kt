package com.neniukov.tradebot.data.binance

import android.util.Log
import com.neniukov.tradebot.data.binance.model.response.BinanceOrderResponse
import com.neniukov.tradebot.data.binance.model.response.BinancePositionResponse
import com.neniukov.tradebot.data.binance.model.response.OpenOrderResponse
import com.neniukov.tradebot.data.local.SharedPrefs
import com.neniukov.tradebot.data.utils.SignatureUtils.generateSignature
import com.neniukov.tradebot.domain.model.Ticker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import kotlin.math.sign

class BinanceRepository @Inject constructor(
    private val api: BinanceApiService,
    private val prefs: SharedPrefs,
) {

    suspend fun getTickers(): List<String> {
        return withContext(Dispatchers.IO) {
            val tickers = api.getTickers().symbols?.map { it.symbol }.orEmpty()
            prefs.saveTickers(tickers)
            tickers
        }
    }

    suspend fun connectToBinance(apiKey: String, secretKey: String): String {
        return withContext(Dispatchers.IO) {
            val serverTime = api.getServerTime().serverTime
            val query = "timestamp=$serverTime"
            val signature = generateSignature(query, secretKey)
            val response = api.getBalance(apiKey, serverTime, signature)
            response.firstOrNull { it.asset == "USDT" }?.balance ?: "-.-"
        }
    }

    suspend fun getBalance(): String {
        return withContext(Dispatchers.IO) {
            val apiKey = prefs.getApiKey()
            if (apiKey.isNullOrBlank()) return@withContext "-.-"
            val secretKey = prefs.getSecretKey()
            val serverTime = api.getServerTime().serverTime
            val query = "timestamp=$serverTime"
            val signature = generateSignature(query, secretKey.orEmpty())
            val response = api.getBalance(apiKey, serverTime, signature)
            response.firstOrNull { it.asset == "USDT" }?.balance ?: "-.-"
        }
    }

    suspend fun getMarkPriceKline(symbol: String, interval: Int): List<List<String>> {
        return api.getMarkPriceKline(symbol, "${interval}m")
    }

    suspend fun getTickerInfo(symbol: String): Ticker {
        return withContext(Dispatchers.IO) {
            val apiKey = prefs.getApiKey()
            val apiSecretKey = prefs.getSecretKey()
            if (apiKey.isNullOrEmpty() || apiSecretKey.isNullOrEmpty()) {
                return@withContext Ticker(symbol, "")
            }
            val timestamp = api.getServerTime().serverTime
            val query = "timestamp=$timestamp"
            val signature = generateSignature(query, apiSecretKey)

            api.getLeverageBrackets(apiKey, timestamp, signature).firstOrNull {
                it.symbol == symbol
            }?.let { leverageInfo ->
                val maxLeverage = leverageInfo.brackets.maxOfOrNull { it.initialLeverage } ?: 1
                val minLeverage = leverageInfo.brackets.minOfOrNull { it.initialLeverage } ?: 1
                return@withContext Ticker(symbol, "", minLeverage.toString(), maxLeverage.toString())
            } ?: kotlin.run { Ticker(symbol, "") }
        }
    }

    suspend fun setLeverage(symbol: String, leverage: String) {
        withContext(Dispatchers.IO) {
            val apiKey = prefs.getApiKey()
            val apiSecretKey = prefs.getSecretKey()
            if (apiKey.isNullOrEmpty() || apiSecretKey.isNullOrEmpty()) {
                return@withContext
            }
            val timestamp = api.getServerTime().serverTime
            val query = "symbol=$symbol&leverage=$leverage&timestamp=$timestamp"
            val signature = generateSignature(query, apiSecretKey)
            api.changeLeverage(
                apiKey = apiKey,
                symbol = symbol,
                leverage = leverage,
                timestamp = timestamp,
                signature = signature
            )
        }
    }

    suspend fun placeMarketOrder(
        symbol: String,
        side: String,
        quantity: String
    ): BinanceOrderResponse? {
        return withContext(Dispatchers.IO) {
            val apiKey = prefs.getApiKey()
            val apiSecretKey = prefs.getSecretKey()
            if (apiKey.isNullOrEmpty() || apiSecretKey.isNullOrEmpty()) {
                return@withContext null
            }
            val timestamp = api.getServerTime().serverTime
            val params =
                "symbol=$symbol&side=${side.uppercase()}&type=MARKET&quantity=$quantity&timestamp=$timestamp"
            val signature = generateSignature(params, apiSecretKey)
            api.placeOrder(
                apiKey = apiKey,
                symbol = symbol,
                side = side.uppercase(),
                type = "MARKET",
                quantity = quantity,
                timestamp = timestamp,
                signature = signature
            )
        }
    }

    suspend fun setTP(
        symbol: String,
        side: String,
        quantity: String,
        closePrice: String
    ): BinanceOrderResponse? {
        return withContext(Dispatchers.IO) {
            val apiKey = prefs.getApiKey()
            val apiSecretKey = prefs.getSecretKey()
            if (apiKey.isNullOrEmpty() || apiSecretKey.isNullOrEmpty()) {
                return@withContext null
            }
            val rawPrice = closePrice.toDouble()
            val formattedPrice = String.format(Locale.US, "%.4f", rawPrice)
            val timestamp = api.getServerTime().serverTime
            val query =
                "symbol=$symbol&side=$side&type=LIMIT&quantity=$quantity&price=$formattedPrice&timeInForce=GTC&reduceOnly=true&timestamp=$timestamp"
            val signature = generateSignature(query, apiSecretKey)

            api.setTakeProfit(
                apiKey = apiKey,
                symbol = symbol,
                side = side,
                quantity = quantity,
                price = formattedPrice,
                timestamp = timestamp,
                signature = signature
            )
        }
    }

    suspend fun getAllPositions(): List<BinancePositionResponse> {
        return withContext(Dispatchers.IO) {
            val apiKey = prefs.getApiKey()
            val apiSecretKey = prefs.getSecretKey()
            if (apiKey.isNullOrEmpty() || apiSecretKey.isNullOrEmpty()) {
                return@withContext emptyList()
            }
            val serverTime = api.getServerTime().serverTime
            val query = "timestamp=$serverTime"
            val signature = generateSignature(query, apiSecretKey)
            api.getPositions(apiKey, serverTime, signature).filter {
                it.positionAmt.let { amt ->
                    amt.isNotEmpty() && amt.toDouble() != 0.0
                }
            }
        }
    }

    suspend fun getOpenOrders(symbol: String? = null): List<OpenOrderResponse> {
        return withContext(Dispatchers.IO) {
            val apiKey = prefs.getApiKey()
            val apiSecretKey = prefs.getSecretKey()
            if (apiKey.isNullOrEmpty() || apiSecretKey.isNullOrEmpty()) {
                return@withContext emptyList()
            }
            val serverTime = api.getServerTime().serverTime
            val query = "symbol=$symbol&timestamp=$serverTime"
            val signature = generateSignature(query, apiSecretKey)
            api.getOpenOrders(apiKey, symbol, serverTime, signature)
        }
    }

    suspend fun cancelOpenOrders(symbol: String) {
        return withContext(Dispatchers.IO) {
            val apiKey = prefs.getApiKey()
            val apiSecretKey = prefs.getSecretKey()
            if (apiKey.isNullOrEmpty() || apiSecretKey.isNullOrEmpty()) {
                return@withContext
            }
            val serverTime = api.getServerTime().serverTime
            val query = "symbol=$symbol&timestamp=$serverTime"
            val signature = generateSignature(query, apiSecretKey)
            api.cancelAllOrders(
                apiKey = apiKey,
                symbol = symbol,
                timestamp = serverTime,
                signature = signature
            )
        }
    }
}