package com.neniukov.tradebot.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.neniukov.tradebot.data.binance.BinanceApiService
import com.neniukov.tradebot.data.binance.BinanceRepository
import com.neniukov.tradebot.data.local.SharedPrefs
import com.neniukov.tradebot.data.managers.AutomatedBotManager
import com.neniukov.tradebot.data.network.RetrofitFactoryImpl
import com.neniukov.tradebot.data.repository.BybitRepository
import com.neniukov.tradebot.data.managers.OrderManager
import com.neniukov.tradebot.data.managers.impl.EmaLongOrderManager
import com.neniukov.tradebot.data.service.BybitApiService
import com.neniukov.tradebot.data.socket.WebSocketService
import com.neniukov.tradebot.data.socket.WebSocketServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Singleton
    @Provides
    fun provideGson() = GsonBuilder().create()

    @Singleton
    @Provides
    fun provideRetrofit(gson: Gson): Retrofit =
        RetrofitFactoryImpl(OkHttpClient()).createRetrofit(gson)

    @Provides
    fun provideBybitService(retrofit: Retrofit) = retrofit.create(BybitApiService::class.java)

    @Provides
    fun provideBybitRepository(bybitService: BybitApiService, orderManager: OrderManager, prefs: SharedPrefs, webSocketService: WebSocketService) =
        BybitRepository(bybitService, orderManager, prefs, webSocketService)

    @Provides
    fun provideBinanceService(retrofit: Retrofit) = retrofit.create(BinanceApiService::class.java)

    @Provides
    fun provideBinanceRepository(api: BinanceApiService, prefs: SharedPrefs) =
        BinanceRepository(api, prefs)

    @Provides
    fun provideOrderManager(): OrderManager = EmaLongOrderManager()

    @Provides
    @Singleton
    fun provideWebSocketService(): WebSocketService = WebSocketServiceImpl()

    @Provides
    fun provideAutomatedBot(repository: BybitRepository) = AutomatedBotManager(repository)
}