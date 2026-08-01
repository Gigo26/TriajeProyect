package com.moviles.triaje.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.moviles.triaje.model.Usuario
import com.moviles.triaje.network.Callback
import com.moviles.triaje.network.FirestoreService

class MainViewModel : ViewModel() {

    private val firestoreService = FirestoreService()
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _usuario = MutableLiveData<Usuario?>()
    val usuario: LiveData<Usuario?> get() = _usuario

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun cargarDatosUsuario() {
        val currentUser = firebaseAuth.currentUser
        Log.d("AVATAR_SYNC", "Cargando datos. Usuario Auth: ${currentUser?.email}")

        if (currentUser != null) {
            firestoreService.obtenerUsuarioPorUid(currentUser.uid, object : Callback<Usuario> {
                override fun onSuccess(result: Usuario?) {
                    _usuario.value = result
                    Log.d("AVATAR_SYNC", "Usuario Firestore cargado. us_avatar: ${result?.us_avatar}")

                    if (result != null && result.us_avatar.isNullOrEmpty()) {
                        currentUser.photoUrl?.let { uri ->
                            Log.d("AVATAR_SYNC", "Sincronizando avatar desde Auth: $uri")
                            sincronizarAvatar(currentUser.uid, uri.toString())
                        } ?: Log.d("AVATAR_SYNC", "El usuario no tiene photoUrl en Firebase Auth")
                    }
                }

                override fun onFailed(exception: Exception) {
                    _error.value = "Error al cargar datos: ${exception.message}"
                    Log.e("AVATAR_SYNC", "Error al cargar usuario de Firestore", exception)
                }
            })
        } else {
            _error.value = "Sesión no iniciada"
        }
    }

    private fun sincronizarAvatar(uid: String, url: String) {
        firestoreService.actualizarAvatar(uid, url, object : Callback<Boolean> {
            override fun onSuccess(result: Boolean?) {
                Log.d("AVATAR_SYNC", "¡Avatar sincronizado exitosamente en Firestore!")
            }
            override fun onFailed(exception: Exception) {
                Log.e("AVATAR_SYNC", "Error al sincronizar avatar", exception)
            }
        })
    }

    fun getNombreFormateado(usuario: Usuario): String {
        return firestoreService.obtenerNombreFormateado(usuario)
    }

    // MODIFICADO: Genera un avatar con iniciales si el usuario no tiene foto, soporta Base64
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
}