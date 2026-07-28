package com.moviles.triaje.model

import java.io.Serializable

data class Recomendacion (
    val step: Int = 0,
    val recomendacion: String = ""
) : Serializable