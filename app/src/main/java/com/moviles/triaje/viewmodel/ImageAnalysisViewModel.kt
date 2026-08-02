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

    private val _analysisResult = MutableLiveData<String?>()
    val analysisResult: LiveData<String?> get() = _analysisResult

    private val _recommendation = MutableLiveData<String?>()
    val recommendation: LiveData<String?> get() = _recommendation

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
        Log.d("IA_DEBUG", "Iniciando análisis de imagen...")
        val bitmap = uriToBitmap(uri)
        if (bitmap != null) {
            val result = classifier.classify(bitmap)
            Log.d("IA_DEBUG", "Resultado final ganador: $result")
            _analysisResult.value = result
            _recommendation.value = getRecommendation(result)
        } else {
            Log.e("IA_DEBUG", "Error: No se pudo convertir la URI a Bitmap")
            _analysisResult.value = "Error al procesar imagen"
            _recommendation.value = "Intente seleccionar o tomar la foto nuevamente."
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

    private fun getRecommendation(result: String): String {
        return when (result) {
            "Burns" -> "Aplicar agua fría (no helada) por 10 minutos. No reventar ampollas ni aplicar remedios caseros."
            "Stab_wound", "Cut", "Laceration" -> "Presionar con una gasa limpia para detener el sangrado. Si hay un objeto incrustado, no lo retire."
            "Bruises", "Abrasions" -> "Lavar cuidadosamente la zona con agua y jabón. Aplicar compresas frías para reducir la inflamación."
            "Ingrown_nails" -> "Remojar en agua tibia con sal y evitar calzado apretado. Consulte a podología."
            else -> "Mantenga la zona limpia y seca. Si nota cambios de color o fiebre, busque atención médica."
        }
    }
}