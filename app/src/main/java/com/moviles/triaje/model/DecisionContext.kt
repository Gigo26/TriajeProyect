package com.moviles.triaje.model

import java.io.Serializable

/**
 * Entidad de dominio que agrupa todos los datos de entrada para la toma de decisiones.
 * Reutiliza las clases existentes del proyecto.
 */
data class DecisionContext(
    val tipoPaciente: String,
    val respuestasBasicas: List<Boolean>, // [Consciente, Respira, Sangrado]
    val sintomas: List<Sintoma>,
    val claseIA: String?,
    val uriImagen: String?,
    val preguntasDinamicas: List<Pregunta>
) : Serializable
