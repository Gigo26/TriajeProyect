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
        "Síndrome Coronario Agudo / Infarto Agudo de Miocardio", // 0
        "Arritmia Cardíaca Severa con Descompensación", // 1
        "Insuficiencia Respiratoria Aguda / Asma Severa", // 2
        "Neumonía Adquirida con Criterios de Gravedad", // 3
        "Enfermedad Cerebrovascular (ACV) en Ventana Terapéutica", // 4
        "Síndrome Sincopal / Pérdida Transitoria de Conciencia", // 5
        "Status Convulsivo / Trastorno Neurológico Paroxístico", // 6
        "Trauma Lacerante Profundo con Riesgo de Infección Mayor", // 7
        "Herida Punzocortante Penetrante con Compromiso Vascular", // 8
        "Lesión Térmica / Quemadura de Segundo o Tercer Grado", // 9
        "Trauma Contuso Severo / Hematoma Estructural", // 10
        "Abrasión Cutánea Extensa con Riesgo de Contaminación", // 11
        "Onicocriptosis Complicada / Infección Periungueal", // 12
        "Trauma Osteomuscular / Fractura Cerrada o Expuesta", // 13
        "Abdomen Agudo Quirúrgico (Apendicitis/Colecistitis)", // 14
        "Hemorragia Digestiva Activa", // 15
        "Síndrome Febril Agudo de Foco Infeccioso Desconocido", // 16
        "Sepsis de Origen Indeterminado", // 17
        "Choque Anafiláctico / Reacción Alérgica Severa", // 18
        "Intoxicación Aguda por Agente Químico/Farmacológico", // 19
        "Envenenamiento Sistémico por Mordedura/Picadura", // 20
        "Infección Respiratoria Alta (Manejo Ambulatorio)", // 21
        "Gastroenteritis Aguda sin Signos de Deshidratación", // 22
        "Mialgia o Dolor Articular Leve", // 23
        "Sintomatología Leve / Evaluación por Consulta Externa" // 24
    )

    private val BANCO_RECOMENDACIONES = listOf(
        Recomendacion(0, "Activar Sistema de Emergencia", "Comuníquese de inmediato con el 106 (SAMU) o 116 (Bomberos).", R.drawable.ic_telefono), // 0
        Recomendacion(0, "Asegurar Vía Aérea", "Coloque al paciente de lado (Posición Lateral de Seguridad) para evitar ahogamiento.", R.drawable.ic_escudo), // 1
        Recomendacion(0, "Monitoreo Vital Constante", "Vigile respiración, pulso y estado de alerta cada 5 minutos.", R.drawable.ic_corazon), // 2
        Recomendacion(0, "Ayuno Estricto", "No administre absolutamente nada por vía oral (ni agua, ni pastillas).", R.drawable.ic_pastillas), // 3
        Recomendacion(0, "RCP Básico", "Si la persona no responde y no respira, inicie compresiones torácicas fuertes y rápidas.", R.drawable.ic_corazon), // 4
        Recomendacion(0, "Hemostasia Directa", "Haga presión fuerte y sostenida sobre la herida con tela limpia para frenar el sangrado.", R.drawable.ic_sangrado), // 5
        Recomendacion(0, "Inmovilizar Objeto", "Si hay cuchillo o cristal incrustado, NO lo retire. Fíjelo para que no se mueva.", R.drawable.ic_escudo), // 6
        Recomendacion(0, "Torniquete de Salvamento", "Solo si la presión directa falla y el sangrado en extremidad es masivo, use un torniquete alto y ajustado.", R.drawable.ic_sangrado), // 7
        Recomendacion(0, "Lavado Antiséptico", "Limpie los bordes de la herida con abundante agua a chorro y jabón neutro.", R.drawable.ic_info), // 8
        Recomendacion(0, "Elevación de Miembro", "Eleve el brazo o pierna sangrante por encima del nivel del corazón.", R.drawable.ic_prim_auxilios), // 9
        Recomendacion(0, "Irrigación Térmica", "Vierta agua a temperatura ambiente (nunca hielo) sobre la quemadura por al menos 15 minutos.", R.drawable.ic_prim_auxilios), // 10
        Recomendacion(0, "Retirar Restricciones", "Quite anillos, pulseras o ropa ajustada cerca de la quemadura antes de que se inflame.", R.drawable.ic_info), // 11
        Recomendacion(0, "Protección Estéril", "Cubra la zona afectada con film plástico transparente o gasa limpia sin presionar.", R.drawable.ic_escudo), // 12
        Recomendacion(0, "Preservar Flictenas", "Prohibido reventar las ampollas; son una barrera natural contra infecciones.", R.drawable.ic_info), // 13
        Recomendacion(0, "Crioterapia Local", "Aplique hielo envuelto en tela por periodos de 15 minutos para reducir el hematoma.", R.drawable.ic_corazon), // 14
        Recomendacion(0, "Férula Improvisada", "Ante sospecha de fractura, inmovilice la zona usando cartón o tablas fijadas con vendas.", R.drawable.ic_escudo), // 15
        Recomendacion(0, "Limpieza por Fricción Suave", "Limpie los raspones arrastrando suavemente la suciedad para evitar tatuaje traumático.", R.drawable.ic_info), // 16
        Recomendacion(0, "Fomento Tibio Salino", "Sumerja la zona del uñero en agua tibia con sal por 15 minutos para ablandar el tejido.", R.drawable.ic_info), // 17
        Recomendacion(0, "Evitar Manipulación", "No intente cortar la uña infectada con cortaúñas caseros. Requiere material esterilizado.", R.drawable.ic_escudo), // 18
        Recomendacion(0, "Control Térmico Físico", "Use paños tibios en zonas de pliegues (axilas, ingles, frente) para bajar la fiebre.", R.drawable.ic_corazon), // 19
        Recomendacion(0, "Vigilancia de Signos de Alarma", "Acuda urgente si hay rigidez de nuca, manchas púrpuras en la piel o letargo extremo.", R.drawable.ic_info), // 20
        Recomendacion(0, "Auto-Inyector de Epinefrina", "En caso de alergia conocida y asfixia, asista al paciente a usar su inyector si lo posee.", R.drawable.ic_pastillas), // 21
        Recomendacion(0, "Aislamiento del Tóxico", "Lleve el envase del producto ingerido al hospital para el antídoto exacto.", R.drawable.ic_info), // 22
        Recomendacion(0, "Contraindicación de Vómito", "En ingesta de ácidos, lejía o hidrocarburos, está prohibido inducir el vómito.", R.drawable.ic_pastillas), // 23
        Recomendacion(0, "Extracción por Raspado", "Saque el aguijón de abeja raspando con una tarjeta; no lo pellizque con pinzas.", R.drawable.ic_info), // 24
        Recomendacion(0, "Posición Materna", "Coloque a la gestante recostada sobre su lado izquierdo para oxigenar al bebé.", R.drawable.ic_escudo), // 25
        Recomendacion(0, "Evitar Hipotermia", "En adultos mayores y niños pequeños, cubra con mantas para evitar pérdida de calor.", R.drawable.ic_corazon), // 26
        Recomendacion(0, "Cero Automedicación", "No administre analgésicos; pueden ocultar el cuadro clínico y retrasar el diagnóstico real.", R.drawable.ic_pastillas), // 27
        Recomendacion(0, "Traslado Asistido", "Diríjase a la Sala de Urgencias del Hospital de Mayor Nivel resolutivo disponible.", R.drawable.ic_hospital), // 28
        Recomendacion(0, "Agenda por Consultorio", "Programe una cita por Consulta Externa (Medicina General). No es una emergencia vital.", R.drawable.ic_hospital) // 29
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
                r.equals("Sí", ignoreCase = true)
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
                listOf(17, 18).forEach { if (c.recomendacionesIndices.contains(it)) score -= 600.0 } // Adiós Uñero (AQUÍ ESTABA TU ERROR ANTES)
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
        val recs = (0 until BANCO_RECOMENDACIONES.size).shuffled().take(4) // TAMAÑO 4
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
        var nuevoGen = Random.nextInt(BANCO_RECOMENDACIONES.size)
        while (recs.contains(nuevoGen)) { nuevoGen = Random.nextInt(BANCO_RECOMENDACIONES.size) }
        recs[idx] = nuevoGen

        val prio = if (Random.nextDouble() < 0.2) Prioridad.values().random() else c.prioridad
        val diag = if (Random.nextDouble() < 0.2) Random.nextInt(BANCO_RESULTADO_POSIBLE.size) else c.diagnosticoIndex

        return Cromosoma(prio, diag, recs, fitnessCalculado = false)
    }

    private fun construirResultadoFinal(c: Cromosoma, context: DecisionContext): ResultadoEvolutivo {
        val diagnostico = BANCO_RESULTADO_POSIBLE.getOrElse(c.diagnosticoIndex) { "Evaluación general clínica" }
        val explicacionMedica = redactarJustificacionClinica(c, context)

        val recomendaciones = c.recomendacionesIndices.mapIndexed { index, recIdx ->
            val plantilla = BANCO_RECOMENDACIONES[recIdx]
            Recomendacion(index + 1, plantilla.titulo, plantilla.recomendacion, plantilla.iconResId)
        }

        return ResultadoEvolutivo(c.prioridad, diagnostico, explicacionMedica, recomendaciones)
    }

    private fun redactarJustificacionClinica(c: Cromosoma, context: DecisionContext): String {
        val sb = StringBuilder("Cuadro clínico derivado por el motor de triaje. ")

        val sintomas = context.sintomas.joinToString(", ") { it.sin_description.lowercase() }
        if (sintomas.isNotEmpty()) {
            sb.append("El paciente manifiesta $sintomas. ")
        }

        if (!context.claseIA.isNullOrEmpty() && context.claseIA != "Error" && context.claseIA != "No se pudo determinar") {
            sb.append("El análisis visual por IA detectó características compatibles con [${context.claseIA}]. ")
        }

        if (context.tipoPaciente != "ADULTO") {
            val pacienteF = context.tipoPaciente.lowercase().replace("_", " ")
            sb.append("Se han aplicado protocolos de protección debido a la condición de vulnerabilidad ($pacienteF). ")
        }

        val consciente = context.respuestasBasicas.getOrElse(0) { true }
        val respira = context.respuestasBasicas.getOrElse(1) { true }
        val sangra = context.respuestasBasicas.getOrElse(2) { false }
        val esCritico = !consciente || !respira || sangra

        if (esCritico) {
            val alarmasClinicas = mutableListOf<String>()
            if (!consciente) alarmasClinicas.add("pérdida de consciencia")
            if (!respira) alarmasClinicas.add("alteración respiratoria severa o apnea")
            if (sangra) alarmasClinicas.add("hemorragia activa grave")

            sb.append("Alerta crítica detectada por: ${alarmasClinicas.joinToString(" y ")}. ")
            sb.append("Se asigna Prioridad ${c.prioridad.nombre} por compromiso inminente de vida. Requiere soporte vital inmediato.")
        } else {
            val graves = context.preguntasDinamicas.count { (it.options.getOrNull(it.selectedOptionIndex) ?: "") == "Sí" }
            sb.append("Se determina nivel de riesgo ${c.prioridad.nombre} al evaluar $graves factor(es) agravante(s) en la anamnesis.")
        }

        return sb.toString()
    }
}