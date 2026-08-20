package com.starrift.starlock.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * Girilen uygulama/oyun ismine göre Clearbit Logo API üzerinden otomatik ikon arar.
 * Clearbit bir "alan adı" bekler, biz isimden basit bir alan adı tahmini üretiyoruz
 * (örn. "WhatsApp" -> "whatsapp.com"). Bulunamazsa null döner ve arayüz kullanıcıyı
 * galeri/kameraya yönlendirir.
 */
object IconFetcher {

    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /** Ağ üzerinden ikonu dener, bulursa bir önizleme Bitmap'i döner (henüz kaydetmez). */
    suspend fun tryFetchPreview(appName: String): Bitmap? = withContext(Dispatchers.IO) {
        val guessedDomain = guessDomain(appName) ?: return@withContext null
        return@withContext try {
            val url = URL("https://logo.clearbit.com/$guessedDomain?size=256")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.requestMethod = "GET"

            if (connection.responseCode == 200) {
                BitmapFactory.decodeStream(connection.inputStream)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /** Onaylanan bitmap'i uygulamanın dahili (özel) depolama alanına kaydeder ve yolunu döner. */
    suspend fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): String =
        withContext(Dispatchers.IO) {
            val iconsDir = File(context.filesDir, "icons").apply { mkdirs() }
            val file = File(iconsDir, "${UUID.randomUUID()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file.absolutePath
        }

    private fun guessDomain(appName: String): String? {
        val cleaned = appName.trim().lowercase()
            .replace(Regex("[^a-z0-9 ]"), "")
            .replace(" ", "")
        if (cleaned.isBlank()) return null
        return "$cleaned.com"
    }
}
