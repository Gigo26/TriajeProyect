package com.moviles.triaje.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.moviles.triaje.R
import com.moviles.triaje.viewmodel.InfoAppViewModel

class PolPrivFragment : Fragment() {

    private lateinit var viewModel: InfoAppViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pol_priv, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializamos el MISMO ViewModel que maneja la configuración
        viewModel = ViewModelProvider(this)[InfoAppViewModel::class.java]

        val ivRegresar = view.findViewById<ImageView>(R.id.ivRegresar)
        val tvPoliticasPrivacidad = view.findViewById<TextView>(R.id.tvPoliticasPrivacidad)

        // Botón regresar
        ivRegresar.setOnClickListener {
            findNavController().popBackStack()
        }

        // Observamos las políticas y actualizamos el texto
        viewModel.politicasApp.observe(viewLifecycleOwner) { politicas ->
            // 1. Limpiamos artefactos de traducción de forma agresiva e insensible a mayúsculas
            val textoLimpio = politicas
                .replace(Regex("\\\\[nN]"), "\n")      // \n o \N (sin espacio)
                .replace(Regex("\\\\ [nN]"), "\n")     // \ n o \ N (con espacio)
                .replace(Regex("//[nN]"), "\n")        // //n o //N
                .replace(Regex("\\\\ [nN][a-zA-Z]"), "\n") // Casos raros como \ NL o \ Nt
                .replace("\\n", "\n")
                .replace("\\\\n", "\n")
            
            tvPoliticasPrivacidad.text = textoLimpio.trim()
        }

        // Ejecutamos la consulta a Firestore
        viewModel.cargarDatosConfiguracion()
    }
}
