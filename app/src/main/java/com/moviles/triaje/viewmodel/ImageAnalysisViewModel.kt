package com.moviles.triaje.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.moviles.triaje.ml.WoundClassifier
import com.moviles.triaje.model.Sintoma

class ImageAnalysisViewModel(application: Application) : AndroidViewModel(application) {

    private val classifier = WoundClassifier(application)

    private val _sintomasRecuperados = MutableLiveData<List<Sintoma>>()
    val sintomasRecuperados: LiveData<List<Sintoma>> get() = _sintomasRecuperados

    private val _imageUri = MutableLiveData<Uri?>()
    val imageUri: LiveData<Uri?> get() = _imageUri

    // Solo guardará la clase cruda devuelta por la IA (Ej: "Burns", "Cut")
    private val _analysisResult = MutableLiveData<String?>()
    val analysisResult: LiveData<String?> get() = _analysisResult

    fun guardarSintomas(lista: List<Sintoma>) {
        _sintomasRecuperados.value = lista
    }

    fun setImageUri(uri: Uri?) {
        _imageUri.value = uri
        if (uri != null) {
            Log.d("IA_DEBUG", "Nueva URI de imagen recibida: $uri")
            analyzeImage(uri)
        }
    }

    private fun analyzeImage(uri: Uri) {
        Log.d("IA_DEBUG", "Iniciando análisis de imagen para Algoritmo Evolutivo...")
        val bitmap = uriToBitmap(uri)
        if (bitmap != null) {
            // El resultado es solo la etiqueta: "Burns", "Cut", "Laceration", etc.
            val result = classifier.classify(bitmap)
            Log.d("IA_DEBUG", "Resultado IA para Motor Evolutivo: $result")
            _analysisResult.value = result
        } else {
            Log.e("IA_DEBUG", "Error: No se pudo convertir la URI a Bitmap")
            _analysisResult.value = "Error"
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}