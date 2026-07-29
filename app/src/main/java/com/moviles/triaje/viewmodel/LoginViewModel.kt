package com.moviles.triaje.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.moviles.triaje.network.Callback
import com.moviles.triaje.network.FirestoreService

class LoginViewModel : ViewModel() {

    private val firestoreService = FirestoreService()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Variable para rastrear los intentos fallidos
    private var intentosFallidos = 0

    sealed class LoginState {
        object Loading : LoginState()
        data class Success(val nombreUsuario: String) : LoginState()
        data class Error(val mensaje: String) : LoginState()
        // NUEVO: Estado específico para lanzar la recuperación de contraseña
        object RequirePasswordRecovery : LoginState()
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

        firebaseAuth.signInWithEmailAndPassword(emailInput, passwordInput)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = firebaseAuth.currentUser

                    firebaseUser?.reload()?.addOnCompleteListener { reloadTask ->
                        if (firebaseUser != null && firebaseUser.isEmailVerified) {

                            // Si entra con éxito, reiniciamos el contador a 0
                            intentosFallidos = 0

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
                            firebaseAuth.signOut()
                            _loginState.value = LoginState.Error("Debes verificar tu cuenta. Revisa el correo electrónico que te enviamos.")
                        }
                    }
                } else {
                    // Manejo de errores e intentos fallidos
                    val errorMessage = task.exception?.message ?: ""

                    // Si el error es por credenciales incorrectas, sumamos un intento
                    if (errorMessage.contains("incorrect", ignoreCase = true) ||
                        errorMessage.contains("invalid-credential", ignoreCase = true) ||
                        errorMessage.contains("wrong-password", ignoreCase = true)
                    ) {
                        intentosFallidos++

                        if (intentosFallidos >= 3) {
                            // Alcanzó el límite: Disparamos el estado de recuperación y reseteamos el contador
                            _loginState.value = LoginState.RequirePasswordRecovery
                            intentosFallidos = 0
                        } else {
                            // Le avisamos cuántos intentos le quedan
                            _loginState.value = LoginState.Error("Credenciales incorrectas. Intento $intentosFallidos de 3.")
                        }
                    } else {
                        // Si es otro tipo de error (ej. sin internet), no lo contamos como intento fallido
                        _loginState.value = LoginState.Error("Error al iniciar sesión. Verifica tus datos.")
                    }
                }
            }
    }

    fun reenviarCorreoDeVerificacion(email: String, pass: String, callback: Callback<String>) {
        firebaseAuth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    user?.sendEmailVerification()?.addOnCompleteListener { emailTask ->
                        firebaseAuth.signOut()
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