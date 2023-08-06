package com.deskwizard.moonphase

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.work.impl.utils.PREFERENCE_FILE_KEY

@SuppressLint("ApplySharedPref")
class MoonPreferenceProvider(context: Context) {

    private val sharedPref: SharedPreferences =
        context.getSharedPreferences(PREFERENCE_FILE_KEY, MODE_PRIVATE)


    private fun save(key: String, value: String) {
        with(sharedPref.edit()) {
            putString(key, value)
            commit()
            println("---------- Saving key $key: $value ----------")
        }
    }

    private fun save(key: String, value: Float) {
        with(sharedPref.edit()) {
            putFloat(key, value)
            commit()
            println("---------- Saving key $key: $value ----------")
        }
    }

    private fun save(key: String, value: Int) {
        with(sharedPref.edit()) {
            putInt(key, value)
            commit()
            println("---------- Saving key $key: $value ----------")
        }
    }

    private fun save(key: String, value: Long) {
        with(sharedPref.edit()) {
            putLong(key, value)
            commit()
            println("---------- Saving key $key: $value ----------")
        }
    }

    fun saveAll(MoonDataObject:MoonData) {
        save("Name", MoonDataObject.Name)
        save("Phase", MoonDataObject.Phase)
        save("Age", MoonDataObject.Age)
        save("Illumination", MoonDataObject.Illumination)
        save("ImageIndex", MoonDataObject.ImageIndex)
        save("LastUpdateTime", MoonDataObject.LastUpdateTime)
        println("---------- Saved all values ----------")
    }

    private fun loadString(key: String): String? {
        val value = sharedPref.getString(key, "No Data")
        println("++++++++++ Loading key $key: $value +++++++++++")
        return value
    }

    private fun loadFloat(key: String): Float {
        val value = sharedPref.getFloat(key, 0.0F)
        println("++++++++++ Loading key $key: $value +++++++++++")
        return value
    }

    private fun loadInt(key: String): Int {
        val value = sharedPref.getInt(key, 0)
        println("++++++++++ Loading key $key: $value +++++++++++")
        return value
    }

    private fun loadLong(key: String): Long {
        val value = sharedPref.getLong(key, 0)
        println("++++++++++ Loading key $key: $value +++++++++++")
        return value
    }

    fun loadAll() : MoonData {
        val moonDataObject = MoonData()

        moonDataObject.Name = loadString("Name").toString()
        moonDataObject.Phase = loadString("Phase").toString()
        moonDataObject.Age = loadFloat("Age")
        moonDataObject.Illumination = loadFloat("Illumination")
        moonDataObject.ImageIndex = loadInt("ImageIndex")
        moonDataObject.LastUpdateTime = loadLong("LastUpdateTime")
        println("---------- Loaded all values ----------")

        return moonDataObject
    }
}