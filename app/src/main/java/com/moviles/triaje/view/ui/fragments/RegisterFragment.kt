package com.moviles.triaje.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.moviles.triaje.R
import com.moviles.triaje.onTextChanged
import com.moviles.triaje.viewmodel.RegisterViewModel

class RegisterFragment : Fragment() {

    private lateinit var viewModel: RegisterViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        viewModel = ViewModelProvider(this)[RegisterViewModel::class.java]

        // Enlazamos componentes
        val etName = view.findViewById<TextInputEditText>(R.id.etName)
        val etDni = view.findViewById<TextInputEditText>(R.id.etDni) // Nuevo campo
        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etPassword)
        val etConfirmPassword = view.findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnRegister = view.findViewById<MaterialButton>(R.id.btnRegister)
        val tvIniciaSesionAqui = view.findViewById<TextView>(R.id.tvIniciaSesionAqui)

        btnRegister.isEnabled = false

        // Observamos el resultado del flujo de registro con correo de validación
        viewModel.registerResult.observe(viewLifecycleOwner) { resultado ->
            if (resultado == "EXITO") {
                Toast.makeText(
                    requireContext(),
                    "¡Cuenta creada! Revisa tu correo electrónico para activarla antes de iniciar sesión.",
                    Toast.LENGTH_LONG
                ).show()
                findNavController().navigateUp() // Retorna al Login
            } else {
                Toast.makeText(requireContext(), resultado, Toast.LENGTH_LONG).show()
            }
        }

        // Observador del botón
        viewModel.isButtonEnabled.observe(viewLifecycleOwner) { infoValida ->
            btnRegister.isEnabled = infoValida
        }

        // Escuchadores en tiempo real (Incluyendo DNI)
        etName.onTextChanged { inputName ->
            viewModel.verificarCampos(inputName, etDni.text.toString(), etEmail.text.toString(), etPassword.text.toString(), etConfirmPassword.text.toString())
        }

        etDni.onTextChanged { inputDni ->
            viewModel.verificarCampos(etName.text.toString(), inputDni, etEmail.text.toString(), etPassword.text.toString(), etConfirmPassword.text.toString())
        }

        etEmail.onTextChanged { inputEmail ->
            viewModel.verificarCampos(etName.text.toString(), etDni.text.toString(), inputEmail, etPassword.text.toString(), etConfirmPassword.text.toString())
        }

        etPassword.onTextChanged { inputPassword ->
            viewModel.verificarCampos(etName.text.toString(), etDni.text.toString(), etEmail.text.toString(), inputPassword, etConfirmPassword.text.toString())
        }

        etConfirmPassword.onTextChanged { inputConfirmPassword ->
            viewModel.verificarCampos(etName.text.toString(), etDni.text.toString(), etEmail.text.toString(), etPassword.text.toString(), inputConfirmPassword)
        }

        // Click del botón Registrarse
        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val dni = etDni.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            // Separamos Nombre y Apellidos de forma lógica y sencilla para Firestore
            val parts = name.split(" ", limit = 2)
            val usNombre = parts.getOrNull(0) ?: ""
            val usApellidos = parts.getOrNull(1) ?: ""

            viewModel.registrarUsuarioConVerificacion(usNombre, usApellidos, dni, email, password, confirmPassword)
        }

        tvIniciaSesionAqui.setOnClickListener {
            findNavController().navigateUp()
        }

        return view
    }
}