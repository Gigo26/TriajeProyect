package com.moviles.triaje.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.type.content
import com.moviles.triaje.model.ChatMessage
import com.moviles.triaje.network.GeminiService
import kotlinx.coroutines.launch

class AuraIAViewModel : ViewModel() {

    private val geminiService = GeminiService()
    private val _messages = MutableLiveData<MutableList<ChatMessage>>(mutableListOf())
    val messages: LiveData<MutableList<ChatMessage>> get() = _messages

    private val _isAILoading = MutableLiveData<Boolean>()
    val isAILoading: LiveData<Boolean> get() = _isAILoading

    fun addMessage(message: ChatMessage) {
        val currentList = _messages.value ?: mutableListOf()
        currentList.add(message)
        _messages.value = currentList
    }

    fun sendMessageToGemini(userText: String) {
        //  mensaje del usuario en estado "procesando" en gris
        addMessage(ChatMessage(userText, isUser = true, isTyping = true))
        _isAILoading.value = true

        viewModelScope.launch {
            // retraso visual
            kotlinx.coroutines.delay(500)
            
            //  mensaje del usuario a en azul q quiere decir procesado
            updateLastUserMessageStatus(isProcessing = false)
            
            //  burbuja de la IA en gris
            val iaPlaceholder = ChatMessage("", isUser = false, isTyping = true)
            addMessage(iaPlaceholder)
            
            // historial para Gemini
            val currentList = _messages.value ?: mutableListOf()
            val history = currentList.filter { !it.isTyping }.map { msg ->
                content(role = if (msg.isUser) "user" else "model") {
                    text(msg.text)
                }
            }
            
            // llamar a la API
            try {
                val response = geminiService.generateResponse(userText, history)
                if (response != null) {
                    _lastResponse.value = response
                } else {
                    updateLastIAMessage("Error: La respuesta de la IA llegó vacía.", isTyping = false)
                }
            } catch (e: Exception) {
                // Mostramos el error REAL para diagnosticar
                updateLastIAMessage("Error detectado: ${e.message}", isTyping = false)
            }
            _isAILoading.value = false
        }
    }

    private val _lastResponse = MutableLiveData<String?>()
    val lastResponse: LiveData<String?> get() = _lastResponse

    fun consumeLastResponse() {
        _lastResponse.value = null
    }

    fun updateLastUserMessageStatus(isProcessing: Boolean) {
        val currentList = _messages.value ?: return
        if (currentList.isNotEmpty()) {
            val lastMsg = currentList.findLast { it.isUser }
            if (lastMsg != null) {
                val index = currentList.lastIndexOf(lastMsg)
                currentList[index] = lastMsg.copy(isTyping = isProcessing)
                _messages.value = currentList
            }
        }
    }

    fun updateLastIAMessage(newText: String, isTyping: Boolean) {
        val currentList = _messages.value ?: return
        if (currentList.isNotEmpty()) {
            val lastMsg = currentList.last()
            if (!lastMsg.isUser) {
                currentList[currentList.size - 1] = lastMsg.copy(text = newText, isTyping = isTyping)
                _messages.value = currentList
            }
        }
    }
}