package com.moviles.triaje.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.moviles.triaje.R

class IntroFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 1. Inflamos el diseño XML de este fragmento
        val view = inflater.inflate(R.layout.fragment_intro, container, false)

        // 2. Buscamos el botón "Comenzar" por su ID usando findViewById
        val btnComenzar = view.findViewById<Button>(R.id.btnComenzar)

        // 3. Programamos la acción del clic para navegar al siguiente fragmento
        btnComenzar.setOnClickListener {
            // Usamos el NavController para disparar la acción que creamos en el nav_intro.xml
            findNavController().navigate(R.id.action_introFragment_to_welcomeFragment)
        }

        return view
    }
}