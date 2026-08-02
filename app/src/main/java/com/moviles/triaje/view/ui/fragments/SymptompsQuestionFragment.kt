package com.moviles.triaje.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.moviles.triaje.R
import com.moviles.triaje.model.DecisionContext
import com.moviles.triaje.model.Sintoma
import com.moviles.triaje.model.Pregunta
import com.moviles.triaje.view.adapter.QuestionListener
import com.moviles.triaje.view.adapter.QuestionAdapter
import com.moviles.triaje.viewmodel.SharedTriageViewModel
import com.moviles.triaje.viewmodel.SymptompsQuestionsViewModel

class SymptompsQuestionFragment : Fragment() {

    private lateinit var viewModel: SymptompsQuestionsViewModel
    private lateinit var sharedViewModel: SharedTriageViewModel
    private lateinit var questionsAdapter: QuestionAdapter

    private lateinit var rvQuestions: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnFinalizarTriaje: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_symptoms_questions, container, false)

        // 1. Vincular vistas tradicionalmente
        rvQuestions = view.findViewById(R.id.rvQuestions)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        progressBar = view.findViewById(R.id.progressBar)
        btnFinalizarTriaje = view.findViewById(R.id.btnQuestionsEvaluar)

        // 2. Inicializar ViewModel
        viewModel = ViewModelProvider(this)[SymptompsQuestionsViewModel::class.java]
        sharedViewModel = ViewModelProvider(requireActivity())[SharedTriageViewModel::class.java]

        // 3. Configurar RecyclerView
        setupRecyclerView()

        // 4. Observar cambios del ViewModel
        setupObservers()

        // 5. Recuperar síntomas desde el Shared ViewModel
        val sintomasSeleccionados = sharedViewModel.sintomasSeleccionados.value.orEmpty()
        viewModel.cargarPreguntasPorSintomas(sintomasSeleccionados)

        // 6. Configurar evento click
        btnFinalizarTriaje.setOnClickListener {
            if (viewModel.verificarPreguntasCompletas()) {

                // Guardar las preguntas respondidas en el Shared ViewModel
                sharedViewModel.setPreguntasDinamicas(viewModel.listaPreguntas.value.orEmpty())

                findNavController().navigate(
                    R.id.action_symptompsQuestionFragment_to_resultFragment
                )
            } else {
                Toast.makeText(requireContext(), "Por favor, responde todas las preguntas obligatorias", Toast.LENGTH_LONG).show()
            }
        }

        return view
    }

    private fun setupRecyclerView() {
        questionsAdapter = QuestionAdapter(object : QuestionListener {
            override fun onOptionSelected(question: Pregunta, optionIndex: Int, position: Int) {
                viewModel.actualizarRespuestaPregunta(question.id, optionIndex)
            }
        })

        rvQuestions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = questionsAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        viewModel.listaPreguntas.observe(viewLifecycleOwner) { listaPreguntas ->
            questionsAdapter.listPreguntas = ArrayList(listaPreguntas)
            questionsAdapter.notifyDataSetChanged()

            if (listaPreguntas.isEmpty()) {
                tvEmptyState.visibility = View.VISIBLE
                btnFinalizarTriaje.visibility = View.GONE
            } else {
                tvEmptyState.visibility = View.GONE
                btnFinalizarTriaje.visibility = View.VISIBLE
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { mensajeError ->
            if (!mensajeError.isNullOrEmpty()) {
                Toast.makeText(requireContext(), mensajeError, Toast.LENGTH_LONG).show()
            }
        }
    }
}