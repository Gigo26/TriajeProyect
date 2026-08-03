package com.moviles.triaje.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.moviles.triaje.R
import com.moviles.triaje.view.adapter.RecomendacionAdapter
import com.moviles.triaje.viewmodel.RecommendationViewModel

class RecommendationsFragment : Fragment() {

    private lateinit var viewModel: RecommendationViewModel
    private val recomendacionAdapter = RecomendacionAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recommendations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Nota el 'requireActivity()' - Esto recupera el mismo ViewModel que usamos en ResultFragment
        viewModel = ViewModelProvider(requireActivity())[RecommendationViewModel::class.java]

        setupRecyclerView(view)
        setupClickListeners(view)
        observeViewModel()
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

        // Ya no necesitamos guardar aquí, el usuario simplemente navega
        view.findViewById<MaterialButton>(R.id.btnViewHospitals)?.setOnClickListener {
            findNavController().navigate(R.id.action_recommendationsFragment_to_hospitalesFragment)
        }

        view.findViewById<MaterialButton>(R.id.btnFinish)?.setOnClickListener {
            findNavController().navigate(R.id.action_recommendationsFragment_to_homeFragment)
        }
    }

    private fun observeViewModel() {
        viewModel.listaRecomendacionesUI.observe(viewLifecycleOwner) { lista ->
            recomendacionAdapter.submitList(lista)
        }
    }
}