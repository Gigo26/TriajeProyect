package com.moviles.triaje.view.adapter

import com.moviles.triaje.model.Sintoma

interface SymptomListener {
    fun onSintomaClicked(sintoma: Sintoma, position: Int)
}