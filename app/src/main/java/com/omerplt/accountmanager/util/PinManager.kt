package com.omerplt.accountmanager.util

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class PinManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "app_security_encrypted",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val ITERATIONS = 120_000
        private const val KEY_LENGTH_BITS = 256
        private const val LOCKOUT_DURATION_MS = 30_000L
        private const val MAX_ATTEMPTS = 5
    }

    fun isPinSet(): Boolean = prefs.contains("pin_hash")

    fun setPin(pin: String) {
        val salt = generateSalt()
        val hash = deriveHash(pin, salt)
        prefs.edit()
            .putString("pin_hash", hash)
            .putString("pin_salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            .putInt("failed_attempts", 0)
            .putLong("lockout_time", 0)
            .apply()
    }

    fun removePin() {
        prefs.edit()
            .remove("pin_hash")
            .remove("pin_salt")
            .remove("failed_attempts")
            .remove("lockout_time")
            .apply()
    }

    fun verifyPin(pin: String): Boolean {
        if (isLockoutActive()) return false

        val storedHash = prefs.getString("pin_hash", null) ?: return false
        val saltString = prefs.getString("pin_salt", null) ?: return false
        val salt = Base64.decode(saltString, Base64.NO_WRAP)

        val isCorrect = deriveHash(pin, salt) == storedHash

        if (isCorrect) {
            prefs.edit().putInt("failed_attempts", 0).putLong("lockout_time", 0).apply()
        } else {
            val attempts = prefs.getInt("failed_attempts", 0) + 1
            prefs.edit().putInt("failed_attempts", attempts).apply()
            if (attempts >= MAX_ATTEMPTS) {
                prefs.edit().putLong("lockout_time", System.currentTimeMillis()).apply()
            }
        }
        return isCorrect
    }

    fun getRemainingLockoutSeconds(): Long {
        if (!isLockoutActive()) return 0
        val lockoutTime = prefs.getLong("lockout_time", 0)
        val elapsed = System.currentTimeMillis() - lockoutTime
        val remainingMillis = LOCKOUT_DURATION_MS - elapsed
        return if (remainingMillis > 0) TimeUnit.MILLISECONDS.toSeconds(remainingMillis) + 1 else 0
    }

    private fun isLockoutActive(): Boolean {
        val lockoutTime = prefs.getLong("lockout_time", 0)
        if (lockoutTime == 0L) return false
        val elapsed = System.currentTimeMillis() - lockoutTime
        if (elapsed > LOCKOUT_DURATION_MS) {
            prefs.edit().putInt("failed_attempts", 0).putLong("lockout_time", 0).apply()
            return false
        }
        return true
    }

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return salt
    }

    private fun deriveHash(pin: String, salt: ByteArray): String {
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hashBytes = factory.generateSecret(spec).encoded
        return Base64.encodeToString(hashBytes, Base64.NO_WRAP)
    }
}
