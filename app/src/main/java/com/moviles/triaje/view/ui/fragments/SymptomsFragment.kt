package com.moviles.triaje.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.moviles.triaje.R
import com.moviles.triaje.model.Sintoma
import com.moviles.triaje.network.Callback
import com.moviles.triaje.network.FirestoreService
import com.moviles.triaje.view.adapter.SintomasAdapter
import com.moviles.triaje.view.adapter.SymptomListener
import com.moviles.triaje.viewmodel.SymptomViewModel

// 🔥 Implementamos de forma explícita SymptomListener
class SymptomsFragment : Fragment(), SymptomListener {

    private lateinit var rvSintomas: RecyclerView
    private lateinit var btnContinuar: MaterialButton
    private lateinit var ivRegresar: ImageView

    private lateinit var adapter: SintomasAdapter
    private lateinit var symptomViewModel: SymptomViewModel
    private val firestoreService = FirestoreService()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_symptoms, container, false)

        // 1. Inicializar vistas
        rvSintomas = view.findViewById(R.id.rvSintomas)
        btnContinuar = view.findViewById(R.id.btnConsultaContinuar)
        ivRegresar = view.findViewById(R.id.ivSintomasRegresar)

        // 2. Inicializar ViewModel amarrado a la actividad
        symptomViewModel = ViewModelProvider(requireActivity())[SymptomViewModel::class.java]

        // 3. Configurar el RecyclerView pasando 'this' como el Listener
        rvSintomas.layoutManager = GridLayoutManager(requireContext(), 2)
        adapter = SintomasAdapter(this)
        rvSintomas.adapter = adapter

        // 4. Observar cambios en el ViewModel para prender/apagar el botón Continuar
        symptomViewModel.sintomasSeleccionados.observe(viewLifecycleOwner) { seleccionados ->
            btnContinuar.isEnabled = seleccionados.isNotEmpty()
        }

        // 5. Cargar datos desde Firestore
        cargarSintomasDeFirestore()

        // 6. Listeners de navegación
        ivRegresar.setOnClickListener {
            findNavController().navigateUp()
        }

        btnContinuar.setOnClickListener {
            val seleccionados = symptomViewModel.sintomasSeleccionados.value.orEmpty()

            // Empaquetamos la lista de síntomas seleccionados en un ArrayList serializable
            val bundle = Bundle().apply {
                putSerializable("sintomas_seleccionados", ArrayList(seleccionados))
            }

            // Navegamos pasando el bundle con los datos salvaguardados
            findNavController().navigate(
                R.id.action_symptomsFragment_to_basicQuestionsFragment,
                bundle
            )
        }

        return view
    }

    // 🔥 Método de la interfaz que reacciona a cada toque en el RecyclerView
    override fun onSintomaClicked(sintoma: Sintoma, position: Int) {
        // Conmutamos el estado de selección del modelo local
        sintoma.isSelected = !sintoma.isSelected

        // Notificamos al ViewModel del cambio
        symptomViewModel.alternarSeleccionSintoma(sintoma)

        // Forzamos al adapter a redibujar el ítem específico para actualizar el stroke/fondo
        adapter.notifyItemChanged(position)
    }

    private fun cargarSintomasDeFirestore() {
        firestoreService.obtenerSintomas(object : Callback<List<Sintoma>> {
            override fun onSuccess(result: List<Sintoma>?) {
                if (result != null) {
                    adapter.updateData(result)
                }
            }

            override fun onFailed(exception: Exception) {
                Toast.makeText(requireContext(), "Error: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}