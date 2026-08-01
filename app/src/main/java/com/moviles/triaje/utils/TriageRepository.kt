package com.moviles.triaje.utils

import com.moviles.triaje.R
import com.moviles.triaje.model.Prioridad
import com.moviles.triaje.model.Recomendacion
import com.moviles.triaje.model.TriajeResultado

object TriageRepository {

    fun evaluarTriaje(
        respuestasDescarte: List<Boolean>, // [Consciente, RespiraNormal, SangradoIncontrolable]
        sintomasSeleccionados: List<String>,
        claseTFLite: String?
    ): TriajeResultado {
        
        // 1. Calcular Prioridad
        // Consciente: Si responde "No" -> ROJA
        // Respira: Si responde "No" -> ROJA
        // Sangrado: Si responde "Sí" -> ROJA
        
        val estaConsciente = respuestasDescarte.getOrNull(0) ?: true
        val respiraNormal = respuestasDescarte.getOrNull(1) ?: true
        val sangradoIncontrolable = respuestasDescarte.getOrNull(2) ?: false

        val prioridad = when {
            !estaConsciente || !respiraNormal || sangradoIncontrolable -> Prioridad.ROJA
            sintomasSeleccionados.contains("Dolor de pecho") || sintomasSeleccionados.contains("Dificultad respiratoria") -> Prioridad.NARANJA
            claseTFLite == "Stab_wound" || claseTFLite == "Burns" -> Prioridad.AMARILLO
            claseTFLite == "Laceration" || claseTFLite == "Cut" -> Prioridad.VERDE
            else -> Prioridad.AZUL
        }

        // 2. Determinar Posible Diagnóstico
        val diagnostico = when {
            claseTFLite != null -> "Posible $claseTFLite"
            sintomasSeleccionados.isNotEmpty() -> "Evaluación por ${sintomasSeleccionados.first()}"
            else -> "Malestar general"
        }

        // 3. Generar EXACTAMENTE 4 Recomendaciones
        val recomendaciones = generarRecomendaciones(claseTFLite, sintomasSeleccionados, prioridad)

        return TriajeResultado(prioridad, diagnostico, recomendaciones)
    }

    private fun generarRecomendaciones(
        claseTFLite: String?,
        sintomas: List<String>,
        prioridad: Prioridad
    ): List<Recomendacion> {
        val steps = mutableListOf<Recomendacion>()

        // Recomendaciones base según el caso principal
        when (claseTFLite) {
            "Burns" -> {
                steps.add(Recomendacion(1, "Enfriar", "Aplique agua fría sobre la quemadura por al menos 10 minutos.", R.drawable.ic_prim_auxilios))
                steps.add(Recomendacion(2, "Cubrir", "Cubra el área con un apósito limpio o film transparente sin presionar.", R.drawable.ic_escudo))
                steps.add(Recomendacion(3, "No reventar", "No aplique ungüentos ni reviente las ampollas si aparecen.", R.drawable.ic_info))
                steps.add(Recomendacion(4, "Hidratar", "Beba líquidos y espere la evaluación médica profesional.", R.drawable.ic_pastillas))
            }
            "Stab_wound", "Cut", "Laceration" -> {
                steps.add(Recomendacion(1, "Presión", "Aplique presión directa sobre la herida con una gasa limpia.", R.drawable.ic_sangrado))
                steps.add(Recomendacion(2, "Elevar", "Si es posible, mantenga la zona afectada por encima del nivel del corazón.", R.drawable.ic_prim_auxilios))
                steps.add(Recomendacion(3, "Limpieza", "Si el sangrado para, lave suavemente con agua y jabón neutro.", R.drawable.ic_info))
                steps.add(Recomendacion(4, "Inmovilizar", "Evite mover la zona y espere indicaciones del personal de salud.", R.drawable.ic_escudo))
            }
            "Bruises", "Abrasions" -> {
                steps.add(Recomendacion(1, "Lavar", "Lave la zona con agua limpia para eliminar impurezas.", R.drawable.ic_info))
                steps.add(Recomendacion(2, "Frío", "Aplique compresas frías para reducir la inflamación.", R.drawable.ic_corazon))
                steps.add(Recomendacion(3, "Protección", "Cubra con una venda ligera si hay riesgo de roce.", R.drawable.ic_escudo))
                steps.add(Recomendacion(4, "Reposo", "Mantenga la zona en reposo y observe cambios de coloración.", R.drawable.ic_info))
            }
            else -> {
                // Recomendaciones genéricas basadas en prioridad o síntomas
                if (prioridad == Prioridad.ROJA) {
                    steps.add(Recomendacion(1, "Llamar 106/116", "Comuníquese inmediatamente con emergencias (SAMU/Bomberos).", R.drawable.ic_telefono))
                    steps.add(Recomendacion(2, "Posición", "Mantenga al paciente en una posición segura y cómoda.", R.drawable.ic_info))
                    steps.add(Recomendacion(3, "Monitoreo", "Vigile constantemente la respiración y el pulso.", R.drawable.ic_corazon))
                    steps.add(Recomendacion(4, "No medicar", "No suministre alimentos ni medicamentos por vía oral.", R.drawable.ic_pastillas))
                } else {
                    steps.add(Recomendacion(1, "Tranquilidad", "Mantenga la calma y ayude al paciente a relajarse.", R.drawable.ic_info))
                    steps.add(Recomendacion(2, "Observación", "Vigile si los síntomas empeoran en los próximos minutos.", R.drawable.ic_info))
                    steps.add(Recomendacion(3, "Hidratación", "Si el paciente está consciente, ofrezca pequeños sorbos de agua.", R.drawable.ic_pastillas))
                    steps.add(Recomendacion(4, "Consulta médica", "Acuda al centro de salud más cercano para una revisión.", R.drawable.ic_hospital))
                }
            }
        }

        // Asegurar que siempre hay 4
        while (steps.size < 4) {
            steps.add(Recomendacion(steps.size + 1, "Soporte", "Siga las indicaciones generales de primeros auxilios.", R.drawable.ic_info))
        }
        
        return steps.take(4)
    }
}