package com.moviles.triaje.viewmodel

import com.moviles.triaje.network.FirestoreService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moviles.triaje.model.Historial
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistorialViewModel(
    private val repository: FirestoreService = FirestoreService()
) : ViewModel() {

    sealed class HistorialUiState {
        object Cargando : HistorialUiState()
        object Vacio : HistorialUiState()
        data class Exito(val datos: List<Historial>) : HistorialUiState()
        data class Error(val mensaje: String) : HistorialUiState()
    }

    private val _uiState = MutableStateFlow<HistorialUiState>(HistorialUiState.Cargando)
    val uiState: StateFlow<HistorialUiState> = _uiState

    fun cargarHistorial(userId: String) {
        viewModelScope.launch {
            repository.obtenerHistorial(userId).collect { result ->
                _uiState.value = if (result.isSuccess) {
                    val lista = result.getOrNull() ?: emptyList()
                    if (lista.isEmpty()) HistorialUiState.Vacio
                    else HistorialUiState.Exito(lista)
                } else {
                    HistorialUiState.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
                }
            }
        }
    }
}

