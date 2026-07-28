package com.moviles.triaje.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.moviles.triaje.model.Usuario
import com.moviles.triaje.network.Callback
import com.moviles.triaje.network.FirestoreService

class VerInformacionViewModel : ViewModel() {

    private val firestoreService = FirestoreService()
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _usuario = MutableLiveData<Usuario?>()
    val usuario: LiveData<Usuario?> get() = _usuario

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun cargarDatosUsuario() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            firestoreService.obtenerUsuarioPorUid(currentUser.uid, object : Callback<Usuario> {
                override fun onSuccess(result: Usuario?) {
                    _usuario.value = result
                }

                override fun onFailed(exception: Exception) {
                    _error.value = "Error al cargar datos: ${exception.message}"
                }
            })
        } else {
            _error.value = "No hay usuario autenticado"
        }
    }

    fun getPhotoUrl(): String? {
        return firebaseAuth.currentUser?.photoUrl?.toString()
    }
}