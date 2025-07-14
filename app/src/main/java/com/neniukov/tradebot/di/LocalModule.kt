package com.neniukov.tradebot.di

import android.app.Application
import com.neniukov.tradebot.data.local.CryptoManager
import com.neniukov.tradebot.data.local.SharedPrefs
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class LocalModule {

    @Singleton
    @Provides
    fun provideSharedPrefs(context: Application, cryptoManager: CryptoManager) = SharedPrefs(context, cryptoManager)

    @Provides
    fun provideCryptoManager(context: Application) = CryptoManager(context)
}