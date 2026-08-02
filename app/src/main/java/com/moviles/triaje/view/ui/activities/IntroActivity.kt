package com.moviles.triaje.view.ui.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.auth.FirebaseAuth
import com.moviles.triaje.R
import com.moviles.triaje.utils.PreferenceManager
import java.util.Locale

class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefManager = PreferenceManager(this)

        // 1. Aplicar Idioma ANTES de super.onCreate
        setAppLocale(prefManager.language)

        // 2. Aplicar Tema ANTES de super.onCreate
        if (prefManager.isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

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

        // Verificamos si venimos de un Cierre de Sesión de forma segura
        try {
            val startAtLogin = intent.getBooleanExtra("START_AT_LOGIN", false)
            if (startAtLogin) {
                val navHostFragment = supportFragmentManager
                    .findFragmentById(R.id.fcvIntroContainer) as? NavHostFragment
                navHostFragment?.navController?.navigate(R.id.loginFragment)
            }
        } catch (e: Exception) {
            Log.e("INTRO_DEBUG", "Error al intentar navegar al Login: ${e.message}")
        }
    }

    private fun setAppLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
