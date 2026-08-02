import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.moviles.triaje.R
import com.moviles.triaje.model.DecisionContext
import com.moviles.triaje.view.adapter.RecomendacionAdapter
import com.moviles.triaje.viewmodel.RecommendationViewModel

/**
 * Fragmento de Recomendaciones (Capa UI).
 * Solo se encarga de la visualización y delegación de eventos.
 */
class RecommendationsFragment : Fragment() {

    private lateinit var viewModel: RecommendationViewModel
    private val recomendacionAdapter = RecomendacionAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recommendations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[RecommendationViewModel::class.java]

        setupRecyclerView(view)
        setupClickListeners(view)
        observeViewModel()

        // Recuperar contexto del triaje desde los argumentos
        @Suppress("DEPRECATION")
        val decisionContext = arguments?.getSerializable("decision_context") as? DecisionContext
        decisionContext?.let { viewModel.processTriage(it) }
    }

    private fun setupRecyclerView(view: View) {
        val rv = view.findViewById<RecyclerView>(R.id.rvRecommendations)
        rv.adapter = recomendacionAdapter
    }

    private fun setupClickListeners(view: View) {
        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }

        view.findViewById<MaterialButton>(R.id.btnViewHospitals).setOnClickListener {
            viewModel.saveTriageToHistory()
            findNavController().navigate(R.id.navHospitalesFragment)
        }

        view.findViewById<MaterialButton>(R.id.btnFinish).setOnClickListener {
            viewModel.saveTriageToHistory()
            Toast.makeText(context, "Consulta guardada en el historial", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.navHomeFragment)
        }
    }

    private fun observeViewModel() {
        viewModel.consultaFinal.observe(viewLifecycleOwner) { consulta ->
            recomendacionAdapter.submitList(consulta.con_recomendaciones)
        }
    }
}

/*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.moviles.triaje.R
import com.moviles.triaje.model.Recomendacion

/**
 * Adaptador moderno usando ListAdapter (DiffUtil) para mejor rendimiento.
 */
class RecomendacionAdapter : ListAdapter<Recomendacion, RecomendacionAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvStepNumber: TextView = view.findViewById(R.id.tvStepNumber)
        val tvStepTitle: TextView = view.findViewById(R.id.tvStepTitle)
        val tvStepDescription: TextView = view.findViewById(R.id.tvStepDescription)
        val ivStepIcon: ImageView = view.findViewById(R.id.ivStepIcon)

        fun bind(item: Recomendacion) {
            tvStepNumber.text = item.step.toString()
            tvStepTitle.text = item.titulo
            tvStepDescription.text = item.recomendacion
            ivStepIcon.setImageResource(if (item.iconResId != 0) item.iconResId else R.drawable.ic_info)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recommendation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    object DiffCallback : DiffUtil.ItemCallback<Recomendacion>() {
        override fun areItemsTheSame(oldItem: Recomendacion, newItem: Recomendacion) = oldItem.step == newItem.step
        override fun areContentsTheSame(oldItem: Recomendacion, newItem: Recomendacion) = oldItem == newItem
    }
}

/*
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
*/