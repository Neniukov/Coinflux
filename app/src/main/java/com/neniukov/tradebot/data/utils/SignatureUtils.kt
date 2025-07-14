package com.neniukov.tradebot.data.utils

import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object SignatureUtils {
    fun generateSignature(data: String, apiSecretKey: String): String {
        val hmac = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(apiSecretKey.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
        hmac.init(secretKey)
        val hash = hmac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
}