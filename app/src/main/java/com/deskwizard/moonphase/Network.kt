package com.deskwizard.moonphase

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.Observer
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
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
        val moon_data = NetworkAPI.moonDataFetcher(context)

        if (moon_data != null) {
            val data_fetcher_success_data = Data.Builder().putString("json", moon_data).build()
            return Result.success(data_fetcher_success_data)
        }

        return Result.failure()
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

    fun startImmediateDataFetch(viewModel: MoonPhaseViewModel, context: Context) {
        println("Immediate fetch requested")
        val fetchRequest: WorkRequest = OneTimeWorkRequest.Builder(DataFetcherWorker::class.java)
            .build()

        // https://developer.android.com/guide/background/persistent/how-to/observe
        WorkManager.getInstance(context).getWorkInfoByIdLiveData(fetchRequest.id).observe(
            ProcessLifecycleOwner.get(),
            Observer {
                if (it.state == WorkInfo.State.SUCCEEDED) {
                    val returnedMoonJSON = it.outputData.getString("json")
                    if (returnedMoonJSON != null) {
                        val fetchedMoonJSON = format.decodeFromString<MoonJSON>(returnedMoonJSON)

                        val unixTime = System.currentTimeMillis() / 1000
                        var moonSuccess = MoonData()
                        moonSuccess.Name = fetchedMoonJSON.Moon
                        moonSuccess.Phase = fetchedMoonJSON.Phase
                        moonSuccess.Age = fetchedMoonJSON.Age
                        moonSuccess.Illumination = fetchedMoonJSON.Illumination
                        moonSuccess.ImageIndex = fetchedMoonJSON.Index
                        moonSuccess.LastUpdateTime = unixTime

                        MoonPreferenceProvider(context).saveAll(moonSuccess)

                        viewModel.setMoonData(moonSuccess)
                    }
                }
            }
        )

        // Schedule the WorkRequest with WorkManager
        WorkManager.getInstance(context).enqueue(fetchRequest)
    }

    // TODO: Always fails, but one time task works with the same worker ??
    fun startDataFetcher(context: Context) {
        val dataFetcherWorkRequest: PeriodicWorkRequest =
            PeriodicWorkRequest.Builder(DataFetcherWorker::class.java, 60L, TimeUnit.MINUTES)
                .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "dataFetcherWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            dataFetcherWorkRequest
        )
    }

    fun moonDataFetcher(context: Context) :String? {
        val unixTime = System.currentTimeMillis() / 1000

        var returnedMoonJSON: String
        val url = URL("https://api.farmsense.net/v1/moonphases/?d=$unixTime")
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
            return null
        }

        // If we get here, we have valid JSON
        val filteredCharacters = "[]"
        returnedMoonJSON = returnedMoonJSON.filterNot { filteredCharacters.indexOf(it) > -1 }
        //val fetchedMoonJSON = format.decodeFromString<MoonJSON>(returnedMoonJSON)

        return returnedMoonJSON
    }
}
