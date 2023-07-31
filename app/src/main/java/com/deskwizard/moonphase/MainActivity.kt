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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.deskwizard.moonphase.ui.theme.MoonPhaseTheme
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /******************************** Notifications ********************************/

        //NotificationHelper.startNotificationHelper(this)

        /******************************** Data fetcher task ********************************/

        //NetworkAPI.startDataFetcher(this)

        val fetchRequest: WorkRequest = OneTimeWorkRequest.Builder(DataFetcherWorker::class.java)
            .setInitialDelay(60, TimeUnit.SECONDS)
            .build()

        // Schedule the WorkRequest with WorkManager
       // WorkManager.getInstance(this).enqueue(fetchRequest)

        val fetchRequest2 = OneTimeWorkRequest.Builder(DataFetcherWorker::class.java)
            .setInitialDelay(3, TimeUnit.MINUTES)
            .build()

        // Schedule the WorkRequest with WorkManager
        //WorkManager.getInstance(this).enqueue(fetchRequest2)

        /******************************** Preferences ********************************/

        MoonPreferenceProvider(this).loadAll()  // Load saved data

        /******************************** The rest ********************************/
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

@Preview
@Composable
fun DataDisplay() {
    //val unixTime = System.currentTimeMillis() / 1000

    val phase = MoonData.Phase
    val moon = MoonData.Name
    val age = MoonData.Age.roundToInt()
    val illumination = (MoonData.Illumination * 100.0).roundToInt()
    val imagePadding = 10

    /*
    Image(
        painter = painterResource(id = moonPhaseImages[MoonData.ImageIndex]),
        contentDescription = "Moon Phase Image",
        Modifier.padding(imagePadding.dp, imagePadding.dp)
    )
*/
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(), content = {
            Column(horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = moonPhaseImages[MoonData.ImageIndex]),
                    contentDescription = "Moon Phase Image",
                    Modifier.padding(imagePadding.dp, imagePadding.dp)
                )
                Text(moon, fontSize = 25.sp)
                Text(phase)
                Text("$illumination% Illumination", color = Color.Gray)
            }
        }
    )
    /*
Column() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(text = " Text 1")
        Text(text = " Text 2")
        Text(text = " Text 3")
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(text = " Text 4")
        Text(text = " Text 5")
        Text(text = " Text 6")
    }
}*/



}

/*
    Image(
        painter = painterResource(id = moonPhaseImages[MoonData.ImageIndex]),
        contentDescription = "Moon Phase Image"
        )

    Text(
        // Keep for debug
        //val date = LocalDateTime.ofInstant(Instant.ofEpochSecond(unixTime), ZoneId.systemDefault())
        //text = "Unix Time: $unixTime \n Date: ${date.toLocalDate()} \n Time: ${date.toLocalTime()} \n Moon: $moon \n Image Index: $index \n Age: $age days \n Phase: $phase \n Illumination: $illumination%",
        text = " $moon \n $phase ($age days old) \n $illumination% Illumination \n\n",
        )
    */
