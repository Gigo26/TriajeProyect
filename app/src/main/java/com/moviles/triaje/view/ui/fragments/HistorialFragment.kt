package com.moviles.triaje.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.moviles.triaje.R
import com.moviles.triaje.databinding.FragmentHistorialBinding
import com.moviles.triaje.view.adapter.HistorialAdapter
import com.moviles.triaje.viewmodel.HistorialViewModel
import kotlinx.coroutines.launch

class HistorialFragment : Fragment() {

    private var _binding: FragmentHistorialBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistorialViewModel by viewModels()
    private lateinit var adapter: HistorialAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistorialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = HistorialAdapter { consulta ->
            val dialog = HistorialDialog.newInstance(consulta)
            dialog.show(childFragmentManager, "HistorialDialog")
        }
        binding.rvHistorial.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistorial.adapter = adapter
        
        binding.tvVacio.text = getString(R.string.history_empty)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModel.cargarHistorial(userId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is HistorialViewModel.HistorialUiState.Cargando -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.tvVacio.visibility = View.GONE
                    }
                    is HistorialViewModel.HistorialUiState.Exito -> {
                        binding.progressBar.visibility = View.GONE
                        binding.tvVacio.visibility = View.GONE
                        adapter.actualizarLista(state.datos)
                    }
                    is HistorialViewModel.HistorialUiState.Vacio -> {
                        binding.progressBar.visibility = View.GONE
                        binding.tvVacio.visibility = View.VISIBLE
                    }
                    is HistorialViewModel.HistorialUiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), state.mensaje, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}