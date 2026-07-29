package com.moviles.triaje.model

import java.io.Serializable
import java.util.Date

data class Usuario(
    val us_nombre: String = "",
    val us_apellidos: String = "",
    val us_dni: String = "",
    val us_celular: String? = null,
    val us_email: String = "",
    val us_fecha_nac: Date? = null,
    val us_avatar: String? = null
) : Serializable