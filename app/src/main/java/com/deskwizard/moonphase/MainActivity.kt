/*
    Remove character(s) from string using filter: https://stackoverflow.com/a/58569614
    Moon phase images from https://phasesmoon.com/
    Array of drawables: https://stackoverflow.com/a/5760783/7936786
    https://stackoverflow.com/questions/69545093/removing-the-green-background-for-android-icon
    https://stackoverflow.com/questions/40170666/java-net-sockettimeoutexception-in-okhttp
    https://medium.com/@sumon.v0.0/android-jetpack-workmanager-onetime-and-periodic-work-request-94ace224ff7d

    FIXME: Image index is 0-30 but we have 30 images, it's gonna crash out-of-bounds
          eventually... Fix that before it happens

    TODO:   - Moon phase indicator
            - Moon phase home screen widget
            - fetch/find/display next moon events (full, half, new, other half...)
            - App settings?
            - Add day info JSON
            -
 */


package com.deskwizard.moonphase

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.impl.utils.PREFERENCE_FILE_KEY
import com.deskwizard.moonphase.ui.theme.MoonPhaseTheme
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

var imgArray = arrayOf(
    R.drawable.moon_phase_0,
    R.drawable.moon_phase_1,
    R.drawable.moon_phase_2,
    R.drawable.moon_phase_3,
    R.drawable.moon_phase_4,
    R.drawable.moon_phase_5,
    R.drawable.moon_phase_6,
    R.drawable.moon_phase_7,
    R.drawable.moon_phase_8,
    R.drawable.moon_phase_9,
    R.drawable.moon_phase_10,
    R.drawable.moon_phase_11,
    R.drawable.moon_phase_12,
    R.drawable.moon_phase_13,
    R.drawable.moon_phase_14,
    R.drawable.moon_phase_15,
    R.drawable.moon_phase_16,
    R.drawable.moon_phase_17,
    R.drawable.moon_phase_18,
    R.drawable.moon_phase_19,
    R.drawable.moon_phase_20,
    R.drawable.moon_phase_21,
    R.drawable.moon_phase_22,
    R.drawable.moon_phase_23,
    R.drawable.moon_phase_24,
    R.drawable.moon_phase_25,
    R.drawable.moon_phase_26,
    R.drawable.moon_phase_27,
    R.drawable.moon_phase_28,
    R.drawable.moon_phase_29,
)
lateinit var sharedPref : SharedPreferences // Needs to be late init


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        /******************************** Notifications ********************************/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // Required for API 26 and up (8.0)
                val channel =
                    NotificationChannel("default", "Default", NotificationManager.IMPORTANCE_DEFAULT)
                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager.createNotificationChannel(channel)
        }

        val notificationWorkRequest: WorkRequest = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()

        // Schedule the WorkRequest with WorkManager
        WorkManager.getInstance(this).enqueue(notificationWorkRequest)


        /******************************** Data fetcher task ********************************/

        val dataFetcherWorkRequest: PeriodicWorkRequest = PeriodicWorkRequest.Builder(DataFetcherWorker::class.java,15L, TimeUnit.MINUTES)
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("dataFetcherWorker", ExistingPeriodicWorkPolicy.KEEP, dataFetcherWorkRequest)


        /********************************  Preferences ********************************/

        sharedPref  = applicationContext.getSharedPreferences(PREFERENCE_FILE_KEY, MODE_PRIVATE)
        MoonPreferenceProvider (sharedPref).loadAll()  // Load saved data

        setContent {
            MoonPhaseTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DataDisplay()
                }
            }
        }
    }
}


@Composable
fun DataDisplay() {
    //val unixTime = System.currentTimeMillis() / 1000

    val phase = MoonData.Phase
    val moon = MoonData.Name
    val age = MoonData.Age.roundToInt()
    val illumination = (MoonData.Illumination * 100.0).roundToInt()

    Image(
        painter = painterResource(id = imgArray[MoonData.ImageIndex]),
        contentDescription = "Moon Phase Image"
    )

    Text(
        // Keep for debug
        //val date = LocalDateTime.ofInstant(Instant.ofEpochSecond(unixTime), ZoneId.systemDefault())
        //text = "Unix Time: $unixTime \n Date: ${date.toLocalDate()} \n Time: ${date.toLocalTime()} \n Moon: $moon \n Image Index: $index \n Age: $age days \n Phase: $phase \n Illumination: $illumination%",
        text = " $moon \n $phase ($age days old) \n $illumination% Illumination \n\n",
        )
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
