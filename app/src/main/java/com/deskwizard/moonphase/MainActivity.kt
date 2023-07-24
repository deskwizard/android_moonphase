package com.deskwizard.moonphase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.deskwizard.moonphase.ui.theme.MoonPhaseTheme

import kotlinx.serialization.*
import kotlinx.serialization.json.*

val format = Json { ignoreUnknownKeys = true; isLenient = true }

@Serializable
class Project(val FormattedDate: String, val TargetDate: String)

@Serializable
class MoonPhaseJSON(val FormattedDate: String, val TargetDate: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoonPhaseTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {

    val fetchedJSON = format.decodeFromString<MoonPhaseJSON>("""
        {"Error":0,"ErrorMessage":"","TargetDate":1350526582,"FormattedDate":"Wed, 17 Oct 2012 22:16:22 -0400","Timezone":"","Sunrise":"07:13:28","Sunset":"18:16:38","Daylength":"11:03:10","Zenith":"12:45:03","Dawn":"06:46:02","Dusk":"18:44:04","Astrodusk":1350513844,"Hours_till_dawn":"08:30:43"}
        """)

    println(fetchedJSON.FormattedDate)

    var name = format.encodeToString(fetchedJSON.FormattedDate)

    Text(
        text = "$name",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MoonPhaseTheme {
        Greeting("Android")
    }
}