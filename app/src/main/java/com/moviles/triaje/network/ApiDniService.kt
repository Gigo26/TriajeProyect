package com.moviles.triaje.network

import com.google.gson.Gson
import com.moviles.triaje.model.DniResponse
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class ApiDniService {
    private val client = OkHttpClient()

    fun buscarDni(dni: String, callback: (Boolean, String) -> Unit) {
        val request = Request.Builder()
            .url("https://api.factiliza.com/v1/dni/info/$dni")
            .addHeader(
                "Authorization",
                "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI0MTU3OSJ9.EXKS-IlTuGDjxj2UY1CvJYFR5k6ae7GJpdKqarvpIu4"
            )
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Error de red: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (body == null) {
                    callback(false, "Sin Respuesta del servidor")
                    return
                }
                try {
                    val gson = Gson()
                    val persona = gson.fromJson(body, DniResponse::class.java)

                    if (persona.success) {
                        // Concatenamos exactamente: nombres + espacio + apellido_paterno + espacio + apellido_materno
                        val nombreCompleto = "${persona.data.nombres} ${persona.data.apellidoPaterno} ${persona.data.apellidoMaterno}".trim()
                        callback(true, nombreCompleto)
                    } else {
                        callback(false, "DNI no encontrado")
                    }
                } catch (e: Exception) {
                    callback(false, "Error procesando datos: ${e.message}")
                }
            }
        })
    }
}