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
            - Add day info JSON?

 */


package com.deskwizard.moonphase

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.deskwizard.moonphase.ui.theme.MoonPhaseTheme
import kotlin.math.roundToInt


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /******************************** Notifications ********************************/

        //NotificationHelper.startNotificationHelper(this) // WIP is an understatement...

        /******************************** Data fetcher task ********************************/

        //NetworkAPI.startDataFetcher(this)
        //NetworkAPI.startImmediateDataFetch(this)


        /******************************** Preferences ********************************/

        MoonPreferenceProvider(this).loadAll()  // Load saved data

        /******************************** The rest ********************************/
        /*        setContent {
                    MoonPhaseTheme {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            DataDisplay(this)
                        }
                    }
                }*/
    }

    override fun onResume() {
        super.onResume()
        setContent {
            MoonPhaseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    println("********** resume ***********")
                    MoonPreferenceProvider(this).loadAll()  // Load saved data
                    DataDisplay(this)
                }
            }
        }


    }
}


@Composable
fun DataDisplay(context: Context) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 15.dp, start = 16.dp, end = 16.dp)

        //modifier = Modifier.border(BorderStroke(5.dp, Color.Red))
    ) {

        DisplayMoonImage()
        DisplayMoonInfo()
        Spacer(modifier = Modifier.height(20.dp))
        DisplayMoonCalendar()
        DisplayUpdateClicketyClick(context)
    }    // End main column

}


@Composable
fun DisplayUpdateClicketyClick(context: Context) {
    var text by remember { mutableStateOf("Last updated: Never") }
    //MoonPreferenceProvider(context).loadAll()  // Load saved data

    println("-------------- clickety")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier
            //.border(BorderStroke(5.dp, Color.Green))
            .fillMaxHeight()
    ) {

        Text(
            modifier = Modifier
                .clickable(enabled = true) {
                    println("ClickableText1")
                    NetworkAPI.startImmediateDataFetch(context)
                    text = updateClick(context)
                },
            text = updateClick(context)
            //text = text
        )

        ClickableText(
            text = AnnotatedString("(Click to update)"),
            onClick = {
                println("ClickableText2")
            }
        )

    }
}

fun updateClick(context: Context): String {

    //NetworkAPI.startImmediateDataFetch(context)

    println("update click")
    var lastUpdateUnitText = "Seconds"
    var lastUpdateValue = MoonData.LastUpdateTime

    if (MoonData.LastUpdateTime == 0L) {
        println("------------- oh zero!")
        return "Last updated: Never"
    }

    // Get delta:
    val lastUpdateDelta = (System.currentTimeMillis() / 1000) - MoonData.LastUpdateTime
    println("------ delta: $lastUpdateDelta")

    if (lastUpdateDelta > 86400L) {
        // days
        lastUpdateValue = (lastUpdateDelta / 86400L)
        lastUpdateUnitText = "day(s)"
    } else if (lastUpdateDelta > 3600L) {
        // hours
        lastUpdateValue = (lastUpdateDelta / 3600L)
        lastUpdateUnitText = "Hour(s)"
    } else if (lastUpdateDelta > 60L) {
        // minutes
        lastUpdateValue = (lastUpdateDelta / 60)
        lastUpdateUnitText = "Minute(s)"
    } else {
        lastUpdateValue = lastUpdateDelta
        lastUpdateUnitText = "Seconds"
    }

    return "Last Updated: $lastUpdateValue $lastUpdateUnitText ago"
}

@Composable
fun DisplayMoonImage() {
    Image(
        painter = painterResource(id = moonPhaseImages[MoonData.ImageIndex]),
        contentDescription = "Moon Phase Image",
        modifier = Modifier
            .size(300.dp)
            //.border(BorderStroke(1.dp, Color.Yellow))
            .padding(25.dp),
        contentScale = ContentScale.FillBounds
    )
}

@Composable
fun DisplayMoonInfo() {
    Row(
        modifier = Modifier
        //.border(BorderStroke(1.dp, Color.Green))
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(MoonData.Phase, fontSize = 25.sp)
            Text(MoonData.Name, fontSize = 20.sp)
            Text("${MoonData.Age.roundToInt()} Days Old", color = Color.DarkGray)
            Text("${MoonData.Illumination.roundToInt()}% Illumination", color = Color.DarkGray)
        }

    }
}

@Composable
fun DisplayMoonCalendar() {
    val moonCalendarImageSize = 75

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
        }
    }   // End moon Calendar

}
