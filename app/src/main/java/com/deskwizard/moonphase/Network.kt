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

class DataFetcherWorker(private val context: Context, params: WorkerParameters) :
    Worker(context, params) {

    @SuppressLint("MissingPermission")   // TODO: fix the code so it works without
    override fun doWork(): Result {

        println("---------- Fetch worker run -----------------")

        // Perform the background task here
        val moonData = NetworkAPI.moonDataFetcher(context)

        if (moonData != null) {
            val dataFetcherSuccessData = Data.Builder().putString("json", moonData).build()
            println("-------------- Worker success, data: $dataFetcherSuccessData")
            return Result.success(dataFetcherSuccessData)
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
    /*

        fun startDataFetcher2(viewModel: MoonPhaseViewModel, context: Context) {

            println(" +++++++++ Periodic start +++++++++")

            val dataFetcherWorkRequest: PeriodicWorkRequest =
                PeriodicWorkRequest.Builder(DataFetcherTest::class.java, 15L, TimeUnit.MINUTES)
                    .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "dataFetcherWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                dataFetcherWorkRequest
            )
        }
    */

    fun startDataFetcher(viewModel: MoonPhaseViewModel, context: Context) {
        println("Start fetcher")

        val fetchRequest: PeriodicWorkRequest =
            PeriodicWorkRequest.Builder(DataFetcherWorker::class.java, 15L, TimeUnit.MINUTES)
                .build()

        // https://developer.android.com/guide/background/persistent/how-to/observe
        WorkManager.getInstance(context).getWorkInfoByIdLiveData(fetchRequest.id).observe(
            ProcessLifecycleOwner.get(),
            Observer {
                print("++++++++++++++++ Returned worker data: ")
                val returnedMoonJSON = it.outputData.getString("json")
                println(returnedMoonJSON)

                if (returnedMoonJSON != null) {
                    println("not null")
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
                } else {
                    println("null???")
                }
            }
        )

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "dataFetcherWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            fetchRequest
        )
    }

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

    fun moonDataFetcher(context: Context): String? {
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
            println("--------- Network Reply OK --------")

        } catch (e: Exception) {
            println("--------- Network Exception --------")
            e.printStackTrace()
            return null
        }

        // If we get here, we have valid JSON
        val filteredCharacters = "[]"
        returnedMoonJSON = returnedMoonJSON.filterNot { filteredCharacters.indexOf(it) > -1 }
        println("Returned from moonDataFetcher: $returnedMoonJSON")
        return returnedMoonJSON
    }
}
