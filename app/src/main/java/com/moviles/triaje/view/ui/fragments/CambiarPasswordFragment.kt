package com.moviles.triaje.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.moviles.triaje.R

class CambiarPasswordFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cambiar_password, container, false)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        val btnActualizarPassword = view.findViewById<MaterialButton>(R.id.btnActualizarPassword)
        btnActualizarPassword.setOnClickListener {
            // Aquí iría la lógica de re-autenticación y cambio de contraseña en Firebase
            Toast.makeText(requireContext(), "Contraseña actualizada (Simulado)", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }

        return view
    }
}