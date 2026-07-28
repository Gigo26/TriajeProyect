package com.moviles.triaje.model

import java.io.Serializable

data class Sintoma(
    val id: String = "", // Para guardar el ID del documento de Firestore
    val sin_description: String = "",
    val sin_image: String = "",
    val sin_requiere_imagen: Boolean = false,
    var isSelected: Boolean = false // 🔥 Campo local para manejar la selección visual en el RecyclerView
) : Serializable