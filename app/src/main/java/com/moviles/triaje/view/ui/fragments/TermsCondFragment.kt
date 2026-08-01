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

class TermsCondFragment : Fragment() {

    private lateinit var viewModel: InfoAppViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_terms_cond, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializamos el MISMO ViewModel que maneja la configuración
        viewModel = ViewModelProvider(this)[InfoAppViewModel::class.java]

        val ivRegresar = view.findViewById<ImageView>(R.id.ivRegresar)
        val tvContenidoTerminos = view.findViewById<TextView>(R.id.tvContenidoTerminos)

        // Botón regresar
        ivRegresar.setOnClickListener {
            findNavController().popBackStack()
        }

        // Observamos los términos y actualizamos el texto
        viewModel.terminosApp.observe(viewLifecycleOwner) { terminos ->
            // 1. Limpiamos artefactos de traducción de forma agresiva e insensible a mayúsculas
            val textoLimpio = terminos
                .replace(Regex("\\\\[nN]"), "\n")      // \n o \N (sin espacio)
                .replace(Regex("\\\\ [nN]"), "\n")     // \ n o \ N (con espacio)
                .replace(Regex("//[nN]"), "\n")        // //n o //N
                .replace(Regex("\\\\ [nN][a-zA-Z]"), "\n") // Casos raros como \ NL o \ Nt
                .replace("\\n", "\n")
                .replace("\\\\n", "\n")
            
            tvContenidoTerminos.text = textoLimpio.trim()
        }

        // Ejecutamos la consulta a Firestore
        viewModel.cargarDatosConfiguracion()
    }
}
