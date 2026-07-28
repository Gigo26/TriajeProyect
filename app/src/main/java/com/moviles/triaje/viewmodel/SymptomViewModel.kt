package com.moviles.triaje.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.moviles.triaje.model.Sintoma

class SymptomViewModel : ViewModel() {

    // Lista en tiempo real de los síntomas seleccionados por el usuario
    private val _sintomasSeleccionados = MutableLiveData<List<Sintoma>>(emptyList())
    val sintomasSeleccionados: LiveData<List<Sintoma>> get() = _sintomasSeleccionados

    // Agrega o remueve un síntoma de la selección temporal
    fun alternarSeleccionSintoma(sintoma: Sintoma) {
        val listaActual = _sintomasSeleccionados.value.orEmpty().toMutableList()

        if (sintoma.isSelected) {
            // Si el objeto local se marcó como seleccionado, lo agregamos a la lista del ViewModel si no existe
            if (!listaActual.any { it.id == sintoma.id }) {
                listaActual.add(sintoma)
            }
        } else {
            // Si se desmarcó, lo removemos basándonos en su ID único
            listaActual.removeAll { it.id == sintoma.id }
        }

        _sintomasSeleccionados.value = listaActual
    }

    fun limpiarDatos() {
        _sintomasSeleccionados.value = emptyList()
    }
}