package com.moviles.triaje.utils

import com.moviles.triaje.R
import com.moviles.triaje.model.*
import java.util.Date

/**
 * Motor de Reglas (Rule Engine) encargado de procesar el contexto y generar una Consulta final.
 * Sigue el principio de Responsabilidad Única (SRP).
 */
object TriajeEvaluator {

    fun evaluate(context: DecisionContext): Consulta {
        val prioridadCalculada = calculatePriority(context)
        val diagnosticoPosible = determineDiagnosis(context)
        val recomendaciones = generateFourSteps(context, prioridadCalculada)

        return Consulta(
            con_tipo_paciente = context.tipoPaciente,
            con_sintomas_seleccionados = context.sintomas.map { it.sin_description },
            con_respuestas_basicas = mapOf(
                "consciente" to context.respuestasBasicas.getOrElse(0) { true },
                "respira" to context.respuestasBasicas.getOrElse(1) { true },
                "sangrado" to context.respuestasBasicas.getOrElse(2) { false }
            ),
            con_url_imagen_evidencia = context.uriImagen,
            con_cuestionario_avanzado = context.preguntasDinamicas.associate { it.text to (it.options.getOrNull(it.selectedOptionIndex) ?: "N/A") },
            con_resultado = Resultado(
                prioridad = prioridadCalculada.nombre,
                posibilidad = diagnosticoPosible
            ),
            con_recomendaciones = recomendaciones,
            con_fecha_registro = Date()
        )
    }

    private fun calculatePriority(context: DecisionContext): Prioridad {
        val estaConsciente = context.respuestasBasicas.getOrElse(0) { true }
        val respiraNormal = context.respuestasBasicas.getOrElse(1) { true }
        val sangradoGrave = context.respuestasBasicas.getOrElse(2) { false }

        // Regla de Oro: Evaluación Primaria
        if (!estaConsciente || !respiraNormal || sangradoGrave) return Prioridad.ROJA

        // Regla de IA
        return when (context.claseIA) {
            "Stab_wound", "Burns" -> Prioridad.NARANJA
            "Laceration", "Cut" -> Prioridad.AMARILLO
            "Bruises", "Abrasions" -> Prioridad.VERDE
            else -> evaluateSymptomSeverity(context.sintomas)
        }
    }

    private fun evaluateSymptomSeverity(sintomas: List<Sintoma>): Prioridad {
        return if (sintomas.any { it.sin_description.contains("pecho", true) || it.sin_description.contains("respirar", true) }) {
            Prioridad.NARANJA
        } else {
            Prioridad.AZUL
        }
    }

    private fun determineDiagnosis(context: DecisionContext): String {
        return buildString {
            if (context.claseIA != null) append("IA detectó: ${context.claseIA}. ")
            if (context.sintomas.isNotEmpty()) append("Síntomas: ${context.sintomas.take(2).joinToString { it.sin_description }}.")
            else append("Evaluación preventiva realizada.")
        }
    }

    private fun generateFourSteps(context: DecisionContext, prioridad: Prioridad): List<Recomendacion> {
        val steps = mutableListOf<Recomendacion>()

        when {
            prioridad == Prioridad.ROJA -> {
                steps.add(Recomendacion(1, "Llamar Emergencias", "Comuníquese al 106 (SAMU) o 116 (Bomberos) de inmediato.", R.drawable.ic_telefono))
                steps.add(Recomendacion(2, "Vía Aérea", "Asegure que la persona tenga la vía aérea despejada.", R.drawable.ic_info))
                steps.add(Recomendacion(3, "No Movilizar", "Si hay sospecha de trauma, no mueva a la persona.", R.drawable.ic_escudo))
                steps.add(Recomendacion(4, "RCP", "Si deja de respirar, inicie maniobras de reanimación básica.", R.drawable.ic_corazon))
            }
            context.claseIA == "Burns" -> {
                steps.add(Recomendacion(1, "Enfriar", "Irrigue la zona con agua fría por 10-15 minutos.", R.drawable.ic_prim_auxilios))
                steps.add(Recomendacion(2, "No Ungüentos", "No aplique pasta dental, aceites ni cremas caseras.", R.drawable.ic_info))
                steps.add(Recomendacion(3, "Protección", "Cubra con un apósito limpio o film transparente.", R.drawable.ic_escudo))
                steps.add(Recomendacion(4, "Hidratar", "Ofrezca agua si la persona está consciente y orientada.", R.drawable.ic_pastillas))
            }
            else -> {
                steps.add(Recomendacion(1, "Mantener Calma", "Evite el pánico para no elevar la presión del paciente.", R.drawable.ic_info))
                steps.add(Recomendacion(2, "Posición Cómoda", "Siente o recueste a la persona en un lugar ventilado.", R.drawable.ic_home))
                steps.add(Recomendacion(3, "Vigilar Síntomas", "Si el dolor aumenta o hay desmayo, llame a un médico.", R.drawable.ic_corazon))
                steps.add(Recomendacion(4, "Atención Profesional", "Acuda a un centro de salud para evaluación definitiva.", R.drawable.ic_hospital))
            }
        }
        return steps.take(4)
    }
}
