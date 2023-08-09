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

var testing: Boolean = false

class PeriodicDataFetcherWorker(private val context: Context, params: WorkerParameters) :
    Worker(context, params) {

    @SuppressLint("MissingPermission")   // TODO: fix the code so it works without
    override fun doWork(): Result {

        println("---------- Periodic Fetch worker run $testing -----------")

        // Perform the background task here
        val moonData = NetworkAPI.moonDataFetcher()

        if (moonData != null) {
            println("-------------- Worker success, data: $moonData")

/*
            val fetchedMoonJSON = NetworkAPI.format.decodeFromString<NetworkAPI.MoonJSON>(moonData)

            MoonData().Name = fetchedMoonJSON.Moon
            MoonData().Phase = fetchedMoonJSON.Phase
            MoonData().Age = fetchedMoonJSON.Age
            MoonData().Illumination = fetchedMoonJSON.Illumination
            MoonData().ImageIndex = fetchedMoonJSON.Index
            MoonData().LastUpdateTime = System.currentTimeMillis() / 1000

            MoonPreferenceProvider(context).saveAll(MoonData())
*/

            //viewModel.setMoonData(MoonData())
            testing = true
            return Result.success()
        }

        testing = false
        return Result.failure()
    }
}

class ImmediateDataFetcherWorker(private val context: Context, params: WorkerParameters) :
    Worker(context, params) {

    @SuppressLint("MissingPermission")   // TODO: fix the code so it works without
    override fun doWork(): Result {

        println("---------- Fetch worker run -----------------")

        // Perform the background task here
        val moonData = NetworkAPI.moonDataFetcher()

        if (moonData != null) {
            val dataFetcherSuccessData = Data.Builder().putString("json", moonData).build()
            println("-------------- Worker success, data: $dataFetcherSuccessData")
            return Result.success(dataFetcherSuccessData)
        }

        return Result.failure()
    }
}

object NetworkAPI {

    val format = Json { ignoreUnknownKeys = true; isLenient = true }

    @Serializable
    class MoonJSON(
        val Moon: String,
        val Index: Int,
        val Age: Float,
        val Phase: String,
        val Illumination: Float
    )


    fun startDataFetcher(context: Context) {

        println(" +++++++++ Periodic start +++++++++")

        val dataFetcherWorkRequest: PeriodicWorkRequest =
            PeriodicWorkRequest.Builder(PeriodicDataFetcherWorker::class.java, 15L, TimeUnit.MINUTES)
                .build()


        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "dataFetcherWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            dataFetcherWorkRequest
        )
    }


    fun startImmediateDataFetch(viewModel: MoonPhaseViewModel, context: Context) {
        println("Immediate fetch requested")
        val fetchRequest: WorkRequest = OneTimeWorkRequest.Builder(ImmediateDataFetcherWorker::class.java)
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
                        val moonSuccess = MoonData()
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

    fun moonDataFetcher(): String? {
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

        // -------------- If we get here, we have valid JSON --------------
        val filteredCharacters = "[]"
        returnedMoonJSON = returnedMoonJSON.filterNot { filteredCharacters.indexOf(it) > -1 }
        println("Return from moonDataFetcher: $returnedMoonJSON")





        return returnedMoonJSON
    }
}
