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

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkRequest
import com.deskwizard.moonphase.NetworkAPI.startImmediateDataFetch
import com.deskwizard.moonphase.ui.theme.MoonPhaseTheme
import kotlinx.coroutines.flow.MutableStateFlow
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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DataDisplay(this)
                }
            }
        }
    }
}

@Composable
fun DataDisplay(context: Context) {
    var text by remember { mutableStateOf("Click a button") }

    //val unixTime = System.currentTimeMillis() / 1000

    val phase = MoonData.Phase
    val name = MoonData.Name
    val age = MoonData.Age.roundToInt()
    val illumination = (MoonData.Illumination * 100.0).roundToInt()

    val moonCalendarImageSize = 75

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 15.dp, start = 16.dp, end = 16.dp)

        //modifier = Modifier.border(BorderStroke(5.dp, Color.Red))
    ) {

        /*********************** Main Moon Image ***********************/
        Image(
            painter = painterResource(id = moonPhaseImages[MoonData.ImageIndex]),
            contentDescription = "Moon Phase Image",
            modifier = Modifier
                .size(300.dp)
                //.border(BorderStroke(1.dp, Color.Yellow))
                .padding(25.dp),
            contentScale = ContentScale.FillBounds
        )

        /*********************** Main Moon Text ***********************/
        Row(
            modifier = Modifier
            //.border(BorderStroke(1.dp, Color.Green))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(phase, fontSize = 25.sp)
                Text(name, fontSize = 20.sp)
                Text("$age Days Old", color = Color.DarkGray)
                Text("$illumination% Illumination", color = Color.DarkGray)
            }

        }

        Spacer(modifier = Modifier.height(20.dp))

        /*********************** Moon Calendar  ***********************/
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            //modifier = Modifier.border(BorderStroke(5.dp, Color.Red))
        ) {
            Row(
                modifier = Modifier
                    //.border(BorderStroke(2.dp, Color.Blue))
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = moonPhaseImages[14]),
                        contentDescription = "Full Moon Image",
                        modifier = Modifier
                            //.border(BorderStroke(1.dp, Color.Yellow))
                            .size(moonCalendarImageSize.dp),
                        contentScale = ContentScale.FillBounds
                    )
                    Text("Aug. 1")
                }

                Divider(
                    color = Color.Gray,
                    modifier = Modifier
                        //.fillMaxHeight()  //fill the max height
                        .width(3.dp)
                        .height(100.dp)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = moonPhaseImages[21]),
                        contentDescription = "Moon Phase Image",
                        modifier = Modifier
                            //.border(BorderStroke(1.dp, Color.Yellow))
                            .size(moonCalendarImageSize.dp),
                        contentScale = ContentScale.FillBounds
                    )
                    Text("Aug. 8")

                }

                Divider(
                    color = Color.Gray,
                    modifier = Modifier
                        //.fillMaxHeight()  //fill the max height
                        .width(3.dp)
                        .height(100.dp)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = moonPhaseImages[29]),
                        contentDescription = "New Moon Image",
                        modifier = Modifier
                            //.border(BorderStroke(1.dp, Color.Yellow))
                            .size(moonCalendarImageSize.dp),
                        contentScale = ContentScale.FillBounds
                    )
                    Text("Aug. 16")
                }

                Divider(
                    color = Color.Gray,
                    modifier = Modifier
                        //.fillMaxHeight()  //fill the max height
                        .width(3.dp)
                        .height(100.dp)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = moonPhaseImages[8]),
                        contentDescription = "Moon Phase Image",
                        modifier = Modifier
                            //.border(BorderStroke(1.dp, Color.Yellow))
                            .size(moonCalendarImageSize.dp),
                        contentScale = ContentScale.FillBounds
                    )
                    Text("Aug. 24")
                }
            }   // End moon Calendar

            /*********************** Update text and button  ***********************/

        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                //.border(BorderStroke(5.dp, Color.Green))
                .fillMaxHeight()
        ) {
            Text("Last Updated: 4 days ago")
/*
            Button(onClick = { text = "Button 1 Clicked" }) {
                Text(text = text)
            }
*/
            ClickableText(
                text = AnnotatedString(text),
                onClick = {
                    println("ClickableText")
                    text = "ah AH!"
                    startImmediateDataFetch(context)
                }
            )

        }
    }    // End main column
//    var text by remember { mutableStateOf("Click a button") }

/*    Button(onClick = { text = "Button 1 Clicked" }) {
        Text(text = text)
    }*/

}

/*

//val text = mutableStateOf("text")
// or
var text = MutableStateFlow("text")

@Composable
fun MyText() {
    //val myText by text
    // or
    val myText by text.collectAsState()
    //Text(myText)
}
*/