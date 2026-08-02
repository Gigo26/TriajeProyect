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
import com.moviles.triaje.model.Usuario
import com.moviles.triaje.view.adapter.HospitalAdapter
import com.moviles.triaje.view.adapter.HospitalListener
import com.moviles.triaje.viewmodel.HospitalesViewModel
import com.moviles.triaje.viewmodel.MainViewModel

class HospitalesFragment : Fragment() {

    private lateinit var viewModel: HospitalesViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var hospitalAdapter: HospitalAdapter

    private lateinit var rvHospitales: RecyclerView
    private lateinit var ivRegresar: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView

    private var currentUser: Usuario? = null

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            viewModel.cargarHospitalesCercanos()
        } else {
            Toast.makeText(requireContext(), "Se requiere permiso de ubicación", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_hospitales, container, false)
        viewModel = ViewModelProvider(this)[HospitalesViewModel::class.java]
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        initViews(view)
        setupObservers()
        checkLocationPermissions()

        return view
    }

    private fun initViews(view: View) {
        rvHospitales = view.findViewById(R.id.rvHospitales)
        ivRegresar = view.findViewById(R.id.ivHospitalesRegresar)
        progressBar = view.findViewById(R.id.progressBar)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)

        ivRegresar.setOnClickListener { findNavController().navigateUp() }
    }

    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            viewModel.cargarHospitalesCercanos()
        } else {
            locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    private fun setupRecyclerView() {
        hospitalAdapter = HospitalAdapter(object : HospitalListener {
            override fun onHospitalClick(hospital: Hospital) {
                HospitalDialog.newInstance(hospital).show(childFragmentManager, "HospitalDetail")
            }

            override fun onFavoriteClick(hospital: Hospital, isFavorite: Boolean) {
                viewModel.toggleFavorito(hospital.hos_name, isFavorite)
            }
        }, currentUser?.hos_fav ?: emptyList())

        rvHospitales.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = hospitalAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        mainViewModel.usuario.observe(viewLifecycleOwner) { user ->
            currentUser = user
            setupRecyclerView() // Recargamos el adapter con los nuevos favoritos
            viewModel.cargarHospitalesCercanos()
        }

        viewModel.listaHospitales.observe(viewLifecycleOwner) { lista ->
            hospitalAdapter.updateData(lista)
            tvEmptyState.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
            rvHospitales.visibility = if (lista.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrEmpty()) Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
        }
    }
}
