package com.example.tugasakhir

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.Worker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "AnxietyNotificationChannel"
        const val NOTIFICATION_ID = 1
    }

    override suspend fun doWork(): Result {
        // Tambahkan log di awal doWork untuk memastikan worker dipanggil
        Log.d("NotificationWorker", "NotificationWorker started.")

        // Ambil nilai latestAnxiety dari SharedPreferences
        val latestAnxiety = getLatestAnxiety()
        Log.d("NotificationWorker", "Latest Anxiety Level: $latestAnxiety")  // Log nilai anxiety

        // Cek apakah nilai latestAnxiety lebih besar dari 0.5
        if (latestAnxiety > 0.5) {
            Log.d("NotificationWorker", "Anxiety level is greater than 0.5, showing notification.")
            showNotification()  // Menampilkan notifikasi jika nilai > 0.5
            Log.d("NotificationWorker", "Notification Showed Successfully")
        } else {
            Log.d("NotificationWorker", "Anxiety level is below or equal to 0.5, no notification shown.")
        }

        return Result.success()
    }

    private fun getLatestAnxiety(): Float {
        // Mengambil nilai latestAnxiety dari SharedPreferences
        val sharedPreferences = applicationContext.getSharedPreferences("anxiety_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getFloat("latestAnxiety", 0f)  // Jika tidak ada, kembalikan 0f
    }

    private fun showNotification() {
        // Buat channel notifikasi jika belum ada
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Anxiety Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Buat notifikasi
        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Anxiety Level Alert")
            .setContentText("Your latest anxiety level is high. Please take action by doing breathing exercise.")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // Tampilkan notifikasi
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
