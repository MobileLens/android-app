package com.mobilelens.mobilelens.core.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val TAG = "AuthTokenStore"
private const val TOKEN_FILE = "auth_token.enc"
private const val KEYSTORE_ALIAS = "mobilelens_auth_token"
private const val ANDROID_KEYSTORE = "AndroidKeyStore"
private const val TRANSFORMATION = "AES/GCM/NoPadding"
private const val GCM_TAG_LENGTH_BITS = 128

/**
 * Bearer token encrypted with AES-GCM via the Android Keystore and stored under
 * [Context.getNoBackupFilesDir] (survives process death / updates, never Auto Backed up).
 */
class AuthTokenStore private constructor(
    private val tokenFile: File,
) {
    @Volatile
    private var cached: String? = null

    var token: String?
        get() = cached
        set(value) {
            cached = value?.takeIf { it.isNotEmpty() }
            persist(cached)
        }

    fun clear() {
        token = null
    }

    private fun persist(value: String?) {
        try {
            if (value == null) {
                if (tokenFile.exists() && !tokenFile.delete()) {
                    Log.w(TAG, "Failed to delete auth token file")
                }
                return
            }
            tokenFile.writeBytes(encrypt(value))
        } catch (e: Exception) {
            Log.w(TAG, "Failed to persist auth token", e)
        }
    }

    private fun loadIntoCache() {
        cached = try {
            if (!tokenFile.isFile || tokenFile.length() == 0L) return
            decrypt(tokenFile.readBytes())
        } catch (e: Exception) {
            Log.w(TAG, "Clearing unreadable auth token", e)
            tokenFile.delete()
            null
        }
    }

    companion object {
        @Volatile
        private var instance: AuthTokenStore? = null

        fun getInstance(context: Context): AuthTokenStore {
            return instance ?: synchronized(this) {
                instance ?: create(context.applicationContext).also { instance = it }
            }
        }

        private fun create(context: Context): AuthTokenStore {
            // Drop leftovers from earlier storage backends
            context.deleteSharedPreferences("auth_token_prefs")
            context.filesDir.resolve("datastore").resolve("auth_token.preferences_pb").delete()

            val store = AuthTokenStore(File(context.noBackupFilesDir, TOKEN_FILE))
            store.loadIntoCache()
            return store
        }

        private fun getOrCreateSecretKey(): SecretKey {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            (keyStore.getKey(KEYSTORE_ALIAS, null) as? SecretKey)?.let { return it }

            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE,
            )
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    KEYSTORE_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
            return keyGenerator.generateKey()
        }

        private fun encrypt(plain: String): ByteArray {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
            val iv = cipher.iv
            val ciphertext = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
            // IV length (1 byte) + IV + ciphertext
            val blob = ByteArray(1 + iv.size + ciphertext.size)
            blob[0] = iv.size.toByte()
            System.arraycopy(iv, 0, blob, 1, iv.size)
            System.arraycopy(ciphertext, 0, blob, 1 + iv.size, ciphertext.size)
            return blob
        }

        private fun decrypt(blob: ByteArray): String {
            val ivLen = blob[0].toInt() and 0xff
            val iv = blob.copyOfRange(1, 1 + ivLen)
            val ciphertext = blob.copyOfRange(1 + ivLen, blob.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                getOrCreateSecretKey(),
                GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv),
            )
            return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
        }
    }
}
