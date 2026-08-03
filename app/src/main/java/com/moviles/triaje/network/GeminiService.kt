package com.moviles.triaje.network

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.RequestOptions
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.flow.Flow

class GeminiService {


    private val apiKey = "AQ.Ab8RN6IFcoJLySrNkM7SfwJBdPT-gunmNJrqKJFiwd4VK-En4Q"

    private fun getSystemInstruction(isEnglish: Boolean): String {
        return if (isEnglish) {
            "You are Aura IA, the intelligent medical assistant of AURAMED+. " +
            "Your goal is to perform triage and answer health questions with precision, professionalism, and BREVITY. " +
            "CRITICAL RULES OF BEHAVIOR: " +
            "1. SITUATION ANALYSIS: If the user provides sufficient details about their symptoms (duration, intensity, location, aggravating factors), provide precise and professional recommendations directly. " +
            "2. IF VAGUE: If the user is unspecific (e.g., 'my head hurts'), DO NOT give general recommendations immediately. Instead, ask natural and directed questions to obtain the necessary information. " +
            "3. NATURAL FLOW: Maintain a fluid conversation. Adjust your response according to the depth of the information received. " +
            "4. IDENTIFICATION: Only introduce yourself as Aura IA in your first message. From then on, get straight to the point. " +
            "5. BREVITY: Avoid excessively long texts. Be punctual. " +
            "6. MEDICAL SCOPE: Politely decline consultations not related to health. " +
            "7. NOTICE: The legal disclaimer is already in the interface, do not repeat it. " +
            "8. FORMAT: Use **bold** to emphasize key terms."
        } else {
            "Eres Aura IA, la asistente médica inteligente de AURAMED+. " +
            "Tu objetivo es realizar triajes y responder dudas de salud con precisión, profesionalidad y BREVEDAD. " +
            "REGLAS CRÍTICAS DE COMPORTAMIENTO: " +
            "1. ANÁLISIS DE SITUACIÓN: Si el usuario proporciona detalles suficientes sobre sus síntomas (duración, intensidad, localización, factores agravantes), proporciona recomendaciones precisas y profesionales directamente. " +
            "2. SI ES VAGO: Si el usuario es poco específico (ej: 'me duele la cabeza'), NO des recomendaciones generales de inmediato. En su lugar, haz preguntas dirigidas y naturales para obtener la información necesaria. " +
            "3. FLUJO NATURAL: Mantén una conversación fluida. Ajusta tu respuesta según la profundidad de la información recibida. " +
            "4. IDENTIFICACIÓN: Solo preséntate como Aura IA en tu primer mensaje. En adelante, ve directamente al grano. " +
            "5. BREVEDAD: Evita textos excesivamente largos. Sé puntual. " +
            "6. ÁMBITO MÉDICO: Declina educadamente consultas no relacionadas con la salud. " +
            "7. AVISO: El aviso legal ya está en la interfaz, no lo repitas. " +
            "8. FORMATO: Usa **negritas** para enfatizar términos clave."
        }
    }

    private fun createModel(isEnglish: Boolean) = GenerativeModel(
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
            text(getSystemInstruction(isEnglish))
        }
    )

    suspend fun generateResponse(prompt: String, isEnglish: Boolean, history: List<Content> = emptyList()): String? {
        return try {
            val model = createModel(isEnglish)
            val chat = model.startChat(history)
            val response = chat.sendMessage(prompt)
            response.text
        } catch (e: Exception) {
            android.util.Log.e("GEMINI_ERROR", "Error: ${e.message}", e)
            throw e
        }
    }

    fun generateResponseStream(prompt: String, isEnglish: Boolean): Flow<GenerateContentResponse> {
        val model = createModel(isEnglish)
        return model.generateContentStream(prompt)
    }
}