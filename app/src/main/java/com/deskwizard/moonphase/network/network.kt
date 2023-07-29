package com.deskwizard.moonphase.network

import com.deskwizard.moonphase.MoonData
import com.deskwizard.moonphase.MoonPreferenceProvider
import com.deskwizard.moonphase.sharedPref
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.net.URL
import java.util.concurrent.TimeUnit


object MoonApi {

    private val format = Json { ignoreUnknownKeys = true; isLenient = true }
    var returnedMoonJSON = ""

    @Serializable
    class MoonJSON(val Moon: String, val Index: Int, val Age: Float, val Phase: String, val Illumination: Float)

    @OptIn(DelicateCoroutinesApi::class) // turn off delicate warning for GlobalScope
    fun startDataFetcher() {
        GlobalScope.launch(Dispatchers.IO) {
            dataFetcher()
        }
    }

    private fun dataFetcher() {
        val unixTime = System.currentTimeMillis() / 1000

        val url = URL("http://api.farmsense.net:80/v1/moonphases/?d=$unixTime")

        val builder = OkHttpClient.Builder()
        builder.connectTimeout(30, TimeUnit.SECONDS)
        builder.readTimeout(30, TimeUnit.SECONDS)
        builder.writeTimeout(30, TimeUnit.SECONDS)

        val client = builder.build()

        val request = okhttp3.Request.Builder()
            .url(url)
            .get()
            .build()

        // TODO: Exception on error?
        try {
            val responseBody = client.newCall(request).execute().body
            returnedMoonJSON = responseBody?.string().toString()
        } catch (e: Exception) {
            println("--------- it's dead Jim... --------")
            e.printStackTrace()
            return
        }


        val filteredCharacters = "[]"
        returnedMoonJSON = returnedMoonJSON.filterNot { filteredCharacters.indexOf(it) > -1 }
        val fetchedMoonJSON = format.decodeFromString<MoonJSON>(returnedMoonJSON)

        MoonData.Name = fetchedMoonJSON.Moon
        MoonData.Phase = fetchedMoonJSON.Phase
        MoonData.Age = fetchedMoonJSON.Age
        MoonData.Illumination = fetchedMoonJSON.Illumination
        MoonData.ImageIndex = fetchedMoonJSON.Index
        MoonData.LastUpdateTime = unixTime

        MoonPreferenceProvider (sharedPref).saveAll()

    }
}