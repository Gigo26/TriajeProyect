package com.moviles.triaje.view.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    // Manejo de permisos de ubicación
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            viewModel.cargarHospitalesCercanos()
        } else {
            Toast.makeText(requireContext(), "Se requiere permiso de ubicación para ver hospitales cercanos", Toast.LENGTH_LONG).show()
            tvEmptyState.visibility = View.VISIBLE
            tvEmptyState.text = "Sin acceso a la ubicación"
        }
    }

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

        // 6. Verificar permisos y cargar datos
        checkLocationPermissions()

        return view
    }

    private fun checkLocationPermissions() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.cargarHospitalesCercanos()
            }
            else -> {
                locationPermissionLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                )
            }
        }
    }

    private fun setupRecyclerView() {
        hospitalAdapter = HospitalAdapter(object : HospitalListener {
            override fun onHospitalClick(hospital: Hospital) {
                // Toast.makeText(requireContext(), "Seleccionado: ${hospital.hos_name}", Toast.LENGTH_SHORT).show()
                // Aquí podrías abrir detalles o mapas
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
