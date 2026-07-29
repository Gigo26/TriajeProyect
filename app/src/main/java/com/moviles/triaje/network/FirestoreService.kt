package com.moviles.triaje.network

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.moviles.triaje.model.Question
import com.moviles.triaje.model.Usuario
import com.moviles.triaje.model.Sintoma

class FirestoreService {

    private val firebaseFirestore = FirebaseFirestore.getInstance()

    private val settings = FirebaseFirestoreSettings.Builder()
        .setPersistenceEnabled(true)
        .build()

    init {
        firebaseFirestore.firestoreSettings = settings
    }

    // Consulta de autenticación
    fun buscarUsuarioPorEmail(email: String, callback: Callback<Usuario>) {
        firebaseFirestore.collection("usuarios")
            .whereEqualTo("us_email", email)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val usuario = result.documents[0].toObject(Usuario::class.java)
                    callback.onSuccess(usuario)
                } else {
                    callback.onSuccess(null)
                }
            }
            .addOnFailureListener { exception ->
                callback.onFailed(exception)
            }
    }

    fun obtenerUsuarioPorUid(uid: String, callback: Callback<Usuario>) {
        firebaseFirestore.collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { result ->
                if (result.exists()) {
                    val usuario = result.toObject(Usuario::class.java)
                    callback.onSuccess(usuario)
                } else {
                    callback.onSuccess(null)
                }
            }
            .addOnFailureListener { exception ->
                callback.onFailed(exception)
            }
    }

    // NUEVO: Consulta para validar unicidad de DNI
    fun verificarDniExistente(dni: String, callback: Callback<Boolean>) {
        firebaseFirestore.collection("usuarios")
            .whereEqualTo("us_dni", dni)
            .get()
            .addOnSuccessListener { result ->
                callback.onSuccess(!result.isEmpty) // Retorna true si ya existe
            }
            .addOnFailureListener { exception ->
                callback.onFailed(exception)
            }
    }

    // MODIFICADO: Ahora recibe el UID seguro de Firebase Auth y guarda los datos en él
    fun registrarNuevoUsuario(uid: String, usuario: Usuario, callback: Callback<String>) {
        firebaseFirestore.collection("usuarios")
            .document(uid)
            .set(usuario)
            .addOnSuccessListener {
                callback.onSuccess(uid)
            }
            .addOnFailureListener { exception ->
                callback.onFailed(exception)
            }
    }

    fun obtenerSintomas(callback: Callback<List<Sintoma>>) {
        firebaseFirestore.collection("sintomas")
            .get()
            .addOnSuccessListener { result ->
                val listaSintomas = mutableListOf<Sintoma>()
                for (document in result) {
                    val sintoma = document.toObject(Sintoma::class.java).copy(id = document.id)
                    listaSintomas.add(sintoma)
                }
                callback.onSuccess(listaSintomas)
            }
            .addOnFailureListener { exception ->
                callback.onFailed(exception)
            }
    }

    fun obtenerPreguntasPorSintoma(sintomaId: String, callback: Callback<List<Question>>) {
        firebaseFirestore.collection("sintomas")
            .document(sintomaId)
            .collection("preguntas")
            .get()
            .addOnSuccessListener { result ->
                val listaPreguntas = mutableListOf<Question>()
                var idContador = 1

                for (document in result) {
                    // Mapeamos manualmente los campos de Firestore a tu modelo Question
                    val textoPregunta = document.getString("pr_pregunta") ?: ""
                    @Suppress("UNCHECKED_CAST")
                    val opciones = document.get("valores_opciones") as? List<String> ?: emptyList()

                    val pregunta = Question(
                        id = idContador++, // ID numérico incremental para el control del RadioGroup
                        text = textoPregunta,
                        options = opciones
                    )
                    listaPreguntas.add(pregunta)
                }
                callback.onSuccess(listaPreguntas)
            }
            .addOnFailureListener { exception ->
                callback.onFailed(exception)
            }
    }

    // NUEVO: Obtener utilidades comunitarias (versión, políticas, términos)
    fun obtenerUtilidadesComunitarias(callback: Callback<Map<String, Any>>) {
        firebaseFirestore.collection("configuracion_app")
            .document("utilidades_comunitarias")
            .get()
            .addOnSuccessListener { result ->
                if (result != null && result.exists()) {
                    // Retornamos todos los campos del documento como un Mapa
                    callback.onSuccess(result.data ?: emptyMap())
                } else {
                    callback.onSuccess(emptyMap())
                }
            }
            .addOnFailureListener { exception ->
                callback.onFailed(exception)
            }
    }

    // ACTUALIZAR AVATAR: Guarda el link de la foto en el documento del usuario
    fun actualizarAvatar(uid: String, avatarUrl: String, callback: Callback<Boolean>) {
        firebaseFirestore.collection("usuarios")
            .document(uid)
            .update("us_avatar", avatarUrl)
            .addOnSuccessListener {
                callback.onSuccess(true)
            }
            .addOnFailureListener { exception ->
                callback.onFailed(exception)
            }
    }

    /**
     * Procesa un objeto Usuario para devolver solo el primer nombre y el primer apellido
     * en formato Tipo Título (ej: AYRTON PALOMINO -> Ayrton Palomino).
     */
    fun obtenerNombreFormateado(usuario: Usuario): String {
        val primerNombreCrudo = usuario.us_nombre.trim().split(" ").firstOrNull() ?: ""
        val primerApellidoCrudo = usuario.us_apellidos.trim().split(" ").firstOrNull() ?: ""

        // Convertir a minúsculas y capitalizar la primera letra
        val nombreFormateado = primerNombreCrudo.lowercase().replaceFirstChar { it.uppercase() }
        val apellidoFormateado = primerApellidoCrudo.lowercase().replaceFirstChar { it.uppercase() }

        return "$nombreFormateado $apellidoFormateado".trim()
    }

    /**
     * Devuelve el primer nombre y todos los apellidos formateados en Tipo Título.
     * Ejemplo: "AYRTON", "PALOMINO TOTIMURA" -> "Ayrton Palomino Totimura"
     */
    fun obtenerNombreCompletoFormateado(usuario: Usuario): String {
        val primerNombreCrudo = usuario.us_nombre.trim().split(" ").firstOrNull() ?: ""
        val apellidosCrudos = usuario.us_apellidos.trim()

        val nombreFormateado = primerNombreCrudo.lowercase().replaceFirstChar { it.uppercase() }
        
        // Formatear cada palabra de los apellidos
        val apellidosFormateados = apellidosCrudos.split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { it.uppercase() }
            }

        return "$nombreFormateado $apellidosFormateados".trim()
    }
}
