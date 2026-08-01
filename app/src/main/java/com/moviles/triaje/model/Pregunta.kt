package com.moviles.triaje.model

import java.io.Serializable

data class Pregunta(
    val id: Int,
    val text: String,
    val text_en: String? = null,
    val options: List<String>,
    val options_en: List<String>? = null,
    var selectedOptionIndex: Int = -1
) : Serializable