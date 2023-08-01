package com.deskwizard.moonphase

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.net.URL
import java.util.concurrent.TimeUnit

// TODO: Make the returned value actually do something useful...
class DataFetcherWorker(private val context: Context, params: WorkerParameters) :
    Worker(context, params) {

    @SuppressLint("MissingPermission")   // TODO: fix the code so it works without
    override fun doWork(): Result {

        // Perform the background task here
        NetworkAPI.moonDataFetcher(context)
        return Result.success()
    }
}

object NetworkAPI {

    private val format = Json { ignoreUnknownKeys = true; isLenient = true }

    @Serializable
    class MoonJSON(
        val Moon: String,
        val Index: Int,
        val Age: Float,
        val Phase: String,
        val Illumination: Float
    )

    fun startImmediateDataFetch(context: Context) {
        println("Immediate fetch requested")
        val fetchRequest: WorkRequest = OneTimeWorkRequest.Builder(DataFetcherWorker::class.java)
            .build()

        // Schedule the WorkRequest with WorkManager
        WorkManager.getInstance(context).enqueue(fetchRequest)
    }

    // TODO: Always fails, but one time task works with the same worker ??
    fun startDataFetcher(context: Context) {
        val dataFetcherWorkRequest: PeriodicWorkRequest =
            PeriodicWorkRequest.Builder(DataFetcherWorker::class.java, 15L, TimeUnit.MINUTES)
                .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "dataFetcherWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            dataFetcherWorkRequest
        )
    }

    fun moonDataFetcher(context: Context) {

        val unixTime = System.currentTimeMillis() / 1000

        var returnedMoonJSON: String
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

        try {
            val responseBody = client.newCall(request).execute().body
            returnedMoonJSON = responseBody?.string().toString()
        } catch (e: Exception) {
            println("--------- Network Exception --------")
            e.printStackTrace()
            return
        }

        // If we get here, we have valid JSON
        val filteredCharacters = "[]"
        returnedMoonJSON = returnedMoonJSON.filterNot { filteredCharacters.indexOf(it) > -1 }
        val fetchedMoonJSON = format.decodeFromString<MoonJSON>(returnedMoonJSON)

        MoonData.Name = fetchedMoonJSON.Moon
        MoonData.Phase = fetchedMoonJSON.Phase
        MoonData.Age = fetchedMoonJSON.Age
        MoonData.Illumination = fetchedMoonJSON.Illumination
        MoonData.ImageIndex = fetchedMoonJSON.Index
        MoonData.LastUpdateTime = unixTime

        MoonPreferenceProvider(context).saveAll()

    }
}