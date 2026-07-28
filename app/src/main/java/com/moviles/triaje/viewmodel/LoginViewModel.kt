package com.moviles.triaje.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.moviles.triaje.model.Usuario
import com.moviles.triaje.network.Callback
import com.moviles.triaje.network.FirestoreService

class LoginViewModel : ViewModel() {

    private val firestoreService = FirestoreService()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    sealed class LoginState {
        object Loading : LoginState()
        data class Success(val nombreUsuario: String) : LoginState()
        data class Error(val mensaje: String) : LoginState()
    }

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> get() = _loginState

    private val _isButtonEnabled = MutableLiveData<Boolean>()
    val isButtonEnabled: LiveData<Boolean> get() = _isButtonEnabled

    fun verificarCampos(email: String, password: String) {
        _isButtonEnabled.value = email.isNotEmpty() && password.isNotEmpty()
    }

    fun autenticarUsuario(emailInput: String, passwordInput: String) {
        if (emailInput.isEmpty() || passwordInput.isEmpty()) {
            _loginState.value = LoginState.Error("Por favor, completa todos los campos")
            return
        }

        _loginState.value = LoginState.Loading

        // 🔥 MODIFICADO: Usamos Firebase Authentication para iniciar sesión de manera segura
        firebaseAuth.signInWithEmailAndPassword(emailInput, passwordInput)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = firebaseAuth.currentUser

                    // Forzar recarga del estado del usuario para actualizar la propiedad "isEmailVerified"
                    firebaseUser?.reload()?.addOnCompleteListener { reloadTask ->
                        if (firebaseUser != null && firebaseUser.isEmailVerified) {

                            // 🔥 OBTENER NOMBRE: Ya logueado, traemos su nombre desde Firestore usando su UID único
                            db.collection("usuarios").document(firebaseUser.uid).get()
                                .addOnSuccessListener { document ->
                                    if (document != null && document.exists()) {
                                        val nombre = document.getString("us_nombre") ?: "Usuario"
                                        _loginState.value = LoginState.Success(nombre)
                                    } else {
                                        _loginState.value = LoginState.Error("No se encontraron datos de perfil.")
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    _loginState.value = LoginState.Error("Error de sincronización: ${exception.message}")
                                }

                        } else {
                            // Si el login fue correcto pero NO ha verificado su correo electrónico
                            firebaseAuth.signOut() // Deslogueamos de inmediato por seguridad
                            _loginState.value = LoginState.Error("Debes verificar tu cuenta. Revisa el correo electrónico que te enviamos.")
                        }
                    }
                } else {
                    // Contraseña incorrecta, formato inválido o correo inexistente
                    _loginState.value = LoginState.Error(task.exception?.message ?: "Error al iniciar sesión. Verifica tus datos.")
                }
            }
    }

    // 🔥 NUEVO: Función para re-enviar correo de verificación en caso el usuario no lo haya recibido
    fun reenviarCorreoDeVerificacion(email: String, pass: String, callback: Callback<String>) {
        firebaseAuth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    user?.sendEmailVerification()?.addOnCompleteListener { emailTask ->
                        firebaseAuth.signOut() // Lo desconectamos de inmediato
                        if (emailTask.isSuccessful) {
                            callback.onSuccess("Correo de verificación reenviado con éxito.")
                        } else {
                            callback.onFailed(Exception("Error al reenviar: ${emailTask.exception?.message}"))
                        }
                    }
                } else {
                    callback.onFailed(Exception("Credenciales incorrectas."))
                }
            }
    }
}