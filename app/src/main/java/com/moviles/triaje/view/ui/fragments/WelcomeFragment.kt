package com.moviles.triaje.view.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.moviles.triaje.R

class WelcomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_welcome, container, false)

        val btnInitSiguiente = view.findViewById<MaterialButton>(R.id.btnInitSiguiente)
        val pbSchedule = view.findViewById<ProgressBar>(R.id.pbSchedule)

        btnInitSiguiente.setOnClickListener {
            // 1. Mostramos el ProgressBar oscuro de inmediato
            pbSchedule.visibility = View.VISIBLE

            // 2. Deshabilitamos el botón para que no le den clic varias veces durante la espera
            btnInitSiguiente.isEnabled = false

            // 3. Programamos el temporizador de 5 segundos (5000 milisegundos)
            Handler(Looper.getMainLooper()).postDelayed({

                // Este bloque de código se ejecutará ÚNICAMENTE cuando pasen los 5 segundos
                if (isAdded) { // Buena práctica: verifica que el fragmento siga activo
                    findNavController().navigate(R.id.action_welcomeFragment_to_loginFragment)
                }

            }, 2000)
        }

        return view
    }
}