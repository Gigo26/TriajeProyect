package com.moviles.triaje.model

import com.moviles.triaje.R

enum class Prioridad(val nombre: String, val colorResId: Int, val iconResId: Int) {
    ROJA("ROJA", android.R.color.holo_red_dark, R.drawable.ic_result_roja),
    NARANJA("NARANJA", android.R.color.holo_orange_dark, R.drawable.ic_result_naranja),
    AMARILLO("AMARILLO", android.R.color.holo_orange_light, R.drawable.ic_result_amarilla),
    VERDE("VERDE", android.R.color.holo_green_dark, R.drawable.ic_result_verde),
    AZUL("AZUL", android.R.color.holo_blue_dark, R.drawable.ic_result_azul)
}