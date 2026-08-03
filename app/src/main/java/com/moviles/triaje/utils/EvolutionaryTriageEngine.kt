package com.moviles.triaje.utils

import com.moviles.triaje.R
import com.moviles.triaje.model.DecisionContext
import com.moviles.triaje.model.Prioridad
import com.moviles.triaje.model.Recomendacion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

data class ResultadoEvolutivo(
    val prioridad: Prioridad,
    val diagnosticoProbable: String,
    val explicacionRiesgo: String,
    val recomendaciones: List<Recomendacion>
)

data class Cromosoma(
    val prioridad: Prioridad,
    val diagnosticoIndex: Int,
    val recomendacionesIndices: List<Int>, // REDUCIDO A 4 GENES
    var fitness: Double = 0.0,
    var fitnessCalculado: Boolean = false
)

object EvolutionaryTriageEngine {

    private const val TAMANO_POBLACION = 150
    private const val GENERACIONES = 100
    private const val TASA_MUTACION_BASE = 0.15
    private const val TAMANO_TORNEO = 5
    private const val LIMITE_ESTANCAMIENTO = 15

    private val BANCO_RESULTADO_POSIBLE = listOf(
        R.string.diag_0, R.string.diag_1, R.string.diag_2, R.string.diag_3, R.string.diag_4,
        R.string.diag_5, R.string.diag_6, R.string.diag_7, R.string.diag_8, R.string.diag_9,
        R.string.diag_10, R.string.diag_11, R.string.diag_12, R.string.diag_13, R.string.diag_14,
        R.string.diag_15, R.string.diag_16, R.string.diag_17, R.string.diag_18, R.string.diag_19,
        R.string.diag_20, R.string.diag_21, R.string.diag_22, R.string.diag_23, R.string.diag_24
    )

    private val BANCO_RECOMENDACIONES_RESOURCES = listOf(
        Pair(R.string.rec_title_0, R.string.rec_desc_0),
        Pair(R.string.rec_title_1, R.string.rec_desc_1),
        Pair(R.string.rec_title_2, R.string.rec_desc_2),
        Pair(R.string.rec_title_3, R.string.rec_desc_3),
        Pair(R.string.rec_title_4, R.string.rec_desc_4),
        Pair(R.string.rec_title_5, R.string.rec_desc_5),
        Pair(R.string.rec_title_6, R.string.rec_desc_6),
        Pair(R.string.rec_title_7, R.string.rec_desc_7),
        Pair(R.string.rec_title_8, R.string.rec_desc_8),
        Pair(R.string.rec_title_9, R.string.rec_desc_9),
        Pair(R.string.rec_title_10, R.string.rec_desc_10),
        Pair(R.string.rec_title_11, R.string.rec_desc_11),
        Pair(R.string.rec_title_12, R.string.rec_desc_12),
        Pair(R.string.rec_title_13, R.string.rec_desc_13),
        Pair(R.string.rec_title_14, R.string.rec_desc_14),
        Pair(R.string.rec_title_15, R.string.rec_desc_15),
        Pair(R.string.rec_title_16, R.string.rec_desc_16),
        Pair(R.string.rec_title_17, R.string.rec_desc_17),
        Pair(R.string.rec_title_18, R.string.rec_desc_18),
        Pair(R.string.rec_title_19, R.string.rec_desc_19),
        Pair(R.string.rec_title_20, R.string.rec_desc_20),
        Pair(R.string.rec_title_21, R.string.rec_desc_21),
        Pair(R.string.rec_title_22, R.string.rec_desc_22),
        Pair(R.string.rec_title_23, R.string.rec_desc_23),
        Pair(R.string.rec_title_24, R.string.rec_desc_24),
        Pair(R.string.rec_title_25, R.string.rec_desc_25),
        Pair(R.string.rec_title_26, R.string.rec_desc_26),
        Pair(R.string.rec_title_27, R.string.rec_desc_27),
        Pair(R.string.rec_title_28, R.string.rec_desc_28),
        Pair(R.string.rec_title_29, R.string.rec_desc_29)
    )

    private val BANCO_RECOMENDACIONES_ICONS = listOf(
        R.drawable.ic_telefono, R.drawable.ic_escudo, R.drawable.ic_corazon, R.drawable.ic_pastillas,
        R.drawable.ic_corazon, R.drawable.ic_sangrado, R.drawable.ic_escudo, R.drawable.ic_sangrado,
        R.drawable.ic_info, R.drawable.ic_prim_auxilios, R.drawable.ic_prim_auxilios, R.drawable.ic_info,
        R.drawable.ic_escudo, R.drawable.ic_info, R.drawable.ic_corazon, R.drawable.ic_escudo,
        R.drawable.ic_info, R.drawable.ic_info, R.drawable.ic_escudo, R.drawable.ic_corazon,
        R.drawable.ic_info, R.drawable.ic_pastillas, R.drawable.ic_info, R.drawable.ic_pastillas,
        R.drawable.ic_info, R.drawable.ic_escudo, R.drawable.ic_corazon, R.drawable.ic_pastillas,
        R.drawable.ic_hospital, R.drawable.ic_hospital
    )

    fun evaluarTriajeEvolutivo(context: DecisionContext): ResultadoEvolutivo {
        var poblacion = MutableList(TAMANO_POBLACION) { generarCromosomaAleatorio() }
        var mejorFitnessHistorico = -9999.0
        var generacionesSinMejora = 0

        val cpuCores = Runtime.getRuntime().availableProcessors()
        val dispatcherOptimo = Dispatchers.Default.limitedParallelism(cpuCores)

        runBlocking(dispatcherOptimo) {
            for (generacion in 1..GENERACIONES) {
                val evaluaciones = poblacion.map { c ->
                    async {
                        if (!c.fitnessCalculado) {
                            c.fitness = calcularFitness(c, context)
                            c.fitnessCalculado = true
                        }
                    }
                }
                evaluaciones.awaitAll()

                val mejorActual = poblacion.maxByOrNull { it.fitness }?.fitness ?: 0.0
                if (mejorActual > mejorFitnessHistorico) {
                    mejorFitnessHistorico = mejorActual
                    generacionesSinMejora = 0
                } else {
                    generacionesSinMejora++
                }

                if (generacionesSinMejora >= LIMITE_ESTANCAMIENTO) break

                poblacion.sortByDescending { it.fitness }
                val nuevaPoblacion = mutableListOf<Cromosoma>()
                nuevaPoblacion.addAll(poblacion.take(10))

                while (nuevaPoblacion.size < TAMANO_POBLACION) {
                    val padre1 = seleccionPorTorneo(poblacion)
                    val padre2 = seleccionPorTorneo(poblacion)
                    var hijo = cruzarUniforme(padre1, padre2)

                    val tasaMutacionActual = if (hijo.fitness < 50.0) TASA_MUTACION_BASE * 2 else TASA_MUTACION_BASE
                    if (Random.nextDouble() < tasaMutacionActual) hijo = mutar(hijo)
                    nuevaPoblacion.add(hijo)
                }
                poblacion = nuevaPoblacion
            }

            val evaluacionesFinales = poblacion.map { c ->
                async {
                    if (!c.fitnessCalculado) {
                        c.fitness = calcularFitness(c, context)
                        c.fitnessCalculado = true
                    }
                }
            }
            evaluacionesFinales.awaitAll()
        }

        val mejorCromosoma = poblacion.maxByOrNull { it.fitness } ?: generarCromosomaAleatorio()
        return construirResultadoFinal(mejorCromosoma, context)
    }

    private fun calcularFitness(c: Cromosoma, context: DecisionContext): Double {
        var score = 0.0
        val ids = context.sintomas.map { it.id }

        val consciente = context.respuestasBasicas.getOrElse(0) { true }
        val respira = context.respuestasBasicas.getOrElse(1) { true }
        val sangra = context.respuestasBasicas.getOrElse(2) { false }
        val esCritico = !consciente || !respira || sangra

        val sintomasTrauma = listOf("golpe_fuerte", "herida_abierta", "posible_fractura", "quemadura")
        val esTrauma = ids.any { it in sintomasTrauma } || (!context.claseIA.isNullOrEmpty() && context.claseIA != "Error" && context.claseIA != "No se pudo determinar")

        if (esCritico) {
            if (c.prioridad == Prioridad.ROJA) score += 500.0 else score -= 800.0
            if (!respira && c.recomendacionesIndices.contains(4)) score += 200.0
        } else {
            val afirmativas = context.preguntasDinamicas.count { p ->
                val r = p.options.getOrNull(p.selectedOptionIndex) ?: ""
                r.equals("Sí", ignoreCase = true) || r.equals("Yes", ignoreCase = true)
            }
            when {
                afirmativas >= 3 -> if (c.prioridad == Prioridad.ROJA || c.prioridad == Prioridad.NARANJA) score += 300.0 else score -= 200.0
                afirmativas == 2 -> if (c.prioridad == Prioridad.NARANJA || c.prioridad == Prioridad.AMARILLO) score += 200.0 else score -= 100.0
                afirmativas == 1 -> if (c.prioridad == Prioridad.AMARILLO || c.prioridad == Prioridad.VERDE) score += 150.0 else score -= 100.0
                else -> if (c.prioridad == Prioridad.AZUL || c.prioridad == Prioridad.VERDE) score += 200.0 else score -= 150.0
            }
        }

        when (context.claseIA) {
            "Burns" -> {
                if (c.diagnosticoIndex == 9) score += 300.0
                if (c.recomendacionesIndices.contains(10)) score += 100.0
                if (c.recomendacionesIndices.contains(12)) score += 100.0
            }
            "Stab_wound" -> {
                if (c.diagnosticoIndex == 8) score += 300.0
                if (c.recomendacionesIndices.contains(5)) score += 100.0
                if (c.recomendacionesIndices.contains(6)) score += 150.0
            }
            "Cut", "Laceration" -> {
                if (c.diagnosticoIndex == 7) score += 300.0
                if (c.recomendacionesIndices.contains(5)) score += 100.0
                if (c.recomendacionesIndices.contains(8)) score += 100.0
            }
            "Bruises" -> {
                if (c.diagnosticoIndex == 10) score += 300.0
                if (c.recomendacionesIndices.contains(14)) score += 100.0
            }
            "Abrasions" -> {
                if (c.diagnosticoIndex == 11) score += 300.0
                if (c.recomendacionesIndices.contains(16)) score += 100.0
            }
            "Ingrown_nails" -> {
                if (c.diagnosticoIndex == 12) score += 300.0
                if (c.recomendacionesIndices.contains(17)) score += 100.0
                if (c.recomendacionesIndices.contains(18)) score += 100.0
            }
        }

        if ("dolor_pecho" in ids) { if (c.diagnosticoIndex in 0..1) score += 200.0 }
        if ("dificultad_respiratoria" in ids) { if (c.diagnosticoIndex == 2) score += 200.0 }
        if ("perdida_conocimiento" in ids) { if (c.diagnosticoIndex == 5) score += 200.0 }
        if ("convulsiones" in ids) {
            if (c.diagnosticoIndex == 6) score += 200.0
            if (c.recomendacionesIndices.contains(1)) score += 100.0
        }
        if ("dolor_abdominal" in ids) {
            if (c.diagnosticoIndex in 14..15) score += 200.0
            if (c.recomendacionesIndices.contains(3)) score += 150.0
        }
        if ("fiebre_alta" in ids) {
            if (c.diagnosticoIndex == 16) score += 200.0
            if (c.recomendacionesIndices.contains(19)) score += 100.0
        }

        if (context.tipoPaciente == "GESTANTE" && c.recomendacionesIndices.contains(25)) score += 150.0
        if (context.tipoPaciente == "NINO" && c.recomendacionesIndices.contains(26)) score += 100.0

        // E. RESTRICCIONES GENÉTICAS (Tamaño 4)
        if (c.recomendacionesIndices.distinct().size < 4) score -= 1000.0

        // =========================================================================
        // F. EL SÚPER FILTRO DE SENTIDO COMÚN (LA MASACRE DE GENES)
        // =========================================================================

        // 1. Coherencia de Prioridad
        if (c.prioridad == Prioridad.AZUL || c.prioridad == Prioridad.VERDE) {
            if (c.recomendacionesIndices.contains(0)) score -= 800.0
            if (c.recomendacionesIndices.contains(28)) score -= 800.0
            if (c.recomendacionesIndices.contains(29)) score += 150.0
        } else if (c.prioridad == Prioridad.ROJA || c.prioridad == Prioridad.NARANJA) {
            if (c.recomendacionesIndices.contains(29)) score -= 800.0
            if (c.recomendacionesIndices.contains(0)) score += 150.0
            if (c.recomendacionesIndices.contains(28)) score += 150.0
        }

        // 2. Prohibir Trauma en Casos Internos (Fiebre, Dolor, etc.)
        if (!esTrauma) {
            val prohibidosTrauma = listOf(5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18)
            c.recomendacionesIndices.forEach { if (it in prohibidosTrauma) score -= 800.0 }
        } else {
            // Específicos dentro de Trauma
            if ("posible_fractura" !in ids && "golpe_fuerte" !in ids) {
                if (c.recomendacionesIndices.contains(15)) score -= 600.0 // Adiós Férula
            }
            if (context.claseIA != "Burns" && "quemadura" !in ids) {
                listOf(10, 11, 12, 13).forEach { if (c.recomendacionesIndices.contains(it)) score -= 600.0 } // Adiós Quemaduras
            }
            if (context.claseIA != "Ingrown_nails") {
                listOf(17, 18).forEach { if (c.recomendacionesIndices.contains(it)) score -= 600.0 } // Adiós Uñero
            }
        }

        // 3. Prohibir Enfermedades Específicas
        if ("fiebre_alta" !in ids) {
            if (c.recomendacionesIndices.contains(19)) score -= 600.0 // Adiós paños tibios
        }
        if ("reaccion_alergica" !in ids) {
            if (c.recomendacionesIndices.contains(21)) score -= 800.0 // Adiós Epinefrina
        }
        if ("intoxicacion" !in ids) {
            listOf(22, 23).forEach { if (c.recomendacionesIndices.contains(it)) score -= 600.0 } // Adiós vómito/tóxico
        }
        if ("picadura_mordedura" !in ids) {
            if (c.recomendacionesIndices.contains(24)) score -= 600.0 // Adiós aguijón
        }

        // 4. Vulnerabilidad Restringida
        if (context.tipoPaciente != "GESTANTE") {
            if (c.recomendacionesIndices.contains(25)) score -= 600.0
        }
        if (context.tipoPaciente != "NINO" && context.tipoPaciente != "ADULTO_MAYOR") {
            if (c.recomendacionesIndices.contains(26)) score -= 500.0
        }

        // 5. Coherencia Vital
        if (!sangra) {
            listOf(5, 7, 9).forEach { if (c.recomendacionesIndices.contains(it)) score -= 800.0 } // Adiós torniquetes
        }
        if (respira) {
            if (c.recomendacionesIndices.contains(4)) score -= 1000.0 // Adiós RCP
        }

        return score
    }

    private fun generarCromosomaAleatorio(): Cromosoma {
        val prio = Prioridad.values().random()
        val diag = Random.nextInt(BANCO_RESULTADO_POSIBLE.size)
        val recs = (0 until BANCO_RECOMENDACIONES_RESOURCES.size).shuffled().take(4) // TAMAÑO 4
        return Cromosoma(prio, diag, recs, fitnessCalculado = false)
    }

    private fun seleccionPorTorneo(poblacion: List<Cromosoma>): Cromosoma {
        val torneo = List(TAMANO_TORNEO) { poblacion.random() }
        return torneo.maxByOrNull { it.fitness } ?: poblacion.random()
    }

    private fun cruzarUniforme(p1: Cromosoma, p2: Cromosoma): Cromosoma {
        val prio = if (Random.nextBoolean()) p1.prioridad else p2.prioridad
        val diag = if (Random.nextBoolean()) p1.diagnosticoIndex else p2.diagnosticoIndex

        val recs = mutableSetOf<Int>()
        val poolPadres = p1.recomendacionesIndices + p2.recomendacionesIndices

        while (recs.size < 4) { // TAMAÑO 4
            recs.add(poolPadres.random())
        }
        return Cromosoma(prio, diag, recs.toList(), fitnessCalculado = false)
    }

    private fun mutar(c: Cromosoma): Cromosoma {
        val recs = c.recomendacionesIndices.toMutableList()
        val idx = Random.nextInt(4) // TAMAÑO 4
        var nuevoGen = Random.nextInt(BANCO_RECOMENDACIONES_RESOURCES.size)
        while (recs.contains(nuevoGen)) { nuevoGen = Random.nextInt(BANCO_RECOMENDACIONES_RESOURCES.size) }
        recs[idx] = nuevoGen

        val prio = if (Random.nextDouble() < 0.2) Prioridad.values().random() else c.prioridad
        val diag = if (Random.nextDouble() < 0.2) Random.nextInt(BANCO_RESULTADO_POSIBLE.size) else c.diagnosticoIndex

        return Cromosoma(prio, diag, recs, fitnessCalculado = false)
    }

    private fun construirResultadoFinal(c: Cromosoma, context: DecisionContext): ResultadoEvolutivo {
        val diagResId = BANCO_RESULTADO_POSIBLE.getOrElse(c.diagnosticoIndex) { R.string.diag_default }
        val diagnostico = context.context.getString(diagResId)
        val explicacionMedica = redactarJustificacionClinica(c, context)

        val recomendaciones = c.recomendacionesIndices.mapIndexed { index, recIdx ->
            val pair = BANCO_RECOMENDACIONES_RESOURCES[recIdx]
            val icon = BANCO_RECOMENDACIONES_ICONS[recIdx]
            Recomendacion(
                index + 1,
                context.context.getString(pair.first),
                context.context.getString(pair.second),
                icon
            )
        }

        return ResultadoEvolutivo(c.prioridad, diagnostico, explicacionMedica, recomendaciones)
    }

    private fun redactarJustificacionClinica(c: Cromosoma, context: DecisionContext): String {
        val androidContext = context.context
        val sb = StringBuilder(androidContext.getString(R.string.just_intro))
        sb.append(" ")

        val sintomas = context.sintomas.joinToString(", ") { it.sin_description.lowercase() }
        if (sintomas.isNotEmpty()) {
            sb.append(androidContext.getString(R.string.just_patient_manifests, sintomas))
            sb.append(" ")
        }

        if (!context.claseIA.isNullOrEmpty() && context.claseIA != "Error" && context.claseIA != "No se pudo determinar") {
            sb.append(androidContext.getString(R.string.just_ia_detected, context.claseIA))
            sb.append(" ")
        }

        if (context.tipoPaciente != "ADULTO") {
            val pacienteF = context.tipoPaciente.lowercase().replace("_", " ")
            sb.append(androidContext.getString(R.string.just_vulnerability, pacienteF))
            sb.append(" ")
        }

        val consciente = context.respuestasBasicas.getOrElse(0) { true }
        val respira = context.respuestasBasicas.getOrElse(1) { true }
        val sangra = context.respuestasBasicas.getOrElse(2) { false }
        val esCritico = !consciente || !respira || sangra

        if (esCritico) {
            val alarmasClinicas = mutableListOf<String>()
            if (!consciente) alarmasClinicas.add(androidContext.getString(R.string.just_alarm_consciousness))
            if (!respira) alarmasClinicas.add(androidContext.getString(R.string.just_alarm_respiratory))
            if (sangra) alarmasClinicas.add(androidContext.getString(R.string.just_alarm_bleeding))

            sb.append(androidContext.getString(R.string.just_critical_alert, alarmasClinicas.joinToString(" y ")))
            sb.append(" ")
            sb.append(androidContext.getString(R.string.just_assign_priority, androidContext.getString(c.prioridad.stringResId)))
        } else {
            val graves = context.preguntasDinamicas.count { 
                val r = it.options.getOrNull(it.selectedOptionIndex) ?: ""
                r.equals("Sí", ignoreCase = true) || r.equals("Yes", ignoreCase = true)
            }
            sb.append(androidContext.getString(R.string.just_determine_risk, androidContext.getString(c.prioridad.stringResId), graves))
        }

        return sb.toString()
    }
}
