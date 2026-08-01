package com.moviles.triaje.model

import java.io.Serializable

data class Recomendacion (
    val step: Int = 0,
    val titulo: String = "",
    val recomendacion: String = "",
    val iconResId: Int = 0
) : Serializable