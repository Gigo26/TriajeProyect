package com.moviles.triaje.view.adapter

import com.moviles.triaje.model.Pregunta

interface QuestionListener {
    /**
     * Se dispara cada vez que el usuario marca una opción (Sí/No u otra) en una pregunta.
     * @param question El objeto pregunta afectado.
     * @param optionIndex El índice numérico de la opción seleccionada.
     * @param position La posición de la tarjeta dentro del RecyclerView.
     */
    fun onOptionSelected(question: Pregunta, optionIndex: Int, position: Int)
}