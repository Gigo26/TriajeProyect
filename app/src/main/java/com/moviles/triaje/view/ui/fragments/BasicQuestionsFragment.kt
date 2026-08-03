package com.moviles.triaje.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.moviles.triaje.R
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

        rgOptions1 = view.findViewById(R.id.rgOptions1)
        rgOptions2 = view.findViewById(R.id.rgOptions2)
        rgOptions3 = view.findViewById(R.id.rgOptions3)
        btnEvaluar = view.findViewById(R.id.btnEvaluar)
        ivRegresar = view.findViewById(R.id.ivAnalisisImagenRegresar)

        viewModel = ViewModelProvider(this)[BasicQuestionsViewModel::class.java]
        sharedViewModel = ViewModelProvider(requireActivity())[SharedTriageViewModel::class.java]

        @Suppress("UNCHECKED_CAST")
        val sintomas = arguments?.getSerializable("sintomas_seleccionados") as? List<Sintoma> ?: emptyList()
        viewModel.guardarSintomas(sintomas)

        setupRadioGroupListeners()

        ivRegresar.setOnClickListener {
            findNavController().navigateUp()
        }

        btnEvaluar.setOnClickListener {
            if (viewModel.validarPreguntasCompletas()) {

                // ==============================================================
                // CORRECCIÓN DE MAPEO PARA EL MOTOR EVOLUTIVO
                // ==============================================================
                // 1. ¿Responde o está despierta? -> Sí (0) es Bueno (True)
                val estaConsciente = viewModel.respuestaConsciente == 0

                // 2. ¿Siente ahogo severo? -> No (1) es Bueno (True, respira normal)
                val respiraNormal = viewModel.respuestaRespira == 1

                // 3. ¿Presenta sangrado profuso? -> Sí (0) es Malo (True, hay hemorragia)
                val sangradoGrave = viewModel.respuestaSangrado == 0

                val listaConsolidada = listOf(
                    estaConsciente,
                    respiraNormal,
                    sangradoGrave
                )

                sharedViewModel.setRespuestasBasicas(listaConsolidada)

                // La evaluación de emergencia en el ViewModel ya estaba bien configurada
                if (viewModel.evaluarCriterioEmergencia()) {
                    findNavController().navigate(
                        R.id.action_basicQuestionsFragment_to_fragmentResult
                    )
                } else {
                    // Si no requiere análisis visual, podrías saltarlo, pero mantenemos tu flujo:
                    findNavController().navigate(
                        R.id.action_basicQuestionsFragment_to_symptomFragment
                    )
                }
            }
        }

        return view
    }

    private fun setupRadioGroupListeners() {
        rgOptions1.setOnCheckedChangeListener { _, checkedId ->
            viewModel.respuestaConsciente = when (checkedId) {
                R.id.rbOptionYes1 -> 0 // Sí
                R.id.rbOptionNo1 -> 1  // No
                else -> -1
            }
        }

        rgOptions2.setOnCheckedChangeListener { _, checkedId ->
            viewModel.respuestaRespira = when (checkedId) {
                R.id.rbOptionYes2 -> 0 // Sí
                R.id.rbOptionNo2 -> 1  // No
                else -> -1
            }
        }

        rgOptions3.setOnCheckedChangeListener { _, checkedId ->
            viewModel.respuestaSangrado = when (checkedId) {
                R.id.rbOptionYes3 -> 0 // Sí
                R.id.rbOptionNo3 -> 1  // No
                else -> -1
            }
        }
    }
}