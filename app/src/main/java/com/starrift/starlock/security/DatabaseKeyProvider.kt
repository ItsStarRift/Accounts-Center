package com.starrift.starlock.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

/**
 * Veritabanının (SQLCipher) şifreleme anahtarını (passphrase) yönetir.
 *
 * Anahtar, cihazda ilk açılışta rastgele üretilir ve Android Keystore'a bağlı
 * EncryptedSharedPreferences içinde saklanır. Bu sayede:
 *  - Kaynak kod açık olsa bile, anahtar donanım destekli Keystore'da tutulduğundan
 *    veritabanı dosyası (.db) tek başına kopyalanıp okunamaz.
 *  - Anahtar hiçbir zaman düz metin olarak koda ya da normal SharedPreferences'a yazılmaz.
 */
object DatabaseKeyProvider {

    private const val PREFS_NAME = "secure_db_prefs"
    private const val KEY_ALIAS = "db_passphrase"

    fun getOrCreatePassphrase(context: Context): CharArray {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val existing = prefs.getString(KEY_ALIAS, null)
        if (existing != null) {
            return existing.toCharArray()
        }

        val newPassphrase = generateRandomPassphrase()
        prefs.edit().putString(KEY_ALIAS, String(newPassphrase)).apply()
        return newPassphrase
    }

    private fun generateRandomPassphrase(): CharArray {
        val allowedChars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val random = SecureRandom()
        return CharArray(64) { allowedChars[random.nextInt(allowedChars.size)] }
    }
}
