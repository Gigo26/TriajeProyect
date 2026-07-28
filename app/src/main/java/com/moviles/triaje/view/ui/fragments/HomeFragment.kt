package com.moviles.triaje.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // 🔥 MODIFICADO: Vinculamos el ViewModel a la Actividad (requireActivity)
        // para que los datos no se destruyan al cambiar de pantalla.
        viewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
        viewModel.limpiarDatos()

        val symptomViewModel = ViewModelProvider(requireActivity())[SymptomViewModel::class.java]
        symptomViewModel.limpiarDatos()

        // 1. Mapeamos las tarjetas y el botón
        val cvNino = view.findViewById<MaterialCardView>(R.id.cvConsultaNino)
        val cvAdulto = view.findViewById<MaterialCardView>(R.id.cvConsultaAdulto)
        val cvAdultoMayor = view.findViewById<MaterialCardView>(R.id.cvConsultaAdultoMayor)
        val cvGestante = view.findViewById<MaterialCardView>(R.id.cvConsultaGestante)
        val btnContinuar = view.findViewById<MaterialButton>(R.id.btnConsultaContinuar)

        // Metemos las tarjetas en una lista para manejarlas fácilmente en lote
        val listaCards = listOf(cvNino, cvAdulto, cvAdultoMayor, cvGestante)

        // 2. El botón inicia apagado por defecto
        btnContinuar.isEnabled = false

        // OBSERVADOR DEL BOTÓN: El fragmento escucha al ViewModel para prender/apagar el botón
        viewModel.isContinuarEnabled.observe(viewLifecycleOwner) { isEnabled ->
            btnContinuar.isEnabled = isEnabled
        }

        // 3. EVENTOS CLICK: Pasamos el reporte al ViewModel y manejamos el estado visual
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

        // Acción del botón Continuar
        btnContinuar.setOnClickListener {
            val seleccionado = viewModel.pacienteSeleccionado.value

            if (seleccionado != null) {
                // Notificación visual rápida al usuario
                Toast.makeText(requireContext(), "Avanzando con tipo: $seleccionado", Toast.LENGTH_SHORT).show()

                // Navegamos al selector de síntomas usando tu nav_main
                findNavController().navigate(
                    R.id.action_homeFragment_to_symptomFragment
                )
            }
        }

        return view
    }

    /**
     * Función auxiliar para asegurarse de que SOLO la tarjeta seleccionada se marque
     * y las demás se limpien por completo.
     */
    private fun actualizarSeleccionVisual(cardSeleccionada: MaterialCardView, todasLasCards: List<MaterialCardView>) {
        todasLasCards.forEach { card ->
            if (card == cardSeleccionada) {
                card.isChecked = true
                card.setStrokeColor(android.content.res.ColorStateList.valueOf(resources.getColor(R.color.black))) // Borde destacado
                card.strokeWidth = 4 // Ancho del borde al seleccionar
            } else {
                card.isChecked = false
                card.strokeWidth = 0 // Quitamos el borde a las que no se eligieron
            }
        }
    }
}