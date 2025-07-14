package com.neniukov.tradebot.data.local

import android.content.Context
import com.google.crypto.tink.Aead
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import javax.inject.Inject

class CryptoManager @Inject constructor(context: Context) {

    private var aead: Aead? = null

    init {
        AeadConfig.register()

        val keysetHandle = AndroidKeysetManager.Builder()
            .withSharedPref(context, KEYSET_NAME, PREF_FILE_NAME)
            .withKeyTemplate(com.google.crypto.tink.aead.AesGcmKeyManager.aes256GcmTemplate())
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle

        aead = keysetHandle.getPrimitive(Aead::class.java)
    }

    fun encrypt(plaintext: String): String {
        val cipher = aead ?: throw IllegalStateException("CryptoManager not initialized")
        val ciphertext = cipher.encrypt(plaintext.toByteArray(), null)
        return android.util.Base64.encodeToString(ciphertext, android.util.Base64.DEFAULT)
    }

    fun decrypt(encrypted: String): String {
        val cipher = aead ?: throw IllegalStateException("CryptoManager not initialized")
        val decoded = android.util.Base64.decode(encrypted, android.util.Base64.DEFAULT)
        val decrypted = cipher.decrypt(decoded, null)
        return String(decrypted)
    }

    companion object {
        private const val KEYSET_NAME = "master_keyset"
        private const val PREF_FILE_NAME = "secure_prefs"
        private const val MASTER_KEY_URI = "android-keystore://tink_master_key"
    }
}