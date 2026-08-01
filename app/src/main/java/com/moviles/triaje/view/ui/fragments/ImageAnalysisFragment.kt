package com.moviles.triaje.view.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.moviles.triaje.R
import com.moviles.triaje.model.Sintoma
import com.moviles.triaje.viewmodel.ImageAnalysisViewModel
import java.io.File

class ImageAnalysisFragment : Fragment() {

    private lateinit var viewModel: ImageAnalysisViewModel

    private lateinit var ivRegresar: ImageView
    private lateinit var ivFotoLesion: ImageView
    private lateinit var cvVistaPrevia: MaterialCardView
    private lateinit var btnTomarFoto: MaterialButton
    private lateinit var btnSubirGaleria: MaterialButton
    private lateinit var tvOmitirPaso: TextView

    private var temporalCameraUri: Uri? = null

    // Contrato para abrir la Galería
    private val pickMedia = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            viewModel.setImageUri(uri)
        }
    }

    // Contrato para abrir la Cámara nativa
    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
            viewModel.setImageUri(temporalCameraUri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_image_analysis, container, false)

        // 1. Vincular componentes de la interfaz
        ivRegresar = view.findViewById(R.id.ivAnalisisImagenRegresar)
        ivFotoLesion = view.findViewById(R.id.ivFotoLesion)
        cvVistaPrevia = view.findViewById(R.id.cvVistaPrevia)
        btnTomarFoto = view.findViewById(R.id.btnTomarFoto)
        btnSubirGaleria = view.findViewById(R.id.btnSubirGaleria)
        tvOmitirPaso = view.findViewById(R.id.tvOmitirPaso)

        // 2. Inicializar ViewModel
        viewModel = ViewModelProvider(this)[ImageAnalysisViewModel::class.java]

        // 3. Recuperar síntomas del flujo previo
        @Suppress("UNCHECKED_CAST")
        val sintomas = arguments?.getSerializable("sintomas_seleccionados") as? List<Sintoma> ?: emptyList()
        viewModel.guardarSintomas(sintomas)

        // 4. Observar cambios en la URI de la imagen para actualizar la interfaz
        viewModel.imageUri.observe(viewLifecycleOwner) { uri ->
            if (uri != null) {
                ivFotoLesion.setPadding(0, 0, 0, 0) // Quitamos el padding del icono por defecto
                ivFotoLesion.setImageURI(uri)
            }
        }

        // 4.1 Observar resultados de la IA
        viewModel.analysisResult.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                val recommendation = viewModel.recommendation.value ?: ""
                mostrarResultadoIA(result, recommendation)
            }
        }

        // 5. Configurar Listeners de Clicks
        ivRegresar.setOnClickListener { findNavController().navigateUp() }

        btnSubirGaleria.setOnClickListener { pickMedia.launch("image/*") }

        btnTomarFoto.setOnClickListener { configurarCamaraYDisparar() }

        tvOmitirPaso.setOnClickListener { irAlCuestionario(null) }

        return view
    }

    private fun configurarCamaraYDisparar() {
        val context = requireContext()
        // Creamos un archivo temporal seguro en la caché del sistema
        val directory = File(context.cacheDir, "images").apply { mkdirs() }
        val file = File.createTempFile("lesion_", ".jpg", directory)

        // Obtenemos la URI segura mediante el FileProvider de tu proyecto
        temporalCameraUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        takePicture.launch(temporalCameraUri!!)
    }

    private fun mostrarResultadoIA(result: String, recommendation: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Resultado del Análisis")
            .setMessage("La IA ha detectado: $result\n\nRecomendación: $recommendation")
            .setPositiveButton("Continuar al Cuestionario") { _, _ ->
                irAlCuestionario(viewModel.imageUri.value?.toString())
            }
            .setNegativeButton("Reintentar") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun irAlCuestionario(imagePath: String?) {
        val listaSintomas = viewModel.sintomasRecuperados.value.orEmpty()

        val bundle = Bundle().apply {
            // Convertimos la lista de manera segura a un ArrayList compatible
            putSerializable("sintomas_seleccionados", ArrayList(listaSintomas))
            putString("uri_imagen_evidencia", imagePath) // Viaja como String o null si se omitió
        }
        findNavController().navigate(
            R.id.action_analisisImagenFragment_to_symptompsQuestionFragment,
            bundle
        )
    }
}