package com.moviles.triaje.model

import com.google.gson.annotations.SerializedName

data class DniResponse (
    val status : Int,
    val message : String,
    val success : Boolean,
    val data : PersonaDni
)

data class PersonaDni (
    val numero : String,
    val nombres : String,

    @SerializedName("apellido_paterno")
    val apellidoPaterno : String,

    @SerializedName("apellido_materno")
    val apellidoMaterno: String
)