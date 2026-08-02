package com.moviles.triaje.utils

import com.moviles.triaje.R
import com.moviles.triaje.model.DecisionContext
import com.moviles.triaje.model.Prioridad
import com.moviles.triaje.model.Recomendacion
import kotlin.random.Random

/**
 * Resultado completo devuelto por el Motor Evolutivo.
 */
data class ResultadoEvolutivo(
    val prioridad: Prioridad,
    val diagnosticoProbable: String,
    val explicacionRiesgo: String,
    val recomendaciones: List<Recomendacion>
)

/**
 * Cromosoma (Individuo): Representa una solución candidata completa.
 * Gen 1: Prioridad asignada
 * Gen 2: Índice de Diagnóstico/Hipótesis
 * Gen 3..6: 4 Índices de Recomendaciones únicas
 */
data class Cromosoma(
    val prioridad: Prioridad,
    val diagnosticoIndex: Int,
    val recomendacionesIndices: List<Int>, // Siempre tamaño 4 y sin duplicados
    var fitness: Double = 0.0
)

object EvolutionaryTriageEngine {

    // Configuración del Algoritmo Genético
    private const val TAMANO_POBLACION = 60
    private const val GENERACIONES = 50
    private const val TASA_MUTACION = 0.20
    private const val TAMANO_TORNEO = 4

    // ─────────────────────────────────────────────────────────────────────────────
    // BANCO DE GENES: DIAGNÓSTICOS PROBABLES (Gen 2)
    // ─────────────────────────────────────────────────────────────────────────────
    private val BANCO_DIAGNOSTICOS = listOf(
        // Críticos / Síntomas de Alta Severidad (0..3)
        "Síndrome Coronario Agudo / Patología Cardíaca a Descartar", // Dolor de pecho
        "Insuficiencia Respiratoria Aguda / Compromiso de Vía Aérea", // Dificultad para respirar
        "Síndrome Sincopal / Pérdida Transitoria de Conciencia", // Pérdida de conocimiento
        "Crisis Convulsiva / Trastorno Neurológico Paroxístico", // Convulsiones

        // Lesiones Externas / TFLite y Trauma (4..10)
        "Trauma Lacerante / Herida Abierta con Riesgo de Infección", // Laceration / Cut / Herida abierta
        "Trauma Penetrante / Herida Punzocortante de Cuidado", // Stab_wound
        "Lesión Térmica / Quemadura Aguda de Espesor Variable", // Burns
        "Trauma Contuso / Hematoma o Equimosis Tisular", // Bruises / Golpe fuerte
        "Abrasión Cutánea / Excoriación Superficial", // Abrasions
        "Patología Ungueal / Onicocriptosis con Infección Local", // Ingrown_nails
        "Trauma Osteomuscular / Sospecha de Fractura o Fisura", // Posible fractura

        // Visceral / Sistémico / Físico (11..14)
        "Cuadro Abdominal Agudo a Descartar Proceso Quirúrgico", // Dolor abdominal
        "Síndrome Febril Agudo / Proceso Infeccioso en Estudio", // Fiebre alta
        "Reacción de Hipersensibilidad / Anafilaxia en Evolución", // Reacción alérgica
        "Síndrome de Intoxicación o Exposición a Agente Tóxico", // Intoxicación

        // Picaduras y Manejo General (15..16)
        "Envenenamiento o Reacción Local por Picadura/Mordedura", // Picadura o mordedura
        "Sintomatología Leve / Manejo sintomático de Consulta Externa" // Caso genérico/Azul
    )

    // ─────────────────────────────────────────────────────────────────────────────
    // BANCO DE GENES: RECOMENDACIONES / PRIMEROS AUXILIOS (Gen 3..6)
    // Amplio para permitir cruzamiento real entre distintas áreas clínicas.
    // ─────────────────────────────────────────────────────────────────────────────
    private val BANCO_RECOMENDACIONES = listOf(
        // [0..5] Soporte Vital y Emergencias
        Recomendacion(0, "Llamar a Emergencias", "Comuníquese inmediatamente con el 106 (SAMU) o 116 (Bomberos).", R.drawable.ic_telefono),
        Recomendacion(0, "Asegurar Vía Aérea", "Coloque al paciente en Posición Lateral de Seguridad y verifique la respiración.", R.drawable.ic_escudo),
        Recomendacion(0, "Monitoreo Vital", "Vigile constante de pulso, estado de conciencia y coloración de piel.", R.drawable.ic_corazon),
        Recomendacion(0, "No Dar Alimentos/Líquidos", "No administre nada por vía oral ante riesgo de atragantamiento o aspiración.", R.drawable.ic_pastillas),
        Recomendacion(0, "Reposo y Calma", "Mantenga al paciente sentado o recostado en un ambiente tranquilo.", R.drawable.ic_info),
        Recomendacion(0, "Desabrochar Prendas", "Afloje la ropa ajustada alrededor del cuello, tórax y cintura.", R.drawable.ic_info),

        // [6..9] Heridas, Laceraciones y Cortantes (Cut, Laceration, Stab_wound)
        Recomendacion(0, "Presión Directa", "Presione la herida firmemente con una gasa o paño limpio para detener la hemorragia.", R.drawable.ic_sangrado),
        Recomendacion(0, "No Retirar Objetos", "Si hay un objeto incrustado, NO lo retire; fíjelo suavemente con bordes de gasa.", R.drawable.ic_escudo),
        Recomendacion(0, "Elevar Extremidad", "Si la herida está en un miembro y no hay fractura, elévela sobre el nivel del corazón.", R.drawable.ic_prim_auxilios),
        Recomendacion(0, "Lavado de Herida", "Si el sangrado cesó, lave suavemente con abundante agua y jabón neutro.", R.drawable.ic_info),

        // [10..12] Quemaduras (Burns)
        Recomendacion(0, "Enfriamiento Continuo", "Vierta agua limpia a temperatura ambiente sobre la quemadura por 10 a 15 min.", R.drawable.ic_prim_auxilios),
        Recomendacion(0, "Cubrir sin Presión", "Proteja la zona con un apósito limpio, gasa estéril o film transparente sin apretar.", R.drawable.ic_escudo),
        Recomendacion(0, "No Reventar Ampollas", "No aplique crema, aceite ni dentífrico. Deje las ampollas intactas.", R.drawable.ic_info),

        // [13..15] Contusiones, Raspones y Uñeros (Bruises, Abrasions, Ingrown_nails)
        Recomendacion(0, "Compresas Frías", "Aplique hielo envuelto en una toalla sobre el golpe o hematoma durante 15 minutos.", R.drawable.ic_corazon),
        Recomendacion(0, "Desinfección Superficial", "Limpie la raspadura suavemente con agua y antiséptico para eliminar restos de tierra.", R.drawable.ic_info),
        Recomendacion(0, "Baños de Agua Tibia / Podología", "Para el uñero, remoje el pie en agua tibia con sal y evite calzado apretado.", R.drawable.ic_info),

        // [16..18] Fiebre y Cuadros Pediátricos
        Recomendacion(0, "Medios Físicos Tibios", "Aplique paños tibios (no helados) en frente, axilas e ingle para bajar la temperatura.", R.drawable.ic_corazon),
        Recomendacion(0, "Hidratación Oral Sorbos", "Si está totalmente consciente, ofrezca suero oral o agua en pequeñas dosis.", R.drawable.ic_pastillas),
        Recomendacion(0, "Vigilancia de Convulsión", "En niños con fiebre > 38.5°C, vigile rigidez muscular, desvío de mirada o espasmos.", R.drawable.ic_info),

        // [19..20] Vulnerabilidad / Gestante
        Recomendacion(0, "Decúbito Lateral Izquierdo", "En gestantes, recueste a la persona sobre su lado izquierdo para mejorar el flujo sanguíneo.", R.drawable.ic_escudo),
        Recomendacion(0, "Inmovilización Preventiva", "Si se sospecha de fractura o lesión articular, no fuerce el movimiento del miembro.", R.drawable.ic_escudo),

        // [21..23] Intoxicación y Picaduras
        Recomendacion(0, "Identificar la Sustancia/Animal", "Guarde el envase del químico o tome foto al insecto/animal sin arriesgarse.", R.drawable.ic_info),
        Recomendacion(0, "Retirar Aguijón / Torniquete NO", "Retire el aguijón rascando con una tarjeta. NO haga torniquetes ni succione el veneno.", R.drawable.ic_info),
        Recomendacion(0, "No Provocar el Vómito", "En intoxicaciones por químicos o corrosivos, NO provoque el vómito.", R.drawable.ic_pastillas),

        // [24..26] Orientación General
        Recomendacion(0, "No Automedicar", "Evite administrar analgésicos o antibióticos sin indicación médica explícita.", R.drawable.ic_pastillas),
        Recomendacion(0, "Consulta Ambulatoria", "Acuda al centro de salud o posta más cercana para una evaluación completa.", R.drawable.ic_hospital),
        Recomendacion(0, "Urgencia Médica", "Diríjase de inmediato a la sala de emergencias del hospital más cercano.", R.drawable.ic_hospital)
    )

    /**
     * FUNCIÓN PRINCIPAL: Ejecuta la evolución y devuelve la mejor solución.
     */
    fun evaluarTriajeEvolutivo(context: DecisionContext): ResultadoEvolutivo {

        // 1. Población Inicial Aleatoria
        var poblacion = MutableList(TAMANO_POBLACION) { generarCromosomaAleatorio() }

        // 2. Bucle de Evolución a lo largo de las Generaciones
        for (generacion in 1..GENERACIONES) {

            // Evaluar la adaptación de cada individuo
            poblacion.forEach { c -> c.fitness = calcularFitness(c, context) }

            // Ordenamiento por adaptación (Elitismo: conservar los 4 mejores)
            poblacion.sortByDescending { it.fitness }
            val nuevaPoblacion = mutableListOf<Cromosoma>()

            // Conservar la Élite (mejores soluciones intactas)
            nuevaPoblacion.addAll(poblacion.take(4))

            // Reproducción hasta llenar la siguiente generación
            while (nuevaPoblacion.size < TAMANO_POBLACION) {
                val padre1 = seleccionPorTorneo(poblacion)
                val padre2 = seleccionPorTorneo(poblacion)

                var hijo = cruzar(padre1, padre2)

                if (Random.nextDouble() < TASA_MUTACION) {
                    hijo = mutar(hijo)
                }

                nuevaPoblacion.add(hijo)
            }

            poblacion = nuevaPoblacion
        }

        // 3. Evaluar la generación final y extraer el Individuo Óptimo
        poblacion.forEach { c -> c.fitness = calcularFitness(c, context) }
        val mejorCromosoma = poblacion.maxByOrNull { it.fitness } ?: generarCromosomaAleatorio()

        return construirResultadoFinal(mejorCromosoma, context)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // FUNCIÓN DE FITNESS (El motor de decisión clínica)
    // ─────────────────────────────────────────────────────────────────────────────
    private fun calcularFitness(c: Cromosoma, context: DecisionContext): Double {
        var score = 100.0 // Puntaje base

        val estaConsciente = context.respuestasBasicas.getOrElse(0) { true }
        val respiraNormal = context.respuestasBasicas.getOrElse(1) { true }
        val sangradoGrave = context.respuestasBasicas.getOrElse(2) { false }
        val esCritico = !estaConsciente || !respiraNormal || sangradoGrave

        // A. EVALUACIÓN DE PRIORIDAD
        if (esCritico) {
            if (c.prioridad == Prioridad.ROJA) score += 150.0 else score -= 300.0
        } else {
            // Si no es crítico por descarte, evaluar según preguntas afirmativas
            val afirmativas = context.preguntasDinamicas.count { p ->
                val r = p.options.getOrNull(p.selectedOptionIndex) ?: ""
                r.equals("Sí", ignoreCase = true)
            }
            when {
                afirmativas >= 2 -> if (c.prioridad in listOf(Prioridad.ROJA, Prioridad.NARANJA)) score += 80.0 else score -= 100.0
                afirmativas == 1 -> if (c.prioridad in listOf(Prioridad.NARANJA, Prioridad.AMARILLO)) score += 60.0 else score -= 50.0
                else -> if (c.prioridad in listOf(Prioridad.AMARILLO, Prioridad.VERDE, Prioridad.AZUL)) score += 50.0 else score -= 40.0
            }
        }

        // B. EVALUACIÓN SEGÚN TIPO DE PACIENTE (Vulnerabilidad)
        val esVulnerable = context.tipoPaciente in listOf("NINO", "GESTANTE", "ADULTO_MAYOR", "MAYOR")
        if (esVulnerable) {
            if (c.prioridad == Prioridad.AZUL) score -= 80.0 // Penalizar infravaloración
            if (context.tipoPaciente == "GESTANTE" && c.recomendacionesIndices.contains(16)) score += 50.0 // Favorecer Decúbito Izquierdo
            if (context.tipoPaciente == "NINO" && c.recomendacionesIndices.contains(13)) score += 40.0 // Paños tibios
        }

        // C. EVALUACIÓN SEGÚN IA DE IMAGEN (best.tflite: 7 clases)
        when (context.claseIA) {
            "Burns" -> {
                if (c.diagnosticoIndex == 6) score += 80.0 // Lesión Térmica / Quemadura
                if (c.recomendacionesIndices.contains(10)) score += 60.0 // Enfriamiento
                if (c.recomendacionesIndices.contains(11)) score += 40.0 // Cubrir sin presión
                if (c.recomendacionesIndices.contains(12)) score += 40.0 // No reventar ampollas
            }
            "Stab_wound" -> {
                if (c.diagnosticoIndex == 5) score += 80.0 // Trauma Penetrante
                if (c.recomendacionesIndices.contains(6)) score += 60.0 // Presión directa
                if (c.recomendacionesIndices.contains(7)) score += 60.0 // No retirar objetos
            }
            "Cut", "Laceration" -> {
                if (c.diagnosticoIndex == 4) score += 80.0 // Trauma Lacerante
                if (c.recomendacionesIndices.contains(6)) score += 50.0 // Presión directa
                if (c.recomendacionesIndices.contains(9)) score += 40.0 // Lavado de herida
            }
            "Bruises" -> {
                if (c.diagnosticoIndex == 7) score += 80.0 // Trauma Contuso / Hematoma
                if (c.recomendacionesIndices.contains(13)) score += 60.0 // Compresas frías
                if (c.recomendacionesIndices.contains(20)) score += 40.0 // Inmovilización preventiva
            }
            "Abrasions" -> {
                if (c.diagnosticoIndex == 8) score += 80.0 // Abrasión Cutánea
                if (c.recomendacionesIndices.contains(14)) score += 60.0 // Desinfección superficial
                if (c.recomendacionesIndices.contains(9)) score += 40.0 // Lavado suave
            }
            "Ingrown_nails" -> {
                if (c.diagnosticoIndex == 9) score += 80.0 // Patología Ungueal / Uñero
                if (c.recomendacionesIndices.contains(15)) score += 70.0 // Baños de agua tibia
                if (c.recomendacionesIndices.contains(25)) score += 50.0 // Consulta ambulatoria
            }
        }

        // D. COINCIDENCIA CON LOS 13 SÍNTOMAS SELECCIONADOS
        val idsSintomas = context.sintomas.map { it.id }

        if (idsSintomas.contains("dolor_pecho") && c.diagnosticoIndex == 0) score += 70.0
        if (idsSintomas.contains("dificultad_respiratoria") && c.diagnosticoIndex == 1) score += 70.0
        if (idsSintomas.contains("perdida_conocimiento") && c.diagnosticoIndex == 2) score += 70.0
        if (idsSintomas.contains("convulsiones")) {
            if (c.diagnosticoIndex == 3) score += 70.0
            if (c.recomendacionesIndices.contains(1)) score += 50.0 // Asegurar vía aérea
        }
        if (idsSintomas.contains("posible_fractura")) {
            if (c.diagnosticoIndex == 10) score += 70.0
            if (c.recomendacionesIndices.contains(20)) score += 50.0 // Inmovilización
        }
        if (idsSintomas.contains("dolor_abdominal") && c.diagnosticoIndex == 11) score += 70.0
        if (idsSintomas.contains("fiebre_alta")) {
            if (c.diagnosticoIndex == 12) score += 70.0
            if (c.recomendacionesIndices.contains(16)) score += 50.0 // Medios físicos
        }
        if (idsSintomas.contains("reaccion_alergica") && c.diagnosticoIndex == 13) score += 70.0
        if (idsSintomas.contains("intoxicacion")) {
            if (c.diagnosticoIndex == 14) score += 70.0
            if (c.recomendacionesIndices.contains(23)) score += 60.0 // No provocar el vómito
            if (c.recomendacionesIndices.contains(21)) score += 40.0 // Identificar la sustancia
        }
        if (idsSintomas.contains("picadura_mordedura")) {
            if (c.diagnosticoIndex == 15) score += 70.0
            if (c.recomendacionesIndices.contains(22)) score += 60.0 // Retirar aguijón / No torniquete
        }

        // E. PENALIZACIÓN DE REPETICIONES Y COHERENCIA
        if (c.recomendacionesIndices.distinct().size < 4) score -= 200.0 // Duplicados prohibidos

        return score
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // OPERADORES GENÉTICOS
    // ─────────────────────────────────────────────────────────────────────────────
    private fun generarCromosomaAleatorio(): Cromosoma {
        val prioridad = Prioridad.values().random()
        val diagIndex = Random.nextInt(BANCO_DIAGNOSTICOS.size)
        val recsIndices = (0 until BANCO_RECOMENDACIONES.size).shuffled().take(4)
        return Cromosoma(prioridad, diagIndex, recsIndices)
    }

    private fun seleccionPorTorneo(poblacion: List<Cromosoma>): Cromosoma {
        val participantes = List(TAMANO_TORNEO) { poblacion.random() }
        return participantes.maxByOrNull { it.fitness } ?: poblacion.random()
    }

    private fun cruzar(p1: Cromosoma, p2: Cromosoma): Cromosoma {
        val nuevaPrioridad = if (Random.nextBoolean()) p1.prioridad else p2.prioridad
        val nuevoDiag = if (Random.nextBoolean()) p1.diagnosticoIndex else p2.diagnosticoIndex

        // Cruzamiento de recomendaciones con unificación sin duplicados
        val combinadas = (p1.recomendacionesIndices.take(2) + p2.recomendacionesIndices.take(2)).toMutableList()
        while (combinadas.distinct().size < 4) {
            val candidato = Random.nextInt(BANCO_RECOMENDACIONES.size)
            if (!combinadas.contains(candidato)) combinadas.add(candidato)
        }

        return Cromosoma(nuevaPrioridad, nuevoDiag, combinadas.distinct().take(4))
    }

    private fun mutar(c: Cromosoma): Cromosoma {
        val recsMutadas = c.recomendacionesIndices.toMutableList()
        val idxAMutar = Random.nextInt(4)
        var nuevoGenRec = Random.nextInt(BANCO_RECOMENDACIONES.size)

        // Asegurar que la mutación no genere duplicados
        while (recsMutadas.contains(nuevoGenRec)) {
            nuevoGenRec = Random.nextInt(BANCO_RECOMENDACIONES.size)
        }
        recsMutadas[idxAMutar] = nuevoGenRec

        val nuevaPrioridad = if (Random.nextDouble() < 0.3) Prioridad.values().random() else c.prioridad
        val nuevoDiag = if (Random.nextDouble() < 0.3) Random.nextInt(BANCO_DIAGNOSTICOS.size) else c.diagnosticoIndex

        return Cromosoma(nuevaPrioridad, nuevoDiag, recsMutadas)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // MAPPING FINAL A LA CAPA DE PRESENTACIÓN
    // ─────────────────────────────────────────────────────────────────────────────
    private fun construirResultadoFinal(c: Cromosoma, context: DecisionContext): ResultadoEvolutivo {
        val diagBase = BANCO_DIAGNOSTICOS.getOrElse(c.diagnosticoIndex) { "Evaluación general de triaje" }

        val sintomasTexto = if (context.sintomas.isNotEmpty()) {
            context.sintomas.joinToString(", ") { it.sin_description }
        } else {
            "Evaluación por descarte inicial"
        }

        val diagnosticoCompleto = "$diagBase. Relacionado a: $sintomasTexto."

        val explicacionRiesgo = "Resultado optimizado tras ${GENERACIONES} generaciones evolutivas. " +
                "Ajustado al perfil de paciente ${context.tipoPaciente} con nivel de aptitud (${c.fitness.toInt()} pts)."

        val recomendacionesFinales = c.recomendacionesIndices.mapIndexed { index, recIdx ->
            val plantilla = BANCO_RECOMENDACIONES[recIdx]
            Recomendacion(
                step = index + 1,
                titulo = plantilla.titulo,
                recomendacion = plantilla.recomendacion,
                iconResId = plantilla.iconResId
            )
        }

        return ResultadoEvolutivo(
            prioridad = c.prioridad,
            diagnosticoProbable = diagnosticoCompleto,
            explicacionRiesgo = explicacionRiesgo,
            recomendaciones = recomendacionesFinales
        )
    }
}