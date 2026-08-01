package com.moviles.triaje.utils

import android.content.Context
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

object TranslationManager {

    private var translator: Translator? = null
    private var isModelDownloaded = false

    fun init(context: Context, onComplete: (Boolean) -> Unit) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.SPANISH)
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build()
        
        translator = Translation.getClient(options)

        val conditions = DownloadConditions.Builder()
            .build()

        translator?.downloadModelIfNeeded(conditions)
            ?.addOnSuccessListener {
                isModelDownloaded = true
                onComplete(true)
            }
            ?.addOnFailureListener {
                isModelDownloaded = false
                onComplete(false)
            }
    }

    fun translate(text: String, onResult: (String) -> Unit) {
        if (text.isBlank()) {
            onResult(text)
            return
        }

        if (translator != null && isModelDownloaded) {
            translator?.translate(text)
                ?.addOnSuccessListener { translatedText ->
                    onResult(translatedText)
                }
                ?.addOnFailureListener {
                    onResult(text) // Fallback to original text on error
                }
        } else {
            onResult(text) // Fallback if not ready
        }
    }

    fun close() {
        translator?.close()
    }
}