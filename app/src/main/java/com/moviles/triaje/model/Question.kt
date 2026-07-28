package com.moviles.triaje.model

import java.io.Serializable

data class Question(
    val id: Int,
    val text: String,
    val options: List<String>,
    var selectedOptionIndex: Int = -1
) : Serializable