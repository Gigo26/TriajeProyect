package com.moviles.triaje.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.moviles.triaje.R
import com.moviles.triaje.viewmodel.HomeViewModel
import com.moviles.triaje.viewmodel.HomeViewModel.TipoPaciente
import androidx.navigation.fragment.findNavController
import com.moviles.triaje.viewmodel.SymptomViewModel
import com.moviles.triaje.viewmodel.VerInformacionViewModel

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var userViewModel: VerInformacionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        viewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
        userViewModel = ViewModelProvider(this)[VerInformacionViewModel::class.java]
        
        viewModel.limpiarDatos()

        val symptomViewModel = ViewModelProvider(requireActivity())[SymptomViewModel::class.java]
        symptomViewModel.limpiarDatos()

        val cvNino = view.findViewById<MaterialCardView>(R.id.cvConsultaNino)
        val cvAdulto = view.findViewById<MaterialCardView>(R.id.cvConsultaAdulto)
        val cvAdultoMayor = view.findViewById<MaterialCardView>(R.id.cvConsultaAdultoMayor)
        val cvGestante = view.findViewById<MaterialCardView>(R.id.cvConsultaGestante)
        val btnContinuar = view.findViewById<MaterialButton>(R.id.btnConsultaContinuar)

        val listaCards = listOf(cvNino, cvAdulto, cvAdultoMayor, cvGestante)
        btnContinuar.isEnabled = false

        viewModel.isContinuarEnabled.observe(viewLifecycleOwner) { isEnabled ->
            btnContinuar.isEnabled = isEnabled
        }

        cvNino.setOnClickListener {
            viewModel.seleccionarPaciente(TipoPaciente.NINO)
            actualizarSeleccionVisual(cvNino, listaCards)
        }

        cvAdulto.setOnClickListener {
            viewModel.seleccionarPaciente(TipoPaciente.ADULTO)
            actualizarSeleccionVisual(cvAdulto, listaCards)
        }

        cvAdultoMayor.setOnClickListener {
            viewModel.seleccionarPaciente(TipoPaciente.ADULTO_MAYOR)
            actualizarSeleccionVisual(cvAdultoMayor, listaCards)
        }

        cvGestante.setOnClickListener {
            viewModel.seleccionarPaciente(TipoPaciente.GESTANTE)
            actualizarSeleccionVisual(cvGestante, listaCards)
        }

        btnContinuar.setOnClickListener {
            val seleccionado = viewModel.pacienteSeleccionado.value
            if (seleccionado != null) {
                findNavController().navigate(R.id.action_homeFragment_to_symptomFragment)
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val tvGreeting = requireActivity().findViewById<TextView>(R.id.tvGreeting)
        
        userViewModel.usuario.observe(viewLifecycleOwner) { usuario ->
            usuario?.let {
                tvGreeting?.text = getString(R.string.hello_user, it.us_nombre)
            }
        }
        
        userViewModel.cargarDatosUsuario()
    }

    private fun actualizarSeleccionVisual(cardSeleccionada: MaterialCardView, todasLasCards: List<MaterialCardView>) {
        todasLasCards.forEach { card ->
            if (card == cardSeleccionada) {
                card.isChecked = true
                card.setStrokeColor(android.content.res.ColorStateList.valueOf(resources.getColor(R.color.black))) 
                card.strokeWidth = 4 
            } else {
                card.isChecked = false
                card.strokeWidth = 0
            }
        }
    }
}