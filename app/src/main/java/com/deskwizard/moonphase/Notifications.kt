// Just a basic test of notifications

package com.deskwizard.moonphase

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

object NotificationHelper {

    fun startNotificationHelper(context: Context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // Required for API 26 and up (8.0)
            val channel =
                NotificationChannel("default", "Default", NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager = getSystemService(context, NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }

        val notificationWorkRequest: WorkRequest =
            OneTimeWorkRequest.Builder(NotificationWorker::class.java)
                .setInitialDelay(30, TimeUnit.SECONDS)
                .build()

        // Schedule the WorkRequest with WorkManager
        WorkManager.getInstance(context).enqueue(notificationWorkRequest)
    }
}

class NotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    @SuppressLint("MissingPermission")   // TODO: fix the code so it works without
    override fun doWork(): Result {
        val notification = NotificationCompat.Builder(applicationContext, "default")
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle("Task completed")
            .setContentText("The background task has completed successfully.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // Perform the background task here
        NotificationManagerCompat.from(applicationContext).notify(1, notification)
        return Result.success()
    }
}