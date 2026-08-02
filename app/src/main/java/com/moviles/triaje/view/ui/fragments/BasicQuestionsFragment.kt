package com.moviles.triaje.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.moviles.triaje.R
import com.moviles.triaje.model.DecisionContext
import com.moviles.triaje.model.Sintoma
import com.moviles.triaje.viewmodel.BasicQuestionsViewModel
import com.moviles.triaje.viewmodel.SharedTriageViewModel

class BasicQuestionsFragment : Fragment() {

    private lateinit var viewModel: BasicQuestionsViewModel
    private lateinit var sharedViewModel: SharedTriageViewModel

    private lateinit var rgOptions1: RadioGroup
    private lateinit var rgOptions2: RadioGroup
    private lateinit var rgOptions3: RadioGroup
    private lateinit var btnEvaluar: MaterialButton
    private lateinit var ivRegresar: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_basic_questions, container, false)

        // 1. Vincular componentes de la interfaz
        rgOptions1 = view.findViewById(R.id.rgOptions1)
        rgOptions2 = view.findViewById(R.id.rgOptions2)
        rgOptions3 = view.findViewById(R.id.rgOptions3)
        btnEvaluar = view.findViewById(R.id.btnEvaluar)
        ivRegresar = view.findViewById(R.id.ivAnalisisImagenRegresar)

        // 2. Inicializar ViewModel
        viewModel = ViewModelProvider(this)[BasicQuestionsViewModel::class.java]
        sharedViewModel = ViewModelProvider(requireActivity())[SharedTriageViewModel::class.java]

        // 3. Recuperar síntomas del argumento y resguardarlos en el ViewModel
        @Suppress("UNCHECKED_CAST")
        val sintomas = arguments?.getSerializable("sintomas_seleccionados") as? List<Sintoma> ?: emptyList()
        viewModel.guardarSintomas(sintomas)

        // 4. Capturar las selecciones de los RadioGroups
        setupRadioGroupListeners()

        // 5. Botón de navegación hacia atrás
        ivRegresar.setOnClickListener {
            findNavController().navigateUp()
        }

        // 6. Botón Siguiente con Enrutamiento de los 3 Flujos
        btnEvaluar.setOnClickListener {
            if (viewModel.validarPreguntasCompletas()) {
                
                // En la UI de BasicQuestions: 0=Sí, 1=No.
                // Engine espera: [Consciente (True), Respira (True), SangradoGrave (True)]
                val estaConsciente = viewModel.respuestaConsciente == 0
                val respiraNormal = viewModel.respuestaRespira == 0
                val sangradoGrave = viewModel.respuestaSangrado == 0 

                val listaConsolidada = listOf(
                    estaConsciente, 
                    respiraNormal,
                    sangradoGrave
                )

                sharedViewModel.setRespuestasBasicas(listaConsolidada)

                if (viewModel.evaluarCriterioEmergencia()) {
                    findNavController().navigate(
                        R.id.action_basicQuestionsFragment_to_fragmentResult
                    )
                } else {
                    findNavController().navigate(
                        R.id.action_basicQuestionsFragment_to_symptomFragment
                    )
                }
            }
        }

        return view
    }

    private fun setupRadioGroupListeners() {
        // PREGUNTA 1: ¿Está consciente?
        rgOptions1.setOnCheckedChangeListener { _, checkedId ->
            viewModel.respuestaConsciente = when (checkedId) {
                R.id.rbOptionYes1 -> 0 // Sí
                R.id.rbOptionNo1 -> 1  // No
                else -> -1
            }
        }

        // PREGUNTA 2: ¿Respira normalmente?
        rgOptions2.setOnCheckedChangeListener { _, checkedId ->
            viewModel.respuestaRespira = when (checkedId) {
                R.id.rbOptionYes2 -> 0 // Sí
                R.id.rbOptionNo2 -> 1  // No
                else -> -1
            }
        }

        // PREGUNTA 3: ¿Tiene sangrado?
        rgOptions3.setOnCheckedChangeListener { _, checkedId ->
            viewModel.respuestaSangrado = when (checkedId) {
                R.id.rbOptionYes3 -> 0 // Sí
                R.id.rbOptionNo3 -> 1  // No
                else -> -1
            }
        }
    }
}