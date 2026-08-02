package com.moviles.triaje.network

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.RequestOptions
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.flow.Flow

class GeminiService {


    private val apiKey = "AQ.Ab8RN6I83oaQJW7mGHvltqOQEWOMP8lijQrfB-aWlJzw9pLBeA"

    private val model = GenerativeModel(
        modelName = "gemini-3.6-flash",
        apiKey = apiKey,
        generationConfig = generationConfig {
            temperature = 0.7f
            topK = 32
            topP = 1f
            maxOutputTokens = 2048
        },
        requestOptions = RequestOptions(apiVersion = "v1"),
        systemInstruction = com.google.ai.client.generativeai.type.content {
            text("Eres Aura IA, la asistente médica inteligente de AURAMED+. " +
                 "Tu objetivo es realizar triajes y responder dudas de salud con precisión, profesionalidad y BREVEDAD. " +
                 "REGLAS CRÍTICAS DE COMPORTAMIENTO: " +
                 "1. ANÁLISIS DE SITUACIÓN: Si el usuario proporciona detalles suficientes sobre sus síntomas (duración, intensidad, localización, factores agravantes), proporciona recomendaciones precisas y profesionales directamente. " +
                 "2. SI ES VAGO: Si el usuario es poco específico (ej: 'me duele la cabeza'), NO des recomendaciones generales de inmediato. En su lugar, haz preguntas dirigidas y naturales para obtener la información necesaria. " +
                 "3. FLUJO NATURAL: Mantén una conversación fluida. Ajusta tu respuesta según la profundidad de la información recibida. " +
                 "4. IDENTIFICACIÓN: Solo preséntate como Aura IA en tu primer mensaje. En adelante, ve directamente al grano. " +
                 "5. BREVEDAD: Evita textos excesivamente largos. Sé puntual. " +
                 "6. ÁMBITO MÉDICO: Declina educadamente consultas no relacionadas con la salud. " +
                 "7. AVISO: El aviso legal ya está en la interfaz, no lo repitas. " +
                 "8. FORMATO: Usa **negritas** para enfatizar términos clave.")
        }
    )

    suspend fun generateResponse(prompt: String, history: List<Content> = emptyList()): String? {
        return try {
            val chat = model.startChat(history)
            val response = chat.sendMessage(prompt)
            response.text
        } catch (e: Exception) {
            android.util.Log.e("GEMINI_ERROR", "Error: ${e.message}", e)
            throw e
        }
    }

    fun generateResponseStream(prompt: String): Flow<GenerateContentResponse> {
        return model.generateContentStream(prompt)
    }
}