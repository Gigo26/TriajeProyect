package com.moviles.triaje.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.moviles.triaje.model.Usuario
import com.moviles.triaje.network.Callback
import com.moviles.triaje.network.FirestoreService

class RegisterViewModel : ViewModel() {

    private val firestoreService = FirestoreService()
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _isButtonEnabled = MutableLiveData<Boolean>()
    val isButtonEnabled: LiveData<Boolean> get() = _isButtonEnabled

    private val _registerResult = MutableLiveData<String>()
    val registerResult: LiveData<String> get() = _registerResult

    // Evalúa si todos los inputs son coherentes para habilitar el botón "Registrarse"
    fun verificarCampos(name: String, dni: String, email: String, pass: String, confirmPass: String) {
        val isValid = name.isNotBlank() &&
                dni.length == 8 &&
                email.contains("@") && email.contains(".") &&
                pass.length >= 8 &&
                pass == confirmPass
        _isButtonEnabled.value = isValid
    }

    // Orquesta la creación del usuario con DNI único y verificación de correo
    fun registrarUsuarioConVerificacion(
        nombre: String,
        apellidos: String,
        dni: String,
        email: String,
        pass: String,
        confirmPass: String
    ) {
        if (pass != confirmPass) {
            _registerResult.value = "Las contraseñas no coinciden."
            return
        }

        // Paso 1: Verificar en Firestore si ese DNI ya está registrado por otra persona
        firestoreService.verificarDniExistente(dni, object : Callback<Boolean> {
            override fun onSuccess(result: Boolean?) {
                if (result == true) {
                    _registerResult.value = "Este DNI ya se encuentra asociado a una cuenta."
                } else {
                    // Paso 2: Crear el usuario en Firebase Authentication (Seguro)
                    crearCuentaEnFirebaseAuth(nombre, apellidos, dni, email, pass)
                }
            }

            override fun onFailed(exception: Exception) {
                _registerResult.value = "Error al validar DNI: ${exception.message}"
            }
        })
    }

    private fun crearCuentaEnFirebaseAuth(
        nombre: String,
        apellidos: String,
        dni: String,
        email: String,
        pass: String
    ) {
        firebaseAuth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = firebaseAuth.currentUser

                    // Paso 3: Enviar el correo electrónico de verificación al usuario
                    firebaseUser?.sendEmailVerification()
                        ?.addOnCompleteListener { emailTask ->
                            if (emailTask.isSuccessful) {

                                // Paso 4: Preparar el objeto Usuario (Sin contraseña) para Firestore
                                val nuevoUsuario = Usuario(
                                    us_nombre = nombre,
                                    us_apellidos = apellidos,
                                    us_dni = dni,
                                    us_email = email,
                                    us_celular = null,
                                    us_fecha_nac = null
                                )

                                // Paso 5: Guardar en Firestore usando el UID del usuario como ID del documento
                                firestoreService.registrarNuevoUsuario(
                                    firebaseUser.uid,
                                    nuevoUsuario,
                                    object : Callback<String> {
                                        override fun onSuccess(result: String?) {
                                            // Cerramos la sesión por el momento para que no entre a la app
                                            // hasta que verifique su correo en la pantalla de Login
                                            firebaseAuth.signOut()
                                            _registerResult.value = "EXITO"
                                        }

                                        override fun onFailed(exception: Exception) {
                                            _registerResult.value = "Se creó la cuenta pero hubo un error al guardar tus datos: ${exception.message}"
                                        }
                                    }
                                )
                            } else {
                                _registerResult.value = "No se pudo enviar el correo de confirmación: ${emailTask.exception?.message}"
                            }
                        }
                } else {
                    _registerResult.value = "Error al registrar la cuenta: ${task.exception?.message}"
                }
            }
    }
}