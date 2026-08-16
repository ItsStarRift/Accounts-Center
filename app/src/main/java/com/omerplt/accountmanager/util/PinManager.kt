package com.omerplt.accountmanager.util

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

class PinManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_security", Context.MODE_PRIVATE)

    fun isPinSet(): Boolean = prefs.contains("pin_hash")

    fun setPin(pin: String) {
        prefs.edit().putString("pin_hash", hash(pin)).apply()
    }

    fun removePin() {
        prefs.edit().remove("pin_hash").remove("failed_attempts").remove("lockout_time").apply()
    }

    fun verifyPin(pin: String): Boolean {
        if (isLockoutActive()) return false

        val correctHash = prefs.getString("pin_hash", null) ?: return false
        val isCorrect = hash(pin) == correctHash

        if (isCorrect) {
            prefs.edit().putInt("failed_attempts", 0).putLong("lockout_time", 0).apply()
        } else {
            val attempts = prefs.getInt("failed_attempts", 0) + 1
            prefs.edit().putInt("failed_attempts", attempts).apply()
            if (attempts >= 5) {
                prefs.edit().putLong("lockout_time", System.currentTimeMillis()).apply()
            }
        }
        return isCorrect
    }

    fun getRemainingLockoutSeconds(): Long {
        if (!isLockoutActive()) return 0
        val lockoutTime = prefs.getLong("lockout_time", 0)
        val elapsed = System.currentTimeMillis() - lockoutTime
        val remainingMillis = 30000L - elapsed
        return if (remainingMillis > 0) TimeUnit.MILLISECONDS.toSeconds(remainingMillis) else 0
    }

    private fun isLockoutActive(): Boolean {
        val lockoutTime = prefs.getLong("lockout_time", 0)
        if (lockoutTime == 0L) return false
        val elapsed = System.currentTimeMillis() - lockoutTime
        if (elapsed > 30000L) {
            prefs.edit().putInt("failed_attempts", 0).putLong("lockout_time", 0).apply()
            return false
        }
        return true
    }

    private fun hash(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
