package com.moviles.triaje.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class WoundClassifier(context: Context) {
    private var interpreter: Interpreter? = null

    // Lista de etiquetas en orden alfabético estricto (0 a 6)
    private val labels = listOf(
        "Abrasions",     // 0
        "Bruises",       // 1
        "Burns",         // 2
        "Cut",           // 3
        "Ingrown_nails", // 4
        "Laceration",    // 5
        "Stab_wound"     // 6
    )

    init {
        try {
            interpreter = Interpreter(loadModelFile(context, "best.tflite"))
        } catch (e: Exception) {
            Log.e("IA_DEBUG", "Error al cargar el modelo best.tflite", e)
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

        // 1. Forzar formato ARGB_8888 en software
        val softwareBitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O &&
            bitmap.config == Bitmap.Config.HARDWARE) {
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
        } else {
            bitmap.copy(Bitmap.Config.ARGB_8888, true)
        }

        // 2. Redimensionar exactamente a 224x224 (Misma bilinear que PIL/PyTorch)
        val resizedBitmap = Bitmap.createScaledBitmap(softwareBitmap, 224, 224, true)

        // 3. Convertir a ByteBuffer forzando RGB de 0.0f a 1.0f
        val inputBuffer = convertBitmapToByteBuffer(resizedBitmap)

        // 4. Preparar buffer de salida
        val outputBuffer = Array(1) { FloatArray(labels.size) }

        // 5. Inferencia
        interpreter?.run(inputBuffer, outputBuffer)

        val probabilities = outputBuffer[0]

        // --- LOGCAT DEPURACIÓN ---
        Log.d("IA_DEBUG", "=== Resultados del Análisis TFLite ===")
        probabilities.forEachIndexed { index, score ->
            val label = labels.getOrElse(index) { "Desconocido" }
            Log.d("IA_DEBUG", "Etiqueta[$index] ($label) -> Probabilidad: ${String.format("%.2f", score * 100)}%")
        }

        val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: -1

        return if (maxIndex != -1) {
            labels[maxIndex]
        } else {
            "No se pudo determinar"
        }
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        // 4 bytes por float * 224 * 224 * 3 canales
        val byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)

        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in 0 until height) {
            for (j in 0 until width) {
                val pixel = pixels[i * width + j]

                // Extraer canales con Color de Android para evitar problemas de endianness
                val r = android.graphics.Color.red(pixel) / 255.0f
                val g = android.graphics.Color.green(pixel) / 255.0f
                val b = android.graphics.Color.blue(pixel) / 255.0f

                // Escribir en orden estricto R -> G -> B
                byteBuffer.putFloat(r)
                byteBuffer.putFloat(g)
                byteBuffer.putFloat(b)
            }
        }
        return byteBuffer
    }
}