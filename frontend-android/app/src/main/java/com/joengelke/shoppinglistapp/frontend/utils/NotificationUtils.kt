package com.joengelke.shoppinglistapp.frontend.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.joengelke.shoppinglistapp.frontend.R
import java.io.File

object NotificationUtils {

    fun showDownloadNotification(context: Context, file: File) {
        val channelId = "recipe_pdf_channel"

        // 1️⃣ Create notification channel (for Android 8+)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            channelId,
            "Recipe PDF Downloads",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        // 2️⃣ Create intent to open PDF
        val pdfUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val openIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(pdfUri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3️⃣ Build and show notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.baseline_download_24)
            .setContentTitle("Recipe PDF downloaded")
            .setContentText(file.name)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // heads-up for pre-Oreo
            .setDefaults(NotificationCompat.DEFAULT_ALL) // sound, vibration
            .build()

        notificationManager.notify(file.hashCode(), notification)
    }
}