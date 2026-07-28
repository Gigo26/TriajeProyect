package com.moviles.triaje.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.moviles.triaje.R
import com.moviles.triaje.viewmodel.VerInformacionViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class VerInformacionFragment : Fragment() {

    private lateinit var viewModel: VerInformacionViewModel
    private lateinit var tvNombre: TextView
    private lateinit var tvCorreo: TextView
    private lateinit var tvApellidos: TextView
    private lateinit var tvDni: TextView
    private lateinit var tvCelular: TextView
    private lateinit var tvFechaNacimiento: TextView
    private lateinit var ivPerfil: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ver_informacion, container, false)
        viewModel = ViewModelProvider(this)[VerInformacionViewModel::class.java]
        
        initViews(view)
        setupObservers()
        
        viewModel.cargarDatosUsuario()
        
        return view
    }

    private fun initViews(view: View) {
        tvNombre = view.findViewById(R.id.tvNombre)
        tvCorreo = view.findViewById(R.id.tvCorreo)
        tvApellidos = view.findViewById(R.id.tvApellidos)
        tvDni = view.findViewById(R.id.tvDni)
        tvCelular = view.findViewById(R.id.tvCelular)
        tvFechaNacimiento = view.findViewById(R.id.tvFechaNacimiento)
        ivPerfil = view.findViewById(R.id.ivPerfil)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupObservers() {
        viewModel.usuario.observe(viewLifecycleOwner) { usuario ->
            usuario?.let {
                tvNombre.text = it.us_nombre
                tvCorreo.text = it.us_email
                tvApellidos.text = it.us_apellidos
                tvDni.text = it.us_dni
                tvCelular.text = it.us_celular ?: "No registrado"
                
                it.us_fecha_nac?.let { fecha ->
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    tvFechaNacimiento.text = sdf.format(fecha)
                } ?: run {
                    tvFechaNacimiento.text = "No registrada"
                }
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
        }

        // Cargar foto de perfil
        val photoUrl = viewModel.getPhotoUrl()
        if (photoUrl != null) {
            Glide.with(this)
                .load(photoUrl)
                .placeholder(R.drawable.ic_perfil)
                .into(ivPerfil)
        }
    }
}