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
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

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

        // 1. Forzar formato ARGB_8888 (Para evitar errores de hardware en algunos Androids)
        val softwareBitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O &&
            bitmap.config == Bitmap.Config.HARDWARE) {
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
        } else {
            bitmap.copy(Bitmap.Config.ARGB_8888, true)
        }

        // 2. RECORTAR LA IMAGEN COMO LO HACE PYTHON (Center Crop)
        val croppedBitmap = centerCrop(softwareBitmap, 224)

        // 3. Convertir a ByteBuffer (Verificando dinámicamente si es NCHW o NHWC)
        val inputBuffer = convertBitmapToByteBuffer(croppedBitmap)

        // 4. Preparar buffer de salida
        val outputBuffer = Array(1) { FloatArray(labels.size) }

        // 5. Inferencia
        interpreter?.run(inputBuffer, outputBuffer)

        // 6. APLICAR SOFTMAX PARA CONVERTIR LOGITS EN PORCENTAJES REALES
        val logits = outputBuffer[0]
        val probabilities = applySoftmax(logits)

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

    // --- FUNCIONES CLAVE AÑADIDAS ---

    // Transforma los valores crudos del modelo en probabilidades (0.0 a 1.0)
    private fun applySoftmax(logits: FloatArray): FloatArray {
        val maxLogit = logits.maxOrNull() ?: 0f
        val expLogits = logits.map { exp((it - maxLogit).toDouble()).toFloat() }
        val sumExp = expLogits.sum()
        return expLogits.map { it / sumExp }.toFloatArray()
    }

    // Recorta el centro de la imagen para no aplastarla/deformarla
    private fun centerCrop(bitmap: Bitmap, targetSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        // Escalar la imagen respetando la proporción (el lado menor será 224)
        val scale = targetSize.toFloat() / min(width, height).toFloat()
        val scaledWidth = Math.round(width * scale)
        val scaledHeight = Math.round(height * scale)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)

        // Recortar exactamente el centro
        val xOffset = max(0, (scaledWidth - targetSize) / 2)
        val yOffset = max(0, (scaledHeight - targetSize) / 2)

        return Bitmap.createBitmap(scaledBitmap, xOffset, yOffset, targetSize, targetSize)
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        // Consultamos dinámicamente qué formato espera tu modelo exportado
        val inputTensor = interpreter?.getInputTensor(0)
        val shape = inputTensor?.shape() // [1, 224, 224, 3] (NHWC) o [1, 3, 224, 224] (NCHW)
        val isNCHW = shape != null && shape.size == 4 && shape[1] == 3

        val byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)

        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        if (isNCHW) {
            // Formato NCHW (PyTorch estándar)
            // Primero todo el Rojo, luego todo el Verde, luego todo el Azul
            for (i in pixels.indices) {
                byteBuffer.putFloat(android.graphics.Color.red(pixels[i]) / 255.0f)
            }
            for (i in pixels.indices) {
                byteBuffer.putFloat(android.graphics.Color.green(pixels[i]) / 255.0f)
            }
            for (i in pixels.indices) {
                byteBuffer.putFloat(android.graphics.Color.blue(pixels[i]) / 255.0f)
            }
        } else {
            // Formato NHWC (TensorFlow estándar) - El que tenías antes
            for (i in 0 until height) {
                for (j in 0 until width) {
                    val pixel = pixels[i * width + j]
                    byteBuffer.putFloat(android.graphics.Color.red(pixel) / 255.0f)
                    byteBuffer.putFloat(android.graphics.Color.green(pixel) / 255.0f)
                    byteBuffer.putFloat(android.graphics.Color.blue(pixel) / 255.0f)
                }
            }
        }
        return byteBuffer
    }
}