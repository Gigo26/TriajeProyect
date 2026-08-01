package com.moviles.triaje.view.ui.fragments

import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.moviles.triaje.R
import com.moviles.triaje.network.ApiDniService
import com.moviles.triaje.viewmodel.MainViewModel
import com.moviles.triaje.viewmodel.PerfilViewModel
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class EditarPerfilFragment : Fragment() {

    private lateinit var viewModel: PerfilViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var etNombreCompleto: TextInputEditText
    private lateinit var etCorreo: TextInputEditText
    private lateinit var etDni: TextInputEditText
    private lateinit var etCelular: TextInputEditText
    private lateinit var etContrasena: TextInputEditText
    private lateinit var etFechaNacimiento: TextInputEditText
    private lateinit var ivPerfil: ImageView
    private lateinit var fabEditPhoto: FloatingActionButton
    private lateinit var btnBuscarDni: MaterialButton
    private lateinit var btnGuardarCambios: MaterialButton

    private var selectedDate: Date? = null
    private var encodedImage: String? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { processSelectedImage(it) }
    }

    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        bitmap?.let { processCapturedImage(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_editar_perfil, container, false)
        viewModel = ViewModelProvider(this)[PerfilViewModel::class.java]
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        initViews(view)
        setupObservers()
        viewModel.cargarDatosUsuario()

        return view
    }

    private fun initViews(view: View) {
        etNombreCompleto = view.findViewById(R.id.etNombreCompleto)
        etCorreo = view.findViewById(R.id.etCorreo)
        etDni = view.findViewById(R.id.etDni)
        etCelular = view.findViewById(R.id.etCelular)
        etContrasena = view.findViewById(R.id.etContrasena)
        etFechaNacimiento = view.findViewById(R.id.etFechaNacimiento)
        ivPerfil = view.findViewById(R.id.ivPerfil)
        fabEditPhoto = view.findViewById(R.id.fabEditPhoto)
        btnBuscarDni = view.findViewById(R.id.btnBuscarDni)
        btnGuardarCambios = view.findViewById(R.id.btnGuardarCambios)

        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener { findNavController().navigateUp() }

        etContrasena.setOnClickListener {
            findNavController().navigate(R.id.action_editarPerfilFragment_to_cambiarPasswordFragment)
        }

        etFechaNacimiento.setOnClickListener { showDatePicker() }

        btnBuscarDni.setOnClickListener { buscarDni() }

        fabEditPhoto.setOnClickListener { showImageOptions() }

        btnGuardarCambios.setOnClickListener { guardarCambios() }
    }

    private fun setupObservers() {
        viewModel.usuario.observe(viewLifecycleOwner) { usuario ->
            usuario?.let {
                etNombreCompleto.setText("${it.us_nombre} ${it.us_apellidos}")
                etCorreo.setText(it.us_email)
                etDni.setText(it.us_dni)
                etCelular.setText(it.us_celular ?: "")

                it.us_fecha_nac?.let { fecha ->
                    selectedDate = fecha
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    etFechaNacimiento.setText(sdf.format(fecha))
                }

                val avatar = viewModel.getAvatarUrl(it)
                if (avatar.startsWith("data:image")) {
                    val base64String = avatar.substringAfter(",")
                    val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    ivPerfil.setImageBitmap(bitmap)
                } else {
                    Glide.with(this).load(avatar).circleCrop().placeholder(R.drawable.ic_perfil).into(ivPerfil)
                }
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        selectedDate?.let { calendar.time = it }
        
        DatePickerDialog(requireContext(), { _, year, month, day ->
            val newCalendar = Calendar.getInstance()
            newCalendar.set(year, month, day)
            selectedDate = newCalendar.time
            etFechaNacimiento.setText(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate!!))
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun buscarDni() {
        val dni = etDni.text.toString().trim()
        if (dni.length != 8) {
            etDni.error = "DNI inválido"
            return
        }

        btnBuscarDni.isEnabled = false
        ApiDniService().buscarDni(dni) { success, result ->
            requireActivity().runOnUiThread {
                btnBuscarDni.isEnabled = true
                if (success) {
                    etNombreCompleto.setText(result)
                } else {
                    Toast.makeText(requireContext(), result, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showImageOptions() {
        val options = arrayOf("Cámara", "Galería")
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Seleccionar Imagen")
            .setItems(options) { _, which ->
                if (which == 0) takePhoto.launch(null) else pickImage.launch("image/*")
            }.show()
    }

    private fun processSelectedImage(uri: Uri) {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        processCapturedImage(bitmap)
    }

    private fun processCapturedImage(bitmap: Bitmap) {
        val resized = Bitmap.createScaledBitmap(bitmap, 400, 400, true)
        ivPerfil.setImageBitmap(resized)
        
        val outputStream = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        encodedImage = "data:image/jpeg;base64,$base64"
    }

    private fun guardarCambios() {
        val fullName = etNombreCompleto.text.toString().trim()
        val dni = etDni.text.toString().trim()
        val celular = etCelular.text.toString().trim()

        if (fullName.isEmpty() || dni.isEmpty()) {
            Toast.makeText(requireContext(), "Nombre y DNI son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val words = fullName.split("\\s+".toRegex())
        val usNombre: String
        val usApellidos: String

        if (words.size >= 3) {
            usNombre = words.dropLast(2).joinToString(" ")
            usApellidos = words.takeLast(2).joinToString(" ")
        } else {
            usNombre = words.firstOrNull() ?: ""
            usApellidos = words.drop(1).joinToString(" ")
        }

        viewModel.actualizarPerfil(dni, usNombre, usApellidos, celular, selectedDate, encodedImage, object : com.moviles.triaje.network.Callback<Boolean> {
            override fun onSuccess(result: Boolean?) {
                Toast.makeText(requireContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show()
                // Refrescar el ViewModel de la Actividad para actualizar el Toolbar
                mainViewModel.cargarDatosUsuario()
                findNavController().navigateUp()
            }
            override fun onFailed(exception: Exception) {
                Toast.makeText(requireContext(), "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
