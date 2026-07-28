package com.moviles.triaje.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.moviles.triaje.model.Sintoma

class ImageAnalysisViewModel : ViewModel() {

    private val _sintomasRecuperados = MutableLiveData<List<Sintoma>>()
    val sintomasRecuperados: LiveData<List<Sintoma>> get() = _sintomasRecuperados

    private val _imageUri = MutableLiveData<Uri?>()
    val imageUri: LiveData<Uri?> get() = _imageUri

    fun guardarSintomas(lista: List<Sintoma>) {
        _sintomasRecuperados.value = lista
    }

    fun setImageUri(uri: Uri?) {
        _imageUri.value = uri
    }
}