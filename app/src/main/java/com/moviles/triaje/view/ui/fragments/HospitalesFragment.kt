package com.moviles.triaje.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
import com.moviles.triaje.model.Hospital
import com.moviles.triaje.view.adapter.HospitalAdapter
import com.moviles.triaje.view.adapter.HospitalListener
import com.moviles.triaje.viewmodel.HospitalesViewModel

class HospitalesFragment : Fragment() {

    private lateinit var viewModel: HospitalesViewModel
    private lateinit var hospitalAdapter: HospitalAdapter

    private lateinit var rvHospitales: RecyclerView
    private lateinit var ivRegresar: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_hospitales, container, false)

        // 1. Vincular componentes de la interfaz
        rvHospitales = view.findViewById(R.id.rvHospitales)
        ivRegresar = view.findViewById(R.id.ivHospitalesRegresar)
        progressBar = view.findViewById(R.id.progressBar)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)

        // 2. Inicializar ViewModel
        viewModel = ViewModelProvider(this)[HospitalesViewModel::class.java]

        // 3. Configurar RecyclerView
        setupRecyclerView()

        // 4. Configurar Observadores del ViewModel
        setupObservers()

        // 5. Configurar Eventos de Clic
        ivRegresar.setOnClickListener {
            findNavController().navigateUp()
        }

        // 6. Cargar datos desde Firebase Firestore
        viewModel.cargarHospitales()

        return view
    }

    private fun setupRecyclerView() {
        hospitalAdapter = HospitalAdapter(object : HospitalListener {
            override fun onHospitalClick(hospital: Hospital) {
                Toast.makeText(requireContext(), "Seleccionado: ${hospital.nombre}", Toast.LENGTH_SHORT).show()
            }
        })

        rvHospitales.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = hospitalAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        viewModel.listaHospitales.observe(viewLifecycleOwner) { lista ->
            hospitalAdapter.updateData(lista)

            if (lista.isEmpty()) {
                tvEmptyState.visibility = View.VISIBLE
                rvHospitales.visibility = View.GONE
            } else {
                tvEmptyState.visibility = View.GONE
                rvHospitales.visibility = View.VISIBLE
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