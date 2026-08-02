package com.moviles.triaje.view.ui.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.moviles.triaje.R
import com.moviles.triaje.network.Callback
import com.moviles.triaje.view.ui.activities.MainActivity
import com.moviles.triaje.viewmodel.ForgotPasswordViewModel
import java.util.concurrent.TimeUnit

class ForgotPasswordDialog : DialogFragment() {

    private lateinit var viewModel: ForgotPasswordViewModel
    private var correoIngresado: String = ""

    private val firebaseAuth = FirebaseAuth.getInstance()
    private var mVerificationId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Recuperamos el correo
        correoIngresado = arguments?.getString("CORREO") ?: ""

        // 2. Instanciamos el NUEVO ViewModel exclusivo para este diálogo
        viewModel = ViewModelProvider(this)[ForgotPasswordViewModel::class.java]
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.detail_forget_password, null)
        builder.setView(dialogView)

        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.setCanceledOnTouchOutside(false)

        val layoutOpciones = dialogView.findViewById<View>(R.id.layoutOpciones)
        val layoutInputCelular = dialogView.findViewById<View>(R.id.layoutInputCelular)
        val layoutInputCodigo = dialogView.findViewById<View>(R.id.layoutInputCodigo)
        val tvDialogMessage = dialogView.findViewById<TextView>(R.id.tvDialogMessage)

        val btnOpcionCorreo = dialogView.findViewById<MaterialButton>(R.id.btnOpcionCorreo)
        val btnOpcionSMS = dialogView.findViewById<MaterialButton>(R.id.btnOpcionSMS)
        val btnEnviarSMSConfirmar = dialogView.findViewById<MaterialButton>(R.id.btnEnviarSMSConfirmar)
        val btnVerificarCodigo = dialogView.findViewById<MaterialButton>(R.id.btnVerificarCodigo)
        val btnDialogCancelar = dialogView.findViewById<MaterialButton>(R.id.btnDialogCancelar)

        val etDialogCelular = dialogView.findViewById<TextInputEditText>(R.id.etDialogCelular)
        val etDialogCodigo = dialogView.findViewById<TextInputEditText>(R.id.etDialogCodigo)

        val mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                iniciarSesionConCredencial(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(requireContext(), "Error al enviar SMS: ${e.message}", Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                mVerificationId = verificationId
                Toast.makeText(requireContext(), "SMS enviado exitosamente", Toast.LENGTH_SHORT).show()
                layoutInputCelular.visibility = View.GONE
                layoutInputCodigo.visibility = View.VISIBLE
                tvDialogMessage.text = "Ingresa el código de 6 dígitos que enviamos a tu celular."
            }
        }

        btnOpcionCorreo.setOnClickListener {
            if (correoIngresado.isEmpty()) {
                Toast.makeText(requireContext(), "Escribe tu correo en la pantalla principal primero.", Toast.LENGTH_LONG).show()
                dismiss()
            } else {
                val context = requireContext()
                viewModel.enviarCorreoRecuperacion(correoIngresado, object : Callback<String> {
                    override fun onSuccess(result: String?) {
                        if (isAdded && activity != null) {
                            Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                            dismiss()
                        }
                    }
                    override fun onFailed(exception: Exception) {
                        if (isAdded && activity != null) {
                            Toast.makeText(context, exception.message, Toast.LENGTH_LONG).show()
                        }
                    }
                })
            }
        }

        btnOpcionSMS.setOnClickListener {
            layoutOpciones.visibility = View.GONE
            layoutInputCelular.visibility = View.VISIBLE
            tvDialogMessage.text = "Ingresa tu número registrado para enviarte un código de verificación."
        }

        btnEnviarSMSConfirmar.setOnClickListener {
            val celular = etDialogCelular.text.toString().trim()
            if (celular.length < 9 || !celular.startsWith("+")) {
                Toast.makeText(requireContext(), "Ingresa un número válido con código de país", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(requireContext(), "Solicitando código...", Toast.LENGTH_SHORT).show()

            val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(celular)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(mCallbacks)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }

        btnVerificarCodigo.setOnClickListener {
            val codigo = etDialogCodigo.text.toString().trim()
            if (codigo.length == 6) {
                val credential = PhoneAuthProvider.getCredential(mVerificationId, codigo)
                iniciarSesionConCredencial(credential)
            } else {
                Toast.makeText(requireContext(), "El código debe tener 6 dígitos", Toast.LENGTH_SHORT).show()
            }
        }

        btnDialogCancelar.setOnClickListener {
            dismiss()
        }

        return alertDialog
    }

    private fun iniciarSesionConCredencial(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Identidad verificada correctamente", Toast.LENGTH_SHORT).show()
                    dismiss()

                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                } else {
                    Toast.makeText(requireContext(), "El código es incorrecto o ha expirado.", Toast.LENGTH_LONG).show()
                }
            }
    }
}