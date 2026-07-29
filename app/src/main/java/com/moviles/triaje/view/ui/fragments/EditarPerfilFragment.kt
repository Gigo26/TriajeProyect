package com.moviles.triaje.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.moviles.triaje.R
import com.moviles.triaje.viewmodel.VerInformacionViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class EditarPerfilFragment : Fragment() {

    private lateinit var viewModel: VerInformacionViewModel
    private lateinit var etNombre: TextInputEditText
    private lateinit var etCorreo: TextInputEditText
    private lateinit var etApellidos: TextInputEditText
    private lateinit var etDni: TextInputEditText
    private lateinit var etCelular: TextInputEditText
    private lateinit var etContrasena: TextInputEditText
    private lateinit var etFechaNacimiento: TextInputEditText
    private lateinit var ivPerfil: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_editar_perfil, container, false)
        viewModel = ViewModelProvider(this)[VerInformacionViewModel::class.java]

        initViews(view)
        setupObservers()
        viewModel.cargarDatosUsuario()

        return view
    }

    private fun initViews(view: View) {
        etNombre = view.findViewById(R.id.etNombre)
        etCorreo = view.findViewById(R.id.etCorreo)
        etApellidos = view.findViewById(R.id.etApellidos)
        etDni = view.findViewById(R.id.etDni)
        etCelular = view.findViewById(R.id.etCelular)
        etContrasena = view.findViewById(R.id.etContrasena)
        etFechaNacimiento = view.findViewById(R.id.etFechaNacimiento)
        ivPerfil = view.findViewById(R.id.ivPerfil)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Navegar a cambiar contraseña al pulsar el campo de contraseña
        etContrasena.setOnClickListener {
            findNavController().navigate(R.id.action_editarPerfilFragment_to_cambiarPasswordFragment)
        }

        val btnGuardarCambios = view.findViewById<MaterialButton>(R.id.btnGuardarCambios)
        btnGuardarCambios.setOnClickListener {
            // Aquí iría la lógica para actualizar el celular en Firestore
            Toast.makeText(requireContext(), "Cambios guardados (Simulado)", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    private fun setupObservers() {
        viewModel.usuario.observe(viewLifecycleOwner) { usuario ->
            usuario?.let {
                etNombre.setText(it.us_nombre)
                etCorreo.setText(it.us_email)
                etApellidos.setText(it.us_apellidos)
                etDni.setText(it.us_dni)
                etCelular.setText(it.us_celular ?: "")

                it.us_fecha_nac?.let { fecha ->
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    etFechaNacimiento.setText(sdf.format(fecha))
                }
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
        }

        val photoUrl = viewModel.getPhotoUrl()
        if (photoUrl != null) {
            Glide.with(this)
                .load(photoUrl)
                .placeholder(R.drawable.ic_perfil)
                .into(ivPerfil)
        }
    }
}