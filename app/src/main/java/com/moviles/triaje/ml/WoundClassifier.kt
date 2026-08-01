package com.moviles.triaje.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class WoundClassifier(context: Context) {
    private var interpreter: Interpreter? = null

    // Etiquetas del modelo (Actualizadas según tu best.tflite)
    private val labels = listOf("Laceration", "Stab_wound", "Ingrown_nails", "Cut", "Burns", "Bruises", "Abrasions")

    init {
        try {
            interpreter = Interpreter(loadModelFile(context, "best.tflite"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadModelFile(context: Context, modelPath: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun classify(bitmap: Bitmap): String {
        if (interpreter == null) return "Error: Modelo no cargado"

        // Solución al error Config#HARDWARE: Asegurar que el bitmap sea accesible por píxeles
        val softwareBitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && 
            bitmap.config == Bitmap.Config.HARDWARE) {
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
        } else {
            bitmap
        }

        // 1. Redimensionar el Bitmap a 224x224
        val resizedBitmap = Bitmap.createScaledBitmap(softwareBitmap, 224, 224, true)
        
        // 2. Convertir Bitmap a ByteBuffer
        val inputBuffer = convertBitmapToByteBuffer(resizedBitmap)
        
        // 3. Preparar el buffer de salida (Ajusta el tamaño según tu número de clases)
        val outputBuffer = Array(1) { FloatArray(labels.size) }

        // 4. Ejecutar la inferencia
        interpreter?.run(inputBuffer, outputBuffer)

        // 5. Obtener el índice con mayor probabilidad
        val probabilities = outputBuffer[0]
        val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: -1
        
        return if (maxIndex != -1) {
            labels[maxIndex]
        } else {
            "No se pudo determinar"
        }
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3) // 4 bytes por float, 224x224 pixeles, 3 canales RGB
        byteBuffer.order(ByteOrder.nativeOrder())
        
        val intValues = IntArray(224 * 224)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        
        var pixel = 0
        for (i in 0 until 224) {
            for (j in 0 until 224) {
                val value = intValues[pixel++]
                // Normalización (0-255 a 0.0-1.0)
                byteBuffer.putFloat(((value shr 16 and 0xFF) / 255.0f))
                byteBuffer.putFloat(((value shr 8 and 0xFF) / 255.0f))
                byteBuffer.putFloat(((value and 0xFF) / 255.0f))
            }
        }
        return byteBuffer
    }
}
