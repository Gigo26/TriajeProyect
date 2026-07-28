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
import com.moviles.triaje.network.ApiDniService
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
        val etDni = view.findViewById<TextInputEditText>(R.id.etDni)
        val btnBuscarDni = view.findViewById<MaterialButton>(R.id.btnBuscarDni) // Botón de búsqueda DNI
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

        // Observador del botón Registrarse
        viewModel.isButtonEnabled.observe(viewLifecycleOwner) { infoValida ->
            btnRegister.isEnabled = infoValida
        }

        // -----------------------------------------------------------
        // LÓGICA DE BÚSQUEDA DE DNI
        // -----------------------------------------------------------
        btnBuscarDni.setOnClickListener {
            val dni = etDni.text.toString().trim()

            if (dni.length != 8) {
                etDni.error = "Ingrese un DNI válido de 8 dígitos"
                return@setOnClickListener
            }

            etDni.error = null
            btnBuscarDni.isEnabled = false // Deshabilitamos temporalmente
            Toast.makeText(requireContext(), "Buscando...", Toast.LENGTH_SHORT).show()

            ApiDniService().buscarDni(dni) { esExito, resultado ->
                requireActivity().runOnUiThread {
                    btnBuscarDni.isEnabled = true // Volvemos a habilitar
                    if (esExito) {
                        etName.setText(resultado)
                        Toast.makeText(requireContext(), "Datos encontrados", Toast.LENGTH_SHORT).show()
                    } else {
                        etName.setText("") // Limpiamos si hay error
                        Toast.makeText(requireContext(), resultado, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // -----------------------------------------------------------
        // ESCUCHADORES EN TIEMPO REAL
        // -----------------------------------------------------------
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

        // -----------------------------------------------------------
        // ACCIÓN REGISTRAR
        // -----------------------------------------------------------
        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val dni = etDni.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            // Lógica ajustada para tomar SIEMPRE las dos últimas palabras como apellidos
            val words = name.split("\\s+".toRegex()) // Divide por cualquier cantidad de espacios
            val usNombre: String
            val usApellidos: String

            if (words.size >= 3) {
                // Toma todas las palabras excepto las últimas 2 para el Nombre
                usNombre = words.dropLast(2).joinToString(" ")
                // Toma estrictamente las últimas 2 para los Apellidos
                usApellidos = words.takeLast(2).joinToString(" ")
            } else {
                // Fallback por si alguien edita y deja solo 2 o 1 palabra
                usNombre = words.firstOrNull() ?: ""
                usApellidos = words.drop(1).joinToString(" ")
            }

            viewModel.registrarUsuarioConVerificacion(usNombre, usApellidos, dni, email, password, confirmPassword)
        }

        tvIniciaSesionAqui.setOnClickListener {
            findNavController().navigateUp()
        }

        return view
    }
}