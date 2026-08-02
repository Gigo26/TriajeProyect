package com.moviles.triaje.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.moviles.triaje.R
import com.moviles.triaje.view.adapter.RecomendacionAdapter
import com.moviles.triaje.viewmodel.RecommendationViewModel
import com.moviles.triaje.viewmodel.SharedTriageViewModel

class RecommendationsFragment : Fragment() {

    private lateinit var viewModel: RecommendationViewModel
    private lateinit var sharedViewModel: SharedTriageViewModel
    private val recomendacionAdapter = RecomendacionAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recommendations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar ViewModels
        viewModel = ViewModelProvider(this)[RecommendationViewModel::class.java]
        sharedViewModel = ViewModelProvider(requireActivity())[SharedTriageViewModel::class.java]

        setupRecyclerView(view)
        setupClickListeners(view)
        observeViewModel()

        // Consolidar consulta final usando los datos del SharedViewModel
        sharedViewModel.resultadoEvolutivo.value?.let { resultado ->
            viewModel.consolidarConsulta(
                tipoPaciente = sharedViewModel.tipoPaciente.value ?: "ADULTO",
                sintomas = sharedViewModel.sintomasSeleccionados.value ?: emptyList(),
                respuestasBasicas = sharedViewModel.respuestasBasicas.value ?: listOf(true, true, false),
                uriImagen = sharedViewModel.uriImagen.value,
                preguntasDinamicas = sharedViewModel.preguntasDinamicas.value ?: emptyList(),
                resultadoEvolutivo = resultado
            )
        }
    }

    private fun setupRecyclerView(view: View) {
        val rv = view.findViewById<RecyclerView>(R.id.rvRecommendations)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = recomendacionAdapter
    }

    private fun setupClickListeners(view: View) {
        view.findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            findNavController().popBackStack()
        }

        view.findViewById<MaterialButton>(R.id.btnViewHospitals)?.setOnClickListener {
            viewModel.saveTriageToHistory { exito ->
                if (exito) {
                    Toast.makeText(context, "Consulta guardada en el historial", Toast.LENGTH_SHORT).show()
                }
                findNavController().navigate(R.id.action_recommendationsFragment_to_hospitalesFragment)
            }
        }

        view.findViewById<MaterialButton>(R.id.btnFinish)?.setOnClickListener {
            viewModel.saveTriageToHistory { exito ->
                if (exito) {
                    Toast.makeText(context, "Consulta guardada en el historial", Toast.LENGTH_SHORT).show()
                }
                findNavController().navigate(R.id.action_recommendationsFragment_to_homeFragment)
            }
        }
    }

    private fun observeViewModel() {
        // Observamos la lista específica para la UI en lugar del mapa de Firebase
        viewModel.listaRecomendacionesUI.observe(viewLifecycleOwner) { lista ->
            recomendacionAdapter.submitList(lista)
        }
    }
}
