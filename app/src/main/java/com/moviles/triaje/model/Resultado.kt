package com.moviles.triaje.model

import java.io.Serializable

data class Resultado (
    val prioridad: String = "",
    val posibilidad: String = ""
) : Serializable