package com.moviles.triaje.viewmodel

import android.app.Application
import com.moviles.triaje.network.FirestoreService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moviles.triaje.R
import com.moviles.triaje.model.Consulta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistorialViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: FirestoreService = FirestoreService()
    private val context = application.applicationContext

    sealed class HistorialUiState {
        object Cargando : HistorialUiState()
        object Vacio : HistorialUiState()
        data class Exito(val datos: List<Consulta>) : HistorialUiState()
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
                    val errorMsg = result.exceptionOrNull()?.message ?: context.getString(R.string.error_loading_content)
                    HistorialUiState.Error(errorMsg)
                }
            }
        }
    }
}

