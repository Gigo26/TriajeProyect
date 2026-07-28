package com.moviles.triaje.view.ui.fragments

import android.content.Intent
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.moviles.triaje.R
import com.moviles.triaje.onTextChanged
import com.moviles.triaje.network.Callback
import com.moviles.triaje.view.ui.activities.MainActivity
import com.moviles.triaje.viewmodel.LoginViewModel
import com.moviles.triaje.viewmodel.LoginViewModel.LoginState

class LoginFragment : Fragment() {

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        val tvOlvidePassword = view.findViewById<TextView>(R.id.tvOlvidePassword)
        val btnLogin = view.findViewById<MaterialButton>(R.id.btnLogin)
        val btnLoginGoogle = view.findViewById<MaterialButton>(R.id.btnLoginGoogle)
        val btnLoginApple = view.findViewById<MaterialButton>(R.id.btnLoginApple)
        val tvRegistrateAqui = view.findViewById<TextView>(R.id.tvRegistrateAqui)
        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etPassword)

        btnLogin.isEnabled = false

        // OBSERVADOR DE ESTADO: Ahora reacciona al inicio seguro mediante Firebase Auth
        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoginState.Loading -> {
                    Toast.makeText(requireContext(), "Autenticando de forma segura...", Toast.LENGTH_SHORT).show()
                }
                is LoginState.Success -> {
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
                is LoginState.Error -> {
                    val mensajeAmigable = when {
                        // 1. Correo no verificado
                        state.mensaje.contains("verificar tu cuenta", ignoreCase = true) -> {
                            mostrarAlertaReenvio(etEmail.text.toString().trim(), etPassword.text.toString().trim())
                            null // No mostramos toast si abrimos el diálogo modal
                        }
                        // 2. Credenciales incorrectas (Contraseña mal escrita, etc.)
                        state.mensaje.contains("incorrect", ignoreCase = true) ||
                                state.mensaje.contains("invalid-credential", ignoreCase = true) ||
                                state.mensaje.contains("wrong-password", ignoreCase = true) -> {
                            "El correo electrónico o la contraseña son incorrectos."
                        }
                        // 3. Usuario no encontrado
                        state.mensaje.contains("user-not-found", ignoreCase = true) -> {
                            "No existe ninguna cuenta registrada con este correo."
                        }
                        // 4. Formato de correo inválido
                        state.mensaje.contains("invalid-email", ignoreCase = true) -> {
                            "El formato del correo electrónico no es válido."
                        }
                        // 5. Demasiados intentos fallidos (Bloqueo temporal por seguridad)
                        state.mensaje.contains("too-many-requests", ignoreCase = true) -> {
                            "Demasiados intentos fallidos. Tu cuenta ha sido bloqueada temporalmente por seguridad."
                        }
                        // 6. Error genérico de respaldo por si falla la conexión
                        else -> {
                            "Ocurrió un problema de red. Por favor, inténtalo de nuevo."
                        }
                    }

                    // Si el mensaje procesado no es nulo, lo mostramos en el Toast
                    if (mensajeAmigable != null) {
                        Toast.makeText(requireContext(), mensajeAmigable, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        viewModel.isButtonEnabled.observe(viewLifecycleOwner) { infoValida ->
            btnLogin.isEnabled = infoValida
        }

        etEmail.onTextChanged { email ->
            val password = etPassword.text.toString()
            viewModel.verificarCampos(email, password)
        }

        etPassword.onTextChanged { inputPassword ->
            val email = etEmail.text.toString()
            viewModel.verificarCampos(email, inputPassword)
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            viewModel.autenticarUsuario(email, password)
        }

        tvOlvidePassword.setOnClickListener { mostrarDialogoCustom() }
        btnLoginGoogle.setOnClickListener { Toast.makeText(requireContext(), "Flujo de Google", Toast.LENGTH_SHORT).show() }
        btnLoginApple.setOnClickListener { Toast.makeText(requireContext(), "Flujo de Apple", Toast.LENGTH_SHORT).show() }

        tvRegistrateAqui.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        return view
    }

    // NUEVO: Alerta por si el usuario no encuentra el correo en su Spam y quiere reenviarlo
    private fun mostrarAlertaReenvio(email: String, pass: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cuenta no verificada")
            .setMessage("Por favor, haz clic en el enlace que enviamos a tu correo para activar tu cuenta. ¿No lo recibiste?")
            .setPositiveButton("Reenviar Correo") { dialog, _ ->
                viewModel.reenviarCorreoDeVerificacion(email, pass, object : Callback<String> {
                    override fun onSuccess(result: String?) {
                        Toast.makeText(requireContext(), result ?: "Enviado.", Toast.LENGTH_LONG).show()
                    }
                    override fun onFailed(exception: Exception) {
                        Toast.makeText(requireContext(), exception.message, Toast.LENGTH_LONG).show()
                    }
                })
                dialog.dismiss()
            }
            .setNegativeButton("Entendido") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun mostrarDialogoCustom() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.detail_forget_password, null)
        builder.setView(dialogView)
        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnDialogOk = dialogView.findViewById<MaterialButton>(R.id.btnDialogOk)
        val btnDialogVolverEnviar = dialogView.findViewById<MaterialButton>(R.id.btnDialogVolverEnviar)

        btnDialogOk.setOnClickListener { alertDialog.dismiss() }
        btnDialogVolverEnviar.setOnClickListener {
            Toast.makeText(requireContext(), "Código reenviado", Toast.LENGTH_SHORT).show()
            alertDialog.dismiss()
        }
        alertDialog.show()
    }
}