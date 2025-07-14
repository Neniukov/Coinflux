package com.neniukov.tradebot.data.repository

import com.neniukov.tradebot.data.local.SharedPrefs
import com.neniukov.tradebot.data.model.response.StatisticsResponse
import com.neniukov.tradebot.data.service.BybitApiService
import com.neniukov.tradebot.data.utils.SignatureUtils
import javax.inject.Inject

class StatisticsRepository @Inject constructor(
    private val api: BybitApiService,
    private val prefs: SharedPrefs,
) {

    suspend fun getStatistics(startTime: Long, endTime: Long = System.currentTimeMillis()): List<StatisticsResponse> {
        val apiKey = prefs.getApiKey()
        val apiSecretKey = prefs.getSecretKey()
        if (apiKey.isNullOrEmpty() || apiSecretKey.isNullOrEmpty()) {
            return emptyList()
        }
        val serverTimeResponse = api.getServerTime()
        val localTime = System.currentTimeMillis()
        val timeOffset = (serverTimeResponse.result.timeSecond * 1000) - localTime
        val correctedTime = System.currentTimeMillis() + timeOffset

        val apiTimestamp = correctedTime.toString()
        val recvWindow = "10000"

        val stringToSign = apiTimestamp + apiKey + recvWindow + "category=linear&startTime=$startTime&endTime=$endTime&limit=100"
        val signature = SignatureUtils.generateSignature(stringToSign, apiSecretKey)

        val response = api.getStatistics(
            apiKey = apiKey,
            timestamp = apiTimestamp,
            signature = signature,
            recvWindow = recvWindow,
            startTime = startTime.toString(),
            endTime = endTime.toString()
        )
        return response.result?.list.orEmpty()
    }
}