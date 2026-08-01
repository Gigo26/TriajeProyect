package com.moviles.triaje.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.moviles.triaje.model.Usuario
import com.moviles.triaje.network.Callback
import com.moviles.triaje.network.FirestoreService

class PerfilViewModel : ViewModel() {

    private val firestoreService = FirestoreService()
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _usuario = MutableLiveData<Usuario?>()
    val usuario: LiveData<Usuario?> get() = _usuario

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun cargarDatosUsuario() {
        val currentUser = firebaseAuth.currentUser
        Log.d("AVATAR_SYNC", "Perfil - Cargando datos. Usuario Auth: ${currentUser?.email}")

        if (currentUser != null) {
            firestoreService.obtenerUsuarioPorUid(currentUser.uid, object : Callback<Usuario> {
                override fun onSuccess(result: Usuario?) {
                    _usuario.value = result
                    Log.d("AVATAR_SYNC", "Perfil - Usuario Firestore cargado. us_avatar: ${result?.us_avatar}")

                    if (result != null && result.us_avatar.isNullOrEmpty()) {
                        currentUser.photoUrl?.let { uri ->
                            Log.d("AVATAR_SYNC", "Perfil - Sincronizando avatar desde Auth: $uri")
                            sincronizarAvatar(currentUser.uid, uri.toString())
                        } ?: Log.d("AVATAR_SYNC", "Perfil - El usuario no tiene photoUrl en Firebase Auth")
                    }
                }

                override fun onFailed(exception: Exception) {
                    _error.value = "Error al cargar datos: ${exception.message}"
                    Log.e("AVATAR_SYNC", "Perfil - Error al cargar usuario", exception)
                }
            })
        } else {
            _error.value = "Sesión no iniciada"
        }
    }

    private fun sincronizarAvatar(uid: String, url: String) {
        firestoreService.actualizarAvatar(uid, url, object : Callback<Boolean> {
            override fun onSuccess(result: Boolean?) {
                Log.d("AVATAR_SYNC", "Perfil - ¡Avatar sincronizado exitosamente!")
            }
            override fun onFailed(exception: Exception) {
                Log.e("AVATAR_SYNC", "Perfil - Error al sincronizar", exception)
            }
        })
    }

    fun getNombreCompletoFormateado(usuario: Usuario): String {
        return firestoreService.obtenerNombreCompletoFormateado(usuario)
    }

    fun getAvatarUrl(usuario: Usuario?): String {
        // 1. Prioridad: Avatar en Firestore (puede ser Base64 o URL)
        val firestorePhoto = usuario?.us_avatar
        if (!firestorePhoto.isNullOrEmpty()) return firestorePhoto

        // 2. Foto de Firebase Auth (Google/etc)
        val authPhoto = firebaseAuth.currentUser?.photoUrl?.toString()
        if (!authPhoto.isNullOrEmpty()) return authPhoto

        // 3. Fallback: Iniciales
        val nombre = usuario?.us_nombre ?: "U"
        val apellidos = usuario?.us_apellidos ?: ""
        val nombreCompleto = "$nombre $apellidos".trim().replace(" ", "+")

        return "https://ui-avatars.com/api/?name=$nombreCompleto&background=1E60D5&color=fff&size=128&bold=true"
    }

    fun actualizarPerfil(
        dni: String,
        nombre: String,
        apellidos: String,
        celular: String,
        fechaNac: java.util.Date?,
        avatarBase64: String?,
        callback: Callback<Boolean>
    ) {
        val uid = firebaseAuth.currentUser?.uid ?: return
        val updates = mutableMapOf<String, Any>(
            "us_dni" to dni,
            "us_nombre" to nombre,
            "us_apellidos" to apellidos,
            "us_celular" to celular
        )
        
        fechaNac?.let { updates["us_fecha_nac"] = it }
        avatarBase64?.let { updates["us_avatar"] = it }

        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(uid)
            .update(updates)
            .addOnSuccessListener { callback.onSuccess(true) }
            .addOnFailureListener { callback.onFailed(it) }
    }

    fun cerrarSesion() {
        firebaseAuth.signOut()
    }
}