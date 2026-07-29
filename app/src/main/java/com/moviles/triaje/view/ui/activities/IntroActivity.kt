package com.moviles.triaje.view.ui.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.auth.FirebaseAuth
import com.moviles.triaje.R

class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        //  1. Forzar tema claro ANTES de super.onCreate
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        // =======================================================
        // 🔥 PRUEBA DE CONEXIÓN RÁPIDA A FIREBASE
        // =======================================================
        try {
            val auth = FirebaseAuth.getInstance()
            Log.d("FIREBASE_TEST", "¡Conexión exitosa! Instancia de Auth obtenida: ${auth.app.name}")
        } catch (e: Exception) {
            Log.e("FIREBASE_TEST", "Error en la conexión a Firebase: ${e.message}")
        }
        // =======================================================

        // Verificamos si venimos de un Cierre de Sesión
        val startAtLogin = intent.getBooleanExtra("START_AT_LOGIN", false)
        if (startAtLogin) {
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.fcvIntroContainer) as? NavHostFragment
            navHostFragment?.navController?.navigate(R.id.loginFragment)
        }
    }
}