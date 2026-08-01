package com.moviles.triaje.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.moviles.triaje.R
import com.moviles.triaje.model.Consulta
import com.moviles.triaje.model.Recomendacion
import com.moviles.triaje.model.TriajeResultado
import com.moviles.triaje.view.adapter.RecomendacionAdapter

class RecommendationsFragment : Fragment() {

    private lateinit var rvRecommendations: RecyclerView
    private lateinit var btnFinish: MaterialButton
    private lateinit var btnViewHospitals: MaterialButton
    private lateinit var btnBack: ImageButton
    private lateinit var tvTitle: TextView
    
    private var triajeResultado: TriajeResultado? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recommendations, container, false)
        
        // Obtener el resultado del triaje (pasado por argumentos o desde un ViewModel)
        triajeResultado = arguments?.getSerializable("triaje_resultado") as? TriajeResultado

        initViews(view)
        setupRecyclerView()
        setupListeners()

        return view
    }

    private fun initViews(view: View) {
        rvRecommendations = view.findViewById(R.id.rvRecommendations)
        btnFinish = view.findViewById(R.id.btnFinish)
        btnViewHospitals = view.findViewById(R.id.btnViewHospitals)
        btnBack = view.findViewById(R.id.btnBack)
        tvTitle = view.findViewById(R.id.tvTitle)
    }

    private fun setupRecyclerView() {
        val recomendaciones = triajeResultado?.recomendaciones ?: emptyList()
        rvRecommendations.adapter = RecomendacionAdapter(recomendaciones)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        btnViewHospitals.setOnClickListener {
            saveConsultaAndNavigate(R.layout.fragment_hospitales)
        }

        btnFinish.setOnClickListener {
            saveConsultaAndNavigate(R.layout.fragment_home)
        }
    }

    private fun saveConsultaAndNavigate(destinationId: Int) {
        // Lógica para guardar en Firebase/Base de Datos
        // Aquí se construiría el objeto Consulta final usando los datos recolectados en el flujo
        
        Toast.makeText(context, "Consulta guardada en el historial", Toast.LENGTH_SHORT).show()
        
        // Navegar al destino seleccionado
        findNavController().navigate(destinationId)
    }
}
