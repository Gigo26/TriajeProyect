package com.moviles.triaje.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.moviles.triaje.network.Callback

class ForgotPasswordViewModel : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()

    fun enviarCorreoRecuperacion(email: String, callback: Callback<String>) {
        if (email.isEmpty()) {
            callback.onFailed(Exception("Por favor, ingresa tu correo para recuperar la contraseña."))
            return
        }

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback.onSuccess("Te hemos enviado un enlace de recuperación. Revisa tu correo.")
                } else {
                    callback.onFailed(Exception("Error al enviar el correo: ${task.exception?.message}"))
                }
            }
    }
}