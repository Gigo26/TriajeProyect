package com.moviles.triaje.view.ui.activities

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.moviles.triaje.R
import com.moviles.triaje.utils.PreferenceManager
import com.moviles.triaje.utils.TranslationManager
import com.moviles.triaje.viewmodel.MainViewModel
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefManager = PreferenceManager(this)

        // Aplicar Tema
        if (prefManager.isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // Aplicar Idioma
        setAppLocale(prefManager.language)

        // Inicializar Traductor si el idioma es Inglés
        if (prefManager.language == "en") {
            TranslationManager.init(this) { success ->
                if (success) {
                    android.util.Log.d("TRADUCCION", "Modelo de traducción descargado con éxito")
                } else {
                    android.util.Log.e("TRADUCCION", "Error al descargar el modelo de traducción")
                }
            }
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // 1. Inicializar ViewModel
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // 2. Observar Datos del Usuario
        observarUsuario()

        // 3. Cargar Datos
        viewModel.cargarDatosUsuario()

        //  2. Manejo de Edge-To-Edge para la vista raíz
        val mainView = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Aplicamos padding solo superior para no chochar con la barra de estado
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        //  3. Configuración del Navigation Component con el BottomNavigationView
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_main) as? NavHostFragment

        navHostFragment?.let {
            val navController = it.navController
            val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNav?.setupWithNavController(navController)
        }
    }

    private fun setAppLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun observarUsuario() {
        viewModel.usuario.observe(this) { usuario ->
            usuario?.let {
                val tvWelcome = findViewById<android.widget.TextView>(R.id.tvGreeting)
                tvWelcome.text = getString(R.string.hello_user, viewModel.getNombreFormateado(it))

                // Cargar foto de perfil
                val ivAvatar = findViewById<android.widget.ImageView>(R.id.ivUserAvatar)
                val avatar = viewModel.getAvatarUrl(it)
                
                if (avatar.startsWith("data:image")) {
                    try {
                        val base64String = avatar.substringAfter(",")
                        val imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                        val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        ivAvatar.setImageBitmap(bitmap)
                        // Opcional: Aplicar recorte circular al bitmap si el XML no lo hace
                        // (puedes usar un ShapeableImageView en el XML para facilitar esto)
                    } catch (e: Exception) {
                        ivAvatar.setImageResource(R.drawable.ic_adulto)
                    }
                } else {
                    Glide.with(this)
                        .load(avatar)
                        .circleCrop()
                        .placeholder(R.drawable.ic_adulto)
                        .into(ivAvatar)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        TranslationManager.close()
    }
}
