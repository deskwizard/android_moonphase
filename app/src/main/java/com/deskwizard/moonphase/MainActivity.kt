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
import androidx.activity.viewModels
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.deskwizard.moonphase.ui.theme.MoonPhaseTheme
import kotlin.math.roundToInt


class MoonPhaseViewModel : ViewModel() {
    var moonInfo by mutableStateOf<MoonData>(MoonData())
        private set

    fun setMoonData(newMoonData: MoonData) {
        moonInfo = newMoonData
    }
}

class MainActivity : ComponentActivity() {

    val viewModel: MoonPhaseViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        setContent {
            MoonPhaseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    //NetworkAPI.startImmediateDataFetch(viewModel, this)
                    DataDisplay(viewModel, this)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /******************************** Notifications ********************************/

        //NotificationHelper.startNotificationHelper(this) // WIP is an understatement...

        /******************************** Data fetcher task ********************************/

        NetworkAPI.startDataFetcher(this)
        //NetworkAPI.startImmediateDataFetch(this)


        /******************************** Preferences ********************************/

        viewModel.setMoonData(MoonPreferenceProvider(this).loadAll())

        /******************************** The rest ********************************/
        /*        setContent {
                    MoonPhaseTheme {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            DataDisplay(viewModel,this)
                        }
                    }
                }*/
    }
}

@Composable
fun DataDisplay(viewModel: MoonPhaseViewModel, context: Context) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 15.dp, start = 16.dp, end = 16.dp)

        //modifier = Modifier.border(BorderStroke(5.dp, Color.Red))
    ) {

        DisplayMoonImage(viewModel)
        DisplayMoonInfo(viewModel)
        Spacer(modifier = Modifier.height(20.dp))
        DisplayMoonCalendar()
        DisplayUpdateClickClick(viewModel, context)
    }    // End main column

}

@Composable
fun DisplayMoonImage(viewModel: MoonPhaseViewModel) {
    if (viewModel.moonInfo != null) {
        Image(
            painter = painterResource(id = moonPhaseImages[viewModel.moonInfo.ImageIndex]),
            contentDescription = "Moon Phase Image",
            modifier = Modifier
                .size(300.dp)
                //.border(BorderStroke(1.dp, Color.Yellow))
                .padding(25.dp),
            contentScale = ContentScale.FillBounds
        )
    }
}

@Composable
fun DisplayMoonInfo(viewModel: MoonPhaseViewModel) {
    Row(
        modifier = Modifier
        //.border(BorderStroke(1.dp, Color.Green))
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(viewModel.moonInfo.Phase, fontSize = 25.sp)
            Text(viewModel.moonInfo.Name, fontSize = 20.sp)
            Text("${viewModel.moonInfo.Age.roundToInt()} Days Old", color = Color.DarkGray)
            Text(
                "${(viewModel.moonInfo.Illumination * 100.0).roundToInt()}% Illumination",
                color = Color.DarkGray
            )
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

@Composable
fun DisplayUpdateClickClick(viewModel: MoonPhaseViewModel, context: Context) {

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
                    NetworkAPI.startImmediateDataFetch(viewModel, context)
                },
            text = updateClick(viewModel, context)
        )

        ClickableText(
            text = AnnotatedString("(Click to update manually)"),
            onClick = {
                println("ClickableText2")
                NetworkAPI.startImmediateDataFetch(viewModel, context)
            }
        )

    }
}

fun updateClick(viewModel: MoonPhaseViewModel, context: Context): String {
    var lastUpdateUnitText = "Second(s)"
    var lastUpdateValue: Int

    if (viewModel.moonInfo.LastUpdateTime == 0L) {
        println("---------------- Zero!  ------------")
        return "Last updated: Never"
    }

    // Get delta:
    val lastUpdateDelta = (System.currentTimeMillis() / 1000) - viewModel.moonInfo.LastUpdateTime
    println("------ delta: $lastUpdateDelta")

    if (lastUpdateDelta > 86400L) {
        lastUpdateValue = (lastUpdateDelta / 86400L).toInt()
        lastUpdateUnitText = "Day(s)"
    } else if (lastUpdateDelta > 3600L) {
        lastUpdateValue = (lastUpdateDelta / 3600L).toInt()
        lastUpdateUnitText = "Hour(s)"
    } else if (lastUpdateDelta > 60L) {
        lastUpdateValue = (lastUpdateDelta / 60).toInt()
        lastUpdateUnitText = "Minute(s)"
    } else { // Seconds
        lastUpdateValue = lastUpdateDelta.toInt()
    }

    return "Last Updated: $lastUpdateValue $lastUpdateUnitText ago"
}
