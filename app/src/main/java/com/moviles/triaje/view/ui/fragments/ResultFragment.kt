package com.moviles.triaje.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.moviles.triaje.R
import com.moviles.triaje.viewmodel.ResultViewModel
import com.moviles.triaje.viewmodel.SharedTriageViewModel

class ResultFragment : Fragment() {

    private lateinit var viewModel: ResultViewModel
    private lateinit var sharedViewModel: SharedTriageViewModel

    private lateinit var cvHeaderResult: MaterialCardView
    private lateinit var ivPriorityIcon: ImageView
    private lateinit var tvPriorityName: TextView
    private lateinit var tvDiagnosisTitle: TextView
    private lateinit var tvRiskExplanation: TextView
    private lateinit var btnVerRecomendaciones: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_result, container, false)

        // 1. Vincular componentes del XML (Asegurados con los nuevos IDs del fragment_result.xml)
        cvHeaderResult = view.findViewById(R.id.cvHeaderResult)
        ivPriorityIcon = view.findViewById(R.id.ivPriorityIcon)
        tvPriorityName = view.findViewById(R.id.tvPriorityName)
        tvDiagnosisTitle = view.findViewById(R.id.tvDiagnosisTitle)
        tvRiskExplanation = view.findViewById(R.id.tvRiskExplanation)
        btnVerRecomendaciones = view.findViewById(R.id.btnVerRecomendaciones)

        // 2. Inicializar ViewModels
        viewModel = ViewModelProvider(this)[ResultViewModel::class.java]
        sharedViewModel = ViewModelProvider(requireActivity())[SharedTriageViewModel::class.java]

        // 3. Ejecutar Evaluación Evolutiva
        sharedViewModel.ejecutarEvaluacion()

        // 4. Observar la respuesta generada por el Algoritmo Genético
        setupObservers()

        // 5. Transición hacia RecommendationsFragment
        btnVerRecomendaciones.setOnClickListener {
            findNavController().navigate(
                R.id.action_resultFragment_to_recomendacionesFragment
            )
        }

        return view
    }

    private fun setupObservers() {
        sharedViewModel.resultadoEvolutivo.observe(viewLifecycleOwner) { resultado ->
            resultado?.let {
                val prioridad = it.prioridad

                // Actualizar datos de Prioridad
                tvPriorityName.text = "PRIORIDAD ${prioridad.nombre}"
                tvPriorityName.setTextColor(ContextCompat.getColor(requireContext(), prioridad.colorResId))
                ivPriorityIcon.setImageResource(prioridad.iconResId)

                // Actualizar Diagnóstico Probable y Explicación Evolutiva
                tvDiagnosisTitle.text = it.diagnosticoProbable
                tvRiskExplanation.text = it.explicacionRiesgo
                
                // Opcional: Cambiar color del botón según la prioridad para mayor coherencia visual
                btnVerRecomendaciones.setBackgroundColor(ContextCompat.getColor(requireContext(), prioridad.colorResId))
            }
        }
    }
}
