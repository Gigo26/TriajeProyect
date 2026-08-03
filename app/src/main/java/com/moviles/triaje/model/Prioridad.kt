package com.moviles.triaje.model

import com.moviles.triaje.R

enum class Prioridad(val nombre: String, val colorResId: Int, val iconResId: Int, val stringResId: Int) {
    ROJA("ROJA", android.R.color.holo_red_dark, R.drawable.ic_result_roja, R.string.priority_red),
    NARANJA("NARANJA", android.R.color.holo_orange_dark, R.drawable.ic_result_naranja, R.string.priority_orange),
    AMARILLO("AMARILLO", android.R.color.holo_orange_light, R.drawable.ic_result_amarilla, R.string.priority_yellow),
    VERDE("VERDE", android.R.color.holo_green_dark, R.drawable.ic_result_verde, R.string.priority_green),
    AZUL("AZUL", android.R.color.holo_blue_dark, R.drawable.ic_result_azul, R.string.priority_blue)
}