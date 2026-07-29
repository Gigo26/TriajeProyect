package com.moviles.triaje.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.moviles.triaje.R
import com.moviles.triaje.viewmodel.InfoAppViewModel

class InfoAppFragment : Fragment() {

    // NUEVO: Declaramos el ViewModel
    private lateinit var viewModel: InfoAppViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_info_app, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // NUEVO: Inicializamos el ViewModel
        viewModel = ViewModelProvider(this)[InfoAppViewModel::class.java]

        // NUEVO: Referencia al TextView de la versión
        val tvVersionApp = view.findViewById<TextView>(R.id.tvVersionApp)

        // Tus referencias actuales
        val btnGuia = view.findViewById<CardView>(R.id.cvInfoGuia)
        val btnPrioridades = view.findViewById<CardView>(R.id.cvInfoIndPrioridad)
        val btnTerminos = view.findViewById<CardView>(R.id.cvInfoTermCond)
        val btnPrivacidad = view.findViewById<CardView>(R.id.cvInfoPolPriv)
        val btnRegresar = view.findViewById<ImageView>(R.id.ivRegresar)

        // NUEVO: Observamos la versión que llega de Firestore y actualizamos el texto
        viewModel.versionApp.observe(viewLifecycleOwner) { versionFirestore ->
            tvVersionApp.text = versionFirestore
        }

        // NUEVO: Le ordenamos al ViewModel que empiece a buscar los datos
        viewModel.cargarDatosConfiguracion()

        // Tus clics de navegación (se mantienen exactamente igual)
        btnRegresar.setOnClickListener {
            findNavController().popBackStack()
        }

        btnGuia?.setOnClickListener {
            findNavController().navigate(R.id.action_infoAppFragment_to_guiaFragment)
        }

        btnPrioridades?.setOnClickListener {
            findNavController().navigate(R.id.action_infoAppFragment_to_prioridadesFragment)
        }

        btnTerminos?.setOnClickListener {
            findNavController().navigate(R.id.action_infoAppFragment_to_termsCondFragment)
        }

        btnPrivacidad?.setOnClickListener {
            findNavController().navigate(R.id.action_infoAppFragment_to_polPrivFragment)
        }
    }
}