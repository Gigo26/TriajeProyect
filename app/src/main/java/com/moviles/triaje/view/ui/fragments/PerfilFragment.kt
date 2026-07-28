package com.moviles.triaje.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.moviles.triaje.R

// Pasamos el layout directamente al constructor
class PerfilFragment : Fragment() {

    private lateinit var tvNombrePerfil: TextView
    private lateinit var tvCorreoPerfil: TextView
    private lateinit var cvVerInformacion: CardView
    private lateinit var cvEditarPerfil: CardView
    private lateinit var cvPreferencias: CardView
    private lateinit var cvNotificaciones: CardView
    private lateinit var cvInfoApp: CardView
    private lateinit var btnCerrarSesion: MaterialButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_perfil, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        loadUserData()
        setupClickListeners()
    }

    private fun initViews(view: View) {
        tvNombrePerfil = view.findViewById(R.id.tvNombrePerfil)
        tvCorreoPerfil = view.findViewById(R.id.tvCorreoPerfil)
        cvVerInformacion = view.findViewById(R.id.cvVerInformacion)
        cvEditarPerfil = view.findViewById(R.id.cvEditarPerfil)
        cvPreferencias = view.findViewById(R.id.cvPreferencias)
        cvNotificaciones = view.findViewById(R.id.cvNotificaciones)
        cvInfoApp = view.findViewById(R.id.cvInfoApp)
        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion)
    }

    private fun loadUserData() {
        tvNombrePerfil.text = "José Núñez"
        tvCorreoPerfil.text = "josenunez@gmail.com"
    }

    private fun setupClickListeners() {
        cvVerInformacion.setOnClickListener {
            Toast.makeText(requireContext(), "Ver Información", Toast.LENGTH_SHORT).show()
        }
        cvEditarPerfil.setOnClickListener {
            Toast.makeText(requireContext(), "Editar Perfil", Toast.LENGTH_SHORT).show()
        }
        cvPreferencias.setOnClickListener {
            Toast.makeText(requireContext(), "Preferencias", Toast.LENGTH_SHORT).show()
        }
        cvNotificaciones.setOnClickListener {
            Toast.makeText(requireContext(), "Notificaciones", Toast.LENGTH_SHORT).show()
        }

        // AQUÍ ESTÁ LA MAGIA DE LA NAVEGACIÓN
        cvInfoApp.setOnClickListener {
            findNavController().navigate(R.id.action_perfilFragment_to_infoAppFragment)
        }

        btnCerrarSesion.setOnClickListener {
            cerrarSesion()
        }
    }

    private fun cerrarSesion() {
        Toast.makeText(requireContext(), "Cerrando sesión...", Toast.LENGTH_SHORT).show()
        // Tu lógica de cierre de sesión
    }
}