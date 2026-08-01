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
import com.bumptech.glide.Glide
import com.moviles.triaje.R
import com.moviles.triaje.model.Usuario
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

        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
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

                // Cargar foto de perfil mejorada
                val avatar = getAvatarUrl(it)
                if (avatar.startsWith("data:image")) {
                    val base64String = avatar.substringAfter(",")
                    val imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                    val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    ivPerfil.setImageBitmap(bitmap)
                } else {
                    Glide.with(this)
                        .load(avatar)
                        .circleCrop()
                        .placeholder(R.drawable.ic_perfil)
                        .into(ivPerfil)
                }
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getAvatarUrl(usuario: Usuario?): String {
        val firestorePhoto = usuario?.us_avatar
        if (!firestorePhoto.isNullOrEmpty()) return firestorePhoto

        val authPhoto = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.photoUrl?.toString()
        if (!authPhoto.isNullOrEmpty()) return authPhoto

        val nombre = usuario?.us_nombre ?: "U"
        val apellidos = usuario?.us_apellidos ?: ""
        val nombreCompleto = "$nombre $apellidos".trim().replace(" ", "+")

        return "https://ui-avatars.com/api/?name=$nombreCompleto&background=1E60D5&color=fff&size=128&bold=true"
    }
}
