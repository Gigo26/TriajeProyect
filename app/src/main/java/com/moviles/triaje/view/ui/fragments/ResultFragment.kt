package com.moviles.triaje.view.ui.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
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
import com.moviles.triaje.viewmodel.RecommendationViewModel
import com.moviles.triaje.viewmodel.SharedTriageViewModel

class ResultFragment : Fragment() {

    private lateinit var sharedViewModel: SharedTriageViewModel
    private lateinit var recommendationViewModel: RecommendationViewModel

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

        cvHeaderResult = view.findViewById(R.id.cvHeaderResult)
        ivPriorityIcon = view.findViewById(R.id.ivPriorityIcon)
        tvPriorityName = view.findViewById(R.id.tvPriorityName)
        tvDiagnosisTitle = view.findViewById(R.id.tvDiagnosisTitle)
        tvRiskExplanation = view.findViewById(R.id.tvRiskExplanation)
        btnVerRecomendaciones = view.findViewById(R.id.btnVerRecomendaciones)

        sharedViewModel = ViewModelProvider(requireActivity())[SharedTriageViewModel::class.java]
        recommendationViewModel = ViewModelProvider(requireActivity())[RecommendationViewModel::class.java]

        if (sharedViewModel.resultadoEvolutivo.value == null) {
            // TRIAJE NUEVO: Reseteamos el seguro para permitir que se guarde en Firebase
            recommendationViewModel.prepararNuevoGuardado()
            sharedViewModel.ejecutarEvaluacion(requireContext())
        }

        setupObservers()

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

                tvPriorityName.text = getString(R.string.priority_label, getString(prioridad.stringResId))
                tvPriorityName.setTextColor(ContextCompat.getColor(requireContext(), prioridad.colorResId))
                ivPriorityIcon.setImageResource(prioridad.iconResId)

                tvDiagnosisTitle.text = it.diagnosticoProbable
                tvRiskExplanation.text = it.explicacionRiesgo

                btnVerRecomendaciones.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), prioridad.colorResId)
                )

                recommendationViewModel.consolidarConsulta(
                    tipoPaciente = sharedViewModel.tipoPaciente.value ?: "ADULTO",
                    sintomas = sharedViewModel.sintomasSeleccionados.value ?: emptyList(),
                    respuestasBasicas = sharedViewModel.respuestasBasicas.value ?: listOf(true, true, false),
                    uriImagen = sharedViewModel.uriImagen.value,
                    preguntasDinamicas = sharedViewModel.preguntasDinamicas.value ?: emptyList(),
                    resultadoEvolutivo = it
                )

                // Ahora recibimos un mensaje de texto para saber exactamente qué pasa en la consola
                recommendationViewModel.saveTriageToHistory { exito, mensaje ->
                    if (exito) {
                        Log.d("TRIAJE_DB", mensaje)
                    } else {
                        Log.e("TRIAJE_DB", "Fallo al guardar: $mensaje")
                    }
                }
            }
        }
    }
}