package com.deskwizard.moonphase

import android.annotation.SuppressLint
import android.content.SharedPreferences

@SuppressLint("ApplySharedPref")
class MoonPreferenceProvider (private val sharedPref : SharedPreferences) {

    private fun save (key: String, value: String) {
        with (sharedPref.edit()) {
            putString(key, value)
            commit()
            println("---------- Saving key $key: $value ----------")
        }
    }
    private fun save (key: String, value: Float) {
        with (sharedPref.edit()) {
            putFloat(key, value)
            commit()
            println("---------- Saving key $key: $value ----------")
        }
    }
    private fun save (key: String, value: Int) {
        with (sharedPref.edit()) {
            putInt(key, value)
            commit()
            println("---------- Saving key $key: $value ----------")
        }
    }
    private fun save (key: String, value: Long) {
        with (sharedPref.edit()) {
            putLong(key, value)
            commit()
            println("---------- Saving key $key: $value ----------")
        }
    }

    private fun loadString (key: String): String? {
        val value = sharedPref.getString(key, "No Data")
        println("++++++++++ Loading key $key: $value +++++++++++")
        return value
    }

    private fun loadFloat (key: String): Float {
        val value = sharedPref.getFloat(key, 6.85F)
        println("++++++++++ Loading key $key: $value +++++++++++")
        return value
    }

    private fun loadInt (key: String): Int {
        val value = sharedPref.getInt(key, 7)
        println("++++++++++ Loading key $key: $value +++++++++++")
        return value
    }
    private fun loadLong (key: String): Long {
        val value = sharedPref.getLong(key, 7)
        println("++++++++++ Loading key $key: $value +++++++++++")
        return value
    }

    fun saveAll() {
        save("Name", MoonData.Name)
        save("Phase", MoonData.Phase)
        save("Age", MoonData.Age)
        save("Illumination", MoonData.Illumination)
        save("ImageIndex", MoonData.ImageIndex)
        save("LastUpdateTime", MoonData.LastUpdateTime)
    }

    fun loadAll() {
        MoonData.Name = loadString("Name").toString()
        MoonData.Phase = loadString("Phase").toString()
        MoonData.Age = loadFloat("Age")
        MoonData.Illumination = loadFloat("Illumination")
        MoonData.ImageIndex = loadInt("ImageIndex")
        MoonData.LastUpdateTime = loadLong("LastUpdateTime")
    }
}