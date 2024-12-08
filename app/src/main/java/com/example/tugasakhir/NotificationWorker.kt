package com.example.tugasakhir

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
        // Create a NotificationManager to manage the notifications
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel (only required for Android Oreo and above)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Anxiety Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Create an Intent to open BreathingExercise activity when notification is clicked
        val intent = Intent(applicationContext, BreathingExercise::class.java)
        // You can pass additional data to the activity if needed, e.g., intent.putExtra("key", "value")

        // Create a PendingIntent to wrap the Intent
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Anxiety Level Alert")
            .setContentText("Your anxiety level is high. Please do breathing exercise.")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)  // Set the PendingIntent that opens BreathingExercise
            .setAutoCancel(true)  // Automatically remove the notification when clicked
            .build()

        // Display the notification
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

}
