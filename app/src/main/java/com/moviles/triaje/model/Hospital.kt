package com.moviles.triaje.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import java.io.Serializable

/**
 * Modelo de Hospital compatible con Firestore.
 * Se usan PropertyName para asegurar el mapeo correcto con la consola de Firebase.
 */
data class Hospital(
    var id: String = "",
    
    @get:PropertyName("hos_name") @set:PropertyName("hos_name")
    var hos_name: String = "",
    
    @get:PropertyName("hos_addres") @set:PropertyName("hos_addres")
    var hos_addres: String = "",
    
    @get:PropertyName("hos_image") @set:PropertyName("hos_image")
    var hos_image: String = "",
    
    @get:PropertyName("hos_contacto") @set:PropertyName("hos_contacto")
    var hos_contacto: String = "",
    
    @get:PropertyName("hos_valoracion") @set:PropertyName("hos_valoracion")
    var hos_valoracion: Double = 0.0,
    
    @get:PropertyName("hos_web") @set:PropertyName("hos_web")
    var hos_web: String = "",
    
    @get:PropertyName("hos_latitud") @set:PropertyName("hos_latitud")
    var hos_latitud: Double = 0.0,
    
    @get:PropertyName("hos_longitud") @set:PropertyName("hos_longitud")
    var hos_longitud: Double = 0.0,

    // Campos de UI (No se guardan en Firestore)
    @get:Exclude @set:Exclude var distance: Double = 0.0,
    @get:Exclude @set:Exclude var duration: Int = 0
) : Serializable
