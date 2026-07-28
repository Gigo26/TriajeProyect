package com.moviles.triaje.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.moviles.triaje.R

class InfoAppFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_info_app, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Nota: Asegúrate de que los IDs (R.id.cvIrGuia, etc.) coincidan
        // con los que le pusiste a las tarjetas o botones en tu fragment_info_app.xml

        val btnGuia = view.findViewById<CardView>(R.id.cvInfoGuia)
        val btnPrioridades = view.findViewById<CardView>(R.id.cvInfoIndPrioridad)
        val btnTerminos = view.findViewById<CardView>(R.id.cvInfoTermCond)
        val btnPrivacidad = view.findViewById<CardView>(R.id.cvInfoPolPriv)


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