package com.deskwizard.moonphase

import kotlinx.serialization.Serializable

@Serializable
class MoonData {
    var Name: String = ""
    var Phase: String = ""
    var Age: Float = 0.0F
    var Illumination: Float = 0.0F
    var ImageIndex = 0
    var LastUpdateTime: Long = 0L
}


var moonPhaseImages = arrayOf(
    R.drawable.moon_phase_0,
    R.drawable.moon_phase_1,
    R.drawable.moon_phase_2,
    R.drawable.moon_phase_3,
    R.drawable.moon_phase_4,
    R.drawable.moon_phase_5,
    R.drawable.moon_phase_6,
    R.drawable.moon_phase_7,
    R.drawable.moon_phase_8,
    R.drawable.moon_phase_9,
    R.drawable.moon_phase_10,
    R.drawable.moon_phase_11,
    R.drawable.moon_phase_12,
    R.drawable.moon_phase_13,
    R.drawable.moon_phase_14,
    R.drawable.moon_phase_15,
    R.drawable.moon_phase_16,
    R.drawable.moon_phase_17,
    R.drawable.moon_phase_18,
    R.drawable.moon_phase_19,
    R.drawable.moon_phase_20,
    R.drawable.moon_phase_21,
    R.drawable.moon_phase_22,
    R.drawable.moon_phase_23,
    R.drawable.moon_phase_24,
    R.drawable.moon_phase_25,
    R.drawable.moon_phase_26,
    R.drawable.moon_phase_27,
    R.drawable.moon_phase_28,
    R.drawable.moon_phase_29,
)
