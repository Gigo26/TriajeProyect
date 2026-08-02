package com.moviles.triaje.utils

import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

object DatabaseSeeder {

    private val db = FirebaseFirestore.getInstance()

    fun sembrarBaseDeDatos(onComplete: (Boolean) -> Unit) {
        val batch = db.batch()

        // Definición de los 13 síntomas con sus IDs y configuraciones
        val mapaSintomas = mapOf(
            "dolor_pecho" to DataSintoma("Dolor de Pecho", "ic_dolor_pecho", false, listOf(
                PreguntaData(1, "¿El dolor se irradia al brazo izquierdo, mandíbula o cuello?", listOf("Sí", "No")),
                PreguntaData(2, "¿Viene acompañado de sudor frío, mareo o sudoración profusa?", listOf("Sí", "No")),
                PreguntaData(3, "¿Cómo describe la intensidad y tipo de dolor?", listOf("Opresivo / Punzada fuerte", "Leve o tipo hincada", "Malestar difuso"))
            )),
            "dificultad_respiratoria" to DataSintoma("Dificultad para respirar", "ic_dificultad_respiratoria", false, listOf(
                PreguntaData(1, "¿Presenta hundimiento de costillas, silbido al respirar o labios morados?", listOf("Sí", "No")),
                PreguntaData(2, "¿La dificultad le impide hablar frases completas o mantenerse de pie?", listOf("Sí", "No")),
                PreguntaData(3, "¿En qué momento o postura empeora la respiración?", listOf("En reposo o acostado", "Solo al hacer esfuerzo", "Es constante e invariable"))
            )),
            "perdida_conocimiento" to DataSintoma("Pérdida de conocimiento", "ic_perdida_conocimiento", false, listOf(
                PreguntaData(1, "¿El desmayo duró más de 1 minuto o demoró en responder al despertar?", listOf("Sí", "No")),
                PreguntaData(2, "¿Sufrió un golpe de cabeza al caer o relajó esfínteres?", listOf("Sí", "No")),
                PreguntaData(3, "¿Cómo se encuentra la persona en este momento?", listOf("Desorientada o somnolienta", "Totalmente consciente", "Aún no responde del todo"))
            )),
            "convulsiones" to DataSintoma("Convulsiones", "ic_convulsiones", false, listOf(
                PreguntaData(1, "¿La convulsión duró más de 5 minutos o se repitió seguidamente?", listOf("Sí", "No")),
                PreguntaData(2, "¿Ocurrió durante un episodio de fiebre alta o trauma?", listOf("Sí", "No")),
                PreguntaData(3, "¿Qué manifestación física presentó principalmente?", listOf("Rigidez y espasmos generales", "Mirada perdida / Ausencia", "Movimiento involuntario focalizado"))
            )),
            "herida_abierta" to DataSintoma("Herida abierta", "ic_herida_abierta", true, listOf(
                PreguntaData(1, "¿La sangre brota a borbotones o no cesa tras aplicar presión por 5 min?", listOf("Sí", "No")),
                PreguntaData(2, "¿La herida deja ver tejido profundo, grasa, hueso o está muy contaminada?", listOf("Sí", "No")),
                PreguntaData(3, "¿En qué zona del cuerpo se ubica la lesión principal?", listOf("Rostro, cuello o genitales", "Tórax o abdomen", "Extremidades (brazos/piernas)"))
            )),
            "quemadura" to DataSintoma("Quemadura", "ic_quemadura", true, listOf(
                PreguntaData(1, "¿Afecta rostro, cuello, manos, articulaciones o zona genital?", listOf("Sí", "No")),
                PreguntaData(2, "¿Fue causada por químicos, electricidad, fuego directo o vapor denso?", listOf("Sí", "No")),
                PreguntaData(3, "¿Cuál es el aspecto visible predominante en la piel?", listOf("Piel carbonizada / Blanquecina", "Ampollas llenas de líquido", "Enrojecimiento y ardor superficial"))
            )),
            "golpe_fuerte" to DataSintoma("Golpe fuerte o caída", "ic_golpe_fuerte", true, listOf(
                PreguntaData(1, "¿Hubo pérdida del conocimiento, vómitos explosivos o sangrado por oído/nariz?", listOf("Sí", "No")),
                PreguntaData(2, "¿La caída fue de una altura superior a su propia estatura o impacto a alta velocidad?", listOf("Sí", "No")),
                PreguntaData(3, "¿Qué síntoma predomina tras el impacto?", listOf("Dolor de cabeza intenso o mareo", "Incapacidad para mover extremidad", "Solo dolor localizado / Hematoma"))
            )),
            "posible_fractura" to DataSintoma("Posible fractura", "ic_posible_fractura", true, listOf(
                PreguntaData(1, "¿Existe deformidad evidente del hueso o el hueso traspasó la piel?", listOf("Sí", "No")),
                PreguntaData(2, "¿La zona afectada luce pálida, fría o no siente los dedos?", listOf("Sí", "No")),
                PreguntaData(3, "¿Qué nivel de movilidad presenta en la zona golpeada?", listOf("Imposibilidad total de apoyar/mover", "Movimiento con dolor tolerable", "Movilidad normal pero sensible"))
            )),
            "dolor_abdominal" to DataSintoma("Dolor abdominal intenso", "ic_dolor_abdominal", false, listOf(
                PreguntaData(1, "¿El abdomen está duro como tabla, sensible al mínimo tacto o cursa con sangrado?", listOf("Sí", "No")),
                PreguntaData(2, "¿Presenta vómitos persistentes que impiden tolerar líquidos o fiebre alta?", listOf("Sí", "No")),
                PreguntaData(3, "¿En qué región del abdomen se concentra el dolor?", listOf("Boca del estómago / Difuso", "Lado inferior derecho", "Región pélvica / Bajo vientre"))
            )),
            "fiebre_alta" to DataSintoma("Fiebre alta", "ic_fiebre_alta", false, listOf(
                PreguntaData(1, "¿Muestra somnolencia extrema, rigidez en el cuello o manchas rojas/moradas en la piel?", listOf("Sí", "No")),
                PreguntaData(2, "En caso de Niños (0-12): ¿Presenta llanto inconsolable o rechazo total a líquidos?", listOf("Sí", "No")),
                PreguntaData(3, "¿A cuánto asciende la temperatura corporal medida?", listOf("Mayor o igual a 39°C", "Entre 38°C y 38.9°C", "Sensación febril (Sin termómetro)"))
            )),
            "reaccion_alergica" to DataSintoma("Reacción alérgica", "ic_reaccion_alergica", false, listOf(
                PreguntaData(1, "¿Presenta hinchazón en labios, lengua o garganta que dificulta tragar o respirar?", listOf("Sí", "No")),
                PreguntaData(2, "¿Siente mareo intenso, sensación de desmayo o voz ronca repentina?", listOf("Sí", "No")),
                PreguntaData(3, "¿Cómo se manifiesta la reacción en la piel u otro sistema?", listOf("Ronchas / Urticaria generalizada", "Enrojecimiento local e picazón", "Solo estornudos o congestión"))
            )),
            "intoxicacion" to DataSintoma("Intoxicación o envenenamiento", "ic_intoxicacion", false, listOf(
                PreguntaData(1, "¿Presenta alteración de la conciencia, convulsiones o dificultad para respirar?", listOf("Sí", "No")),
                PreguntaData(2, "¿La sustancia ingerida/inhalada fue un químico industrial, medicamento o plaguicida?", listOf("Sí", "No")),
                PreguntaData(3, "¿Vía por la cual ocurrió la exposición al elemento tóxico?", listOf("Ingestión (vía oral)", "Inhalación de gases/humo", "Contacto con piel o mucosas"))
            )),
            "picadura_mordedura" to DataSintoma("Picadura o mordedura", "ic_picadura_mordedura", true, listOf(
                PreguntaData(1, "¿Fue por animal venenoso (serpiente, araña) o animal con sospecha de rabia?", listOf("Sí", "No")),
                PreguntaData(2, "¿Presenta sangrado profuso, adormecimiento de la extremidad o mareo general?", listOf("Sí", "No")),
                PreguntaData(3, "¿Qué estado presenta la zona afectada?", listOf("Hinchazón rápida y cambio de color", "Herida o desgarradura abierta", "Solo dolor y enrojecimiento leve"))
            ))
        )

        mapaSintomas.forEach { (sintomaId, data) ->
            val sintomaRef = db.collection("sintomas").document(sintomaId)

            batch.set(sintomaRef, mapOf(
                "sin_description" to data.description,
                "sin_image" to data.image,
                "sin_requiere_imagen" to data.requiereImagen
            ))

            data.preguntas.forEachIndexed { index, preg ->
                val pregRef = sintomaRef.collection("preguntas").document("q${index + 1}")
                batch.set(pregRef, mapOf(
                    "pr_orden" to preg.orden,
                    "pr_pregunta" to preg.texto,
                    "valores_opciones" to preg.opciones
                ))
            }
        }

        batch.commit()
            .addOnSuccessListener {
                Log.d("SEEDER", "Base de datos sembrada con éxito")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e("SEEDER", "Error sembrando base de datos", e)
                onComplete(false)
            }
    }

    private data class DataSintoma(
        val description: String,
        val image: String,
        val requiereImagen: Boolean,
        val preguntas: List<PreguntaData>
    )

    private data class PreguntaData(
        val orden: Int,
        val texto: String,
        val opciones: List<String>
    )
}