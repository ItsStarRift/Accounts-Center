package com.starrift.starlock

import android.app.Application
import android.os.Environment
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StarLockApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                val text = "=== StarLock Crash [$timestamp] ===\n$sw\n"
                val dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    ?: File(Environment.getExternalStorageDirectory(), "Download")
                dir.mkdirs()
                val file = File(dir, "StarLock_crash.txt")
                file.appendText(text)
            } catch (e: Exception) {
                // yazma basarisiz olursa sessizce gec
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
