package com.moviles.triaje.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    // Representamos los 4 tipos de pacientes de forma clara
    enum class TipoPaciente {
        NINO, ADULTO, ADULTO_MAYOR, GESTANTE
    }

    // Guarda el tipo de paciente seleccionado (nulo al principio)
    private val _pacienteSeleccionado = MutableLiveData<TipoPaciente?>(null)
    val pacienteSeleccionado: LiveData<TipoPaciente?> get() = _pacienteSeleccionado

    // Controla si el botón continuar está habilitado
    private val _isContinuarEnabled = MutableLiveData<Boolean>(false)
    val isContinuarEnabled: LiveData<Boolean> get() = _isContinuarEnabled

    // Función que se ejecuta cuando el usuario toca una tarjeta
    fun seleccionarPaciente(tipo: TipoPaciente) {
        _pacienteSeleccionado.value = tipo
        _isContinuarEnabled.value = true // En cuanto elija uno, activamos el botón
    }

    fun limpiarDatos() {
        _pacienteSeleccionado.value = null
        _isContinuarEnabled.value = false
    }
}