package com.moviles.triaje.view.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.moviles.triaje.R
import com.moviles.triaje.model.Sintoma
import com.moviles.triaje.viewmodel.ImageAnalysisViewModel
import com.moviles.triaje.viewmodel.SharedTriageViewModel
import java.io.File

class ImageAnalysisFragment : Fragment() {

    private lateinit var viewModel: ImageAnalysisViewModel
    private lateinit var sharedViewModel: SharedTriageViewModel

    private lateinit var ivRegresar: ImageView
    private lateinit var ivFotoLesion: ImageView
    private lateinit var vOverlayResultado: View
    private lateinit var tvResultadoIAOverlay: TextView
    private lateinit var cvVistaPrevia: MaterialCardView
    private lateinit var btnTomarFoto: MaterialButton
    private lateinit var btnSubirGaleria: MaterialButton
    private lateinit var btnContinuarCuestionario: MaterialButton
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
        vOverlayResultado = view.findViewById(R.id.vOverlayResultado)
        tvResultadoIAOverlay = view.findViewById(R.id.tvResultadoIAOverlay)
        cvVistaPrevia = view.findViewById(R.id.cvVistaPrevia)
        btnTomarFoto = view.findViewById(R.id.btnTomarFoto)
        btnSubirGaleria = view.findViewById(R.id.btnSubirGaleria)
        btnContinuarCuestionario = view.findViewById(R.id.btnContinuarCuestionario)
        tvOmitirPaso = view.findViewById(R.id.tvOmitirPaso)

        // 2. Inicializar ViewModel (SharedTriageViewModel atado a la Activity para mantener los datos)
        viewModel = ViewModelProvider(this)[ImageAnalysisViewModel::class.java]
        sharedViewModel = ViewModelProvider(requireActivity())[SharedTriageViewModel::class.java]

        // 3. Recuperar síntomas del flujo previo
        @Suppress("UNCHECKED_CAST")
        val sintomas = arguments?.getSerializable("sintomas_seleccionados") as? List<Sintoma> ?: emptyList()
        viewModel.guardarSintomas(sintomas)

        // 4. Observar cambios en la URI de la imagen para actualizar la interfaz
        viewModel.imageUri.observe(viewLifecycleOwner) { uri ->
            if (uri != null) {
                ivFotoLesion.setPadding(0, 0, 0, 0)
                ivFotoLesion.setImageURI(uri)
            }
        }

        // 4.1 Observar resultados de la IA
        viewModel.analysisResult.observe(viewLifecycleOwner) { result ->
            if (result != null && result != "Error" && result != "No se pudo determinar") {
                mostrarResultadoIAEnImagen(result)
            } else if (result == "Error" || result == "No se pudo determinar") {
                vOverlayResultado.visibility = View.GONE
                tvResultadoIAOverlay.visibility = View.GONE
                btnContinuarCuestionario.visibility = View.GONE
                btnSubirGaleria.visibility = View.VISIBLE
            }
        }

        // 5. Configurar Listeners de Clicks
        ivRegresar.setOnClickListener { findNavController().navigateUp() }

        btnSubirGaleria.setOnClickListener { pickMedia.launch("image/*") }

        btnTomarFoto.setOnClickListener { configurarCamaraYDisparar() }

        btnContinuarCuestionario.setOnClickListener {
            irAlCuestionario(viewModel.imageUri.value?.toString())
        }

        tvOmitirPaso.setOnClickListener { irAlCuestionario(null) }

        return view
    }

    private fun configurarCamaraYDisparar() {
        val context = requireContext()
        val directory = File(context.cacheDir, "images").apply { mkdirs() }
        val file = File.createTempFile("lesion_", ".jpg", directory)

        temporalCameraUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        takePicture.launch(temporalCameraUri!!)
    }

    private fun mostrarResultadoIAEnImagen(resultRaw: String) {
        val resourceId = when (resultRaw) {
            "Abrasions" -> R.string.ia_class_abrasions
            "Bruises" -> R.string.ia_class_bruises
            "Burns" -> R.string.ia_class_burns
            "Cut" -> R.string.ia_class_cut
            "Ingrown_nails" -> R.string.ia_class_ingrown_nails
            "Laceration" -> R.string.ia_class_laceration
            "Stab_wound" -> R.string.ia_class_stab_wound
            else -> R.string.ia_class_unknown
        }

        tvResultadoIAOverlay.text = getString(resourceId)
        vOverlayResultado.visibility = View.VISIBLE
        tvResultadoIAOverlay.visibility = View.VISIBLE

        // Ocultamos galería, mostramos botón verde de continuar
        btnSubirGaleria.visibility = View.GONE
        btnContinuarCuestionario.visibility = View.VISIBLE
    }

    private fun irAlCuestionario(imagePath: String?) {
        // Guardar datos de IA en el ViewModel compartido para que el Algoritmo Genético lo consuma al final
        sharedViewModel.setIAData(viewModel.analysisResult.value, imagePath)

        findNavController().navigate(
            R.id.action_analisisImagenFragment_to_symptompsQuestionFragment
        )
    }
}
