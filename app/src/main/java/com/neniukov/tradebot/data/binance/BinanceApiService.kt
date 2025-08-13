package com.neniukov.tradebot.data.binance

import com.neniukov.tradebot.data.binance.model.response.BinanceBalance
import com.neniukov.tradebot.data.binance.model.response.BinanceLeverageBracketResponse
import com.neniukov.tradebot.data.binance.model.response.BinanceOrderResponse
import com.neniukov.tradebot.data.binance.model.response.BinancePositionResponse
import com.neniukov.tradebot.data.binance.model.response.BinanceTickersResponse
import com.neniukov.tradebot.data.binance.model.response.LeverageResponse
import com.neniukov.tradebot.data.binance.model.response.OpenOrderResponse
import com.neniukov.tradebot.data.binance.model.response.ServerTimeResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface BinanceApiService {

    @GET("fapi/v1/time")
    suspend fun getServerTime(): ServerTimeResponse

    @GET("fapi/v1/exchangeInfo")
    suspend fun getTickers(): BinanceTickersResponse

    @GET("fapi/v3/balance")
    suspend fun getBalance(
        @Header("X-MBX-APIKEY") apiKey: String,
        @Query("timestamp") timestamp: Long,
        @Query("signature") signature: String
    ):  List<BinanceBalance>

    @GET("fapi/v1/klines")
    suspend fun getMarkPriceKline(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
    ): List<List<String>>

    @GET("fapi/v1/leverageBracket")
    suspend fun getLeverageBrackets(
        @Header("X-MBX-APIKEY") apiKey: String,
        @Query("timestamp") timestamp: Long,
        @Query("signature") signature: String
    ): List<BinanceLeverageBracketResponse>

    @POST("fapi/v1/leverage")
    suspend fun changeLeverage(
        @Header("X-MBX-APIKEY") apiKey: String,
        @Query("symbol") symbol: String,
        @Query("leverage") leverage: String,
        @Query("timestamp") timestamp: Long,
        @Query("signature") signature: String
    ): LeverageResponse

    @POST("fapi/v1/order")
    suspend fun placeOrder(
        @Header("X-MBX-APIKEY") apiKey: String,
        @Query("symbol") symbol: String,
        @Query("side") side: String,
        @Query("type") type: String = "MARKET",
        @Query("quantity") quantity: String,
        @Query("timestamp") timestamp: Long,
        @Query("signature") signature: String
    ): BinanceOrderResponse

    @POST("fapi/v1/order")
    suspend fun setTakeProfit(
        @Header("X-MBX-APIKEY") apiKey: String,
        @Query("symbol") symbol: String,
        @Query("side") side: String,
        @Query("type") type: String = "LIMIT",
        @Query("quantity") quantity: String,
        @Query("price") price: String,
        @Query("timeInForce") timeInForce: String = "GTC",
        @Query("reduceOnly") reduceOnly: Boolean = true,
        @Query("timestamp") timestamp: Long,
        @Query("signature") signature: String
    ): BinanceOrderResponse

    @GET("fapi/v2/positionRisk")
    suspend fun getPositions(
        @Header("X-MBX-APIKEY") apiKey: String,
        @Query("timestamp") timestamp: Long,
        @Query("signature") signature: String
    ): List<BinancePositionResponse>

    @GET("fapi/v1/openOrders")
    suspend fun getOpenOrders(
        @Header("X-MBX-APIKEY") apiKey: String,
        @Query("symbol") symbol: String? = null,
        @Query("timestamp") timestamp: Long,
        @Query("signature") signature: String
    ): List<OpenOrderResponse>

    @DELETE("fapi/v1/allOpenOrders")
    suspend fun cancelAllOrders(
        @Header("X-MBX-APIKEY") apiKey: String,
        @Query("symbol") symbol: String,
        @Query("timestamp") timestamp: Long,
        @Query("signature") signature: String
    ): Response<Unit>
}