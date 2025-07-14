package com.neniukov.tradebot.data.network

import com.google.gson.Gson
import retrofit2.Retrofit

interface RetrofitFactory {
    fun createRetrofit(gson: Gson): Retrofit
}