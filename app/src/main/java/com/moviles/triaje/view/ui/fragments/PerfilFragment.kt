package com.moviles.triaje.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.moviles.triaje.R
import com.moviles.triaje.viewmodel.VerInformacionViewModel

class PerfilFragment : Fragment() {

    private lateinit var viewModel: VerInformacionViewModel
    private lateinit var tvNombrePerfil: TextView
    private lateinit var tvCorreoPerfil: TextView
    private lateinit var ivPerfil: ImageView
    private lateinit var cvVerInformacion: CardView
    private lateinit var cvEditarPerfil: CardView
    private lateinit var cvPreferencias: CardView
    private lateinit var cvNotificaciones: CardView
    private lateinit var cvInfoApp: CardView
    private lateinit var btnCerrarSesion: MaterialButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)
        viewModel = ViewModelProvider(this)[VerInformacionViewModel::class.java]
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupObservers()
        viewModel.cargarDatosUsuario()
        setupClickListeners()
    }

    private fun initViews(view: View) {
        tvNombrePerfil = view.findViewById(R.id.tvNombrePerfil)
        tvCorreoPerfil = view.findViewById(R.id.tvCorreoPerfil)
        ivPerfil = view.findViewById(R.id.ivPerfil)
        cvVerInformacion = view.findViewById(R.id.cvVerInformacion)
        cvEditarPerfil = view.findViewById(R.id.cvEditarPerfil)
        cvPreferencias = view.findViewById(R.id.cvPreferencias)
        cvNotificaciones = view.findViewById(R.id.cvNotificaciones)
        cvInfoApp = view.findViewById(R.id.cvInfoApp)
        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion)
    }

    private fun setupObservers() {
        viewModel.usuario.observe(viewLifecycleOwner) { usuario ->
            usuario?.let {
                tvNombrePerfil.text = "${it.us_nombre} ${it.us_apellidos}"
                tvCorreoPerfil.text = it.us_email
            }
        }

        // Cargar foto de perfil
        val photoUrl = viewModel.getPhotoUrl()
        Glide.with(this)
            .load(photoUrl)
            .placeholder(R.drawable.ic_perfil)
            .error(R.drawable.ic_perfil)
            .into(ivPerfil)
    }

    private fun setupClickListeners() {
        cvVerInformacion.setOnClickListener {
            findNavController().navigate(R.id.action_perfilFragment_to_verInformacionFragment)
        }
        cvEditarPerfil.setOnClickListener {
            findNavController().navigate(R.id.action_perfilFragment_to_editarPerfilFragment)
        }
        cvPreferencias.setOnClickListener {
            findNavController().navigate(R.id.action_perfilFragment_to_preferenciasFragment)
        }
        cvNotificaciones.setOnClickListener {
            findNavController().navigate(R.id.action_perfilFragment_to_notificacionesFragment)
        }

        cvInfoApp.setOnClickListener {
            findNavController().navigate(R.id.action_perfilFragment_to_infoAppFragment)
        }

        btnCerrarSesion.setOnClickListener {
            cerrarSesion()
        }
    }

    private fun cerrarSesion() {
        Toast.makeText(requireContext(), "Cerrando sesión...", Toast.LENGTH_SHORT).show()
    }
}