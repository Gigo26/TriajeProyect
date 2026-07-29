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

        // Observamos los términos y actualizamos el texto
        viewModel.terminosApp.observe(viewLifecycleOwner) { politicas ->
            // Reemplazamos el texto literal "\n" por un salto de línea real de programación
            val textoFormateado = politicas.replace("\\n", "\n")
            tvPoliticasPrivacidad.text = textoFormateado
        }

        // Ejecutamos la consulta a Firestore
        viewModel.cargarDatosConfiguracion()
    }
}