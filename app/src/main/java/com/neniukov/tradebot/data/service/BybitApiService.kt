package com.neniukov.tradebot.data.service

import com.neniukov.tradebot.data.model.request.LeverageRequest
import com.neniukov.tradebot.data.model.request.OrderRequest
import com.neniukov.tradebot.data.model.request.StopLossRequest
import com.neniukov.tradebot.data.model.response.BybitResponse
import com.neniukov.tradebot.data.model.response.KlineResponse
import com.neniukov.tradebot.data.model.response.ListBalanceResponse
import com.neniukov.tradebot.data.model.response.ListPositionResponse
import com.neniukov.tradebot.data.model.response.ListStatisticsResponse
import com.neniukov.tradebot.data.model.response.OrderResponse
import com.neniukov.tradebot.data.model.response.TickersResponse
import com.neniukov.tradebot.data.model.response.TimeResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface BybitApiService {

    @GET("v5/market/kline")
    suspend fun getMarkPriceKline(
        @Query("symbol") symbol: String,
        @Query("interval") interval: Int,
    ): BybitResponse<KlineResponse>

    @POST("v5/order/create")
    suspend fun placeOrder(
        @Header("X-BAPI-API-KEY") apiKey: String,
        @Header("X-BAPI-TIMESTAMP") timestamp: String,
        @Header("X-BAPI-SIGN") signature: String,
        @Header("X-BAPI-RECV-WINDOW") recvWindow: String = "10000",
        @Body body: OrderRequest,
    ): OrderResponse

    @GET("v5/market/time")
    suspend fun getServerTime(): TimeResponse

    @GET("/v5/position/list")
    suspend fun getPosition(
        @Header("X-BAPI-API-KEY") apiKey: String,
        @Header("X-BAPI-TIMESTAMP") timestamp: String,
        @Header("X-BAPI-SIGN") signature: String,
        @Header("X-BAPI-RECV-WINDOW") recvWindow: String = "10000",
        @Query("category") category: String = "linear",
        @Query("symbol") symbol: String?
    ): BybitResponse<ListPositionResponse>

    @POST("/v5/position/trading-stop")
    suspend fun setTPAndSL(
        @Header("X-BAPI-API-KEY") apiKey: String,
        @Header("X-BAPI-TIMESTAMP") timestamp: String,
        @Header("X-BAPI-SIGN") signature: String,
        @Header("X-BAPI-RECV-WINDOW") recvWindow: String = "10000",
        @Body body: StopLossRequest
    ): Response<Unit>

    @GET("/v5/position/list")
    suspend fun getPositions(
        @Header("X-BAPI-API-KEY") apiKey: String,
        @Header("X-BAPI-TIMESTAMP") timestamp: String,
        @Header("X-BAPI-SIGN") signature: String,
        @Header("X-BAPI-RECV-WINDOW") recvWindow: String = "10000",
        @Query("category") category: String = "linear",
        @Query("settleCoin") settleCoin: String = "USDT"
    ): BybitResponse<ListPositionResponse>

    @GET("v5/market/tickers")
    suspend fun getTickers(
        @Query("category") category: String = "linear"
    ): BybitResponse<TickersResponse>

    @GET("/v5/account/wallet-balance")
    suspend fun getBalance(
        @Header("X-BAPI-API-KEY") apiKey: String,
        @Header("X-BAPI-TIMESTAMP") timestamp: String,
        @Header("X-BAPI-SIGN") signature: String,
        @Header("X-BAPI-RECV-WINDOW") recvWindow: String = "10000",
        @Query("accountType") accountType: String
    ): BybitResponse<ListBalanceResponse>

    @GET("/v5/market/instruments-info")
    suspend fun getTickerInfo(
        @Query("symbol") symbol: String,
        @Query("category") category: String = "linear"
    ): BybitResponse<TickersResponse>

    @POST("/v5/position/set-leverage")
    suspend fun setLeverage(
        @Header("X-BAPI-API-KEY") apiKey: String,
        @Header("X-BAPI-TIMESTAMP") timestamp: String,
        @Header("X-BAPI-SIGN") signature: String,
        @Header("X-BAPI-RECV-WINDOW") recvWindow: String = "10000",
        @Body body: LeverageRequest
    ): BybitResponse<Unit>

    @GET("/v5/position/closed-pnl")
    suspend fun getStatistics(
        @Header("X-BAPI-API-KEY") apiKey: String,
        @Header("X-BAPI-TIMESTAMP") timestamp: String,
        @Header("X-BAPI-SIGN") signature: String,
        @Header("X-BAPI-RECV-WINDOW") recvWindow: String = "10000",
        @Query("category") category: String = "linear",
        @Query("startTime") startTime: String?,
        @Query("endTime") endTime: String?,
        @Query("limit") limit: Int = 100,
    ): BybitResponse<ListStatisticsResponse>
}