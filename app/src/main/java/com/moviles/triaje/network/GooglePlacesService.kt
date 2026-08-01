package com.moviles.triaje.network

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.moviles.triaje.model.Hospital
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Servicio especializado para interactuar con Google Places API vía REST (HTTP).
 * Se prefiere REST sobre el SDK porque el SDK nativo searchNearby tiene restricciones 
 * de tipos muy severas que causan el error 9012 (Unsupported types).
 */
class GooglePlacesService(private val apiKey: String) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .build()

    fun buscarHospitalesCercanos(
        latLng: LatLng,
        radius: Int = 10000,
        callback: Callback<List<Hospital>>
    ) {
        // En la API REST, usamos keywords para evitar errores de tipos de Google
        val query = "hospital|clinica|centro+medico|salud"
        val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?location=${latLng.latitude},${latLng.longitude}" +
                "&radius=$radius" +
                "&keyword=$query" +
                "&language=es" +
                "&key=$apiKey"

        Log.d("HOSPITAL_DEBUG", "Consultando API Google REST...")

        val request = Request.Builder().url(url).build()

        httpClient.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("HOSPITAL_DEBUG", "Error de red HTTP", e)
                callback.onFailed(e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                
                if (!response.isSuccessful || responseBody == null) {
                    callback.onFailed(Exception("Fallo en respuesta de Google"))
                    return
                }

                try {
                    val json = JSONObject(responseBody)
                    
                    if (json.has("error_message")) {
                        val msg = json.getString("error_message")
                        Log.e("HOSPITAL_DEBUG", "Error API Google: $msg")
                        callback.onFailed(Exception(msg))
                        return
                    }

                    val results = json.getJSONArray("results")
                    val hospitales = mutableListOf<Hospital>()

                    for (i in 0 until results.length()) {
                        val obj = results.getJSONObject(i)
                        val loc = obj.getJSONObject("geometry").getJSONObject("location")
                        
                        val hospital = Hospital(
                            id = obj.getString("place_id"),
                            hos_name = obj.getString("name"),
                            hos_addres = obj.optString("vicinity", "Dirección no disponible"),
                            hos_valoracion = obj.optDouble("rating", 0.0),
                            hos_latitud = loc.getDouble("lat"),
                            hos_longitud = loc.getDouble("lng"),
                            hos_image = obtenerPhotoUrl(obj)
                        )
                        hospitales.add(hospital)
                    }

                    Log.d("HOSPITAL_DEBUG", "Google REST encontró ${hospitales.size} resultados")
                    callback.onSuccess(hospitales)

                } catch (e: Exception) {
                    Log.e("HOSPITAL_DEBUG", "Error parseando JSON", e)
                    callback.onFailed(e)
                }
            }
        })
    }

    private fun obtenerPhotoUrl(obj: JSONObject): String {
        val photos = obj.optJSONArray("photos")
        if (photos != null && photos.length() > 0) {
            val photoReference = photos.getJSONObject(0).getString("photo_reference")
            return "https://maps.googleapis.com/maps/api/place/photo" +
                    "?maxwidth=400" +
                    "&photo_reference=$photoReference" +
                    "&key=$apiKey"
        }
        return ""
    }
}
