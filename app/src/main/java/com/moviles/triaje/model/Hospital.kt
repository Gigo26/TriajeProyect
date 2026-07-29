package com.moviles.triaje.model

import java.io.Serializable

data class Hospital(
    val id: String = "",
    val nombre: String = "",
    val distancia: String = "",
    val direccion: String = "",
    val tiempoEstimado: String = "",
    val imagenUrl: String = "",
    val telefono: String = ""
) : Serializable
