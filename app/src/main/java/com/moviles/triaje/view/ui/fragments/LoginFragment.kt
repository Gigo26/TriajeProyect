package com.moviles.triaje.view.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
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
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText

    private lateinit var googleSignInClient: GoogleSignInClient

    // 🔥 NUEVO: Lanzador de la ventana de Google
    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    // Si recibimos el Token de Google, se lo pasamos al ViewModel
                    viewModel.autenticarConGoogle(idToken)
                } else {
                    Toast.makeText(requireContext(), "Error: No se recibió token de Google", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Toast.makeText(requireContext(), "Fallo al iniciar sesión con Google: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        val tvOlvidePassword = view.findViewById<TextView>(R.id.tvOlvidePassword)
        val btnLogin = view.findViewById<MaterialButton>(R.id.btnLogin)
        val btnLoginGoogle = view.findViewById<MaterialButton>(R.id.btnLoginGoogle)
        val tvRegistrateAqui = view.findViewById<TextView>(R.id.tvRegistrateAqui)
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)

        btnLogin.isEnabled = false

        // 🔥 NUEVO: Configuración del cliente de Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // default_web_client_id es generado automáticamente por google-services.json
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile() // 👈 AGREGADO: Solicitar acceso al perfil (incluye foto)
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoginState.Loading -> {

                }
                is LoginState.Success -> {
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
                is LoginState.RequirePasswordRecovery -> {
                    Toast.makeText(requireContext(), "Demasiados intentos. Te ayudaremos a recuperar tu cuenta.", Toast.LENGTH_LONG).show()
                    abrirDialogoRecuperacion()
                }
                is LoginState.Error -> {
                    val mensajeAmigable = when {
                        state.mensaje.contains("verificar tu cuenta", ignoreCase = true) -> {
                            mostrarAlertaReenvio(etEmail.text.toString().trim(), etPassword.text.toString().trim())
                            null
                        }
                        state.mensaje.contains("user-not-found", ignoreCase = true) -> {
                            "No existe ninguna cuenta registrada con este correo."
                        }
                        state.mensaje.contains("invalid-email", ignoreCase = true) -> {
                            "El formato del correo electrónico no es válido."
                        }
                        state.mensaje.contains("too-many-requests", ignoreCase = true) -> {
                            "Tu cuenta ha sido bloqueada temporalmente por seguridad."
                        }
                        else -> state.mensaje
                    }

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

        tvOlvidePassword.setOnClickListener {
            abrirDialogoRecuperacion()
        }

        // 🔥 NUEVO: Clic en el botón de Google
        btnLoginGoogle.setOnClickListener {
            // Cerramos sesión previa para obligar a mostrar la lista de cuentas (Opcional pero recomendado)
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            }
        }

        tvRegistrateAqui.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        return view
    }

    private fun abrirDialogoRecuperacion() {
        val correoIngresado = etEmail.text.toString().trim()
        val dialog = ForgotPasswordDialog().apply {
            arguments = Bundle().apply {
                putString("CORREO", correoIngresado)
            }
        }
        dialog.show(parentFragmentManager, "ForgotPasswordDialog")
    }

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
}