package com.moviles.triaje.model

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    var isTyping: Boolean = false,
    var isDelivered: Boolean = false,
    val type: Int = TYPE_MESSAGE
) {
    companion object {
        const val TYPE_MESSAGE = 0
        const val TYPE_DATE = 1
        const val TYPE_DISCLAIMER = 2
    }
}