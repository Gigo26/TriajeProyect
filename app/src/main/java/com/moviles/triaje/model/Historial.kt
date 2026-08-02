package com.moviles.triaje.model
import com.google.firebase.Timestamp
import java.io.Serializable

data class Historial(
    val id: String = "",
    val categoria: String = "",
    val fechaHora: Timestamp = Timestamp.now()
) : Serializable {
    constructor() : this("", "", Timestamp.now())
}