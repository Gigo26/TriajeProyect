package com.moviles.triaje.view.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.moviles.triaje.R
import com.moviles.triaje.model.Sintoma
import com.moviles.triaje.utils.PreferenceManager
import com.moviles.triaje.utils.TranslationManager

class SintomasAdapter(
    val symptomListener: SymptomListener
) : RecyclerView.Adapter<SintomasAdapter.ViewHolder>() {

    var listSintomas = ArrayList<Sintoma>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_sintoma, parent, false)
        )

    override fun getItemCount() = listSintomas.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sintoma = listSintomas[position]
        val context = holder.itemView.context
        val prefManager = PreferenceManager(context)

        // Lógica de traducción dinámica (Prioriza campo en BD, si no hay usa ML Kit)
        if (prefManager.language == "en") {
            if (!sintoma.sin_description_en.isNullOrBlank()) {
                holder.tvSintomaDescripcion.text = sintoma.sin_description_en
            } else {
                // Traducción Automática por código
                TranslationManager.translate(sintoma.sin_description) { translated ->
                    holder.tvSintomaDescripcion.text = translated
                }
            }
        } else {
            holder.tvSintomaDescripcion.text = sintoma.sin_description
        }

        // Carga dinámica de la imagen desde los recursos locales drawable
        val resourceId = context.resources.getIdentifier(
            sintoma.sin_image, "drawable", context.packageName
        )
        if (resourceId != 0) {
            holder.ivSintomaImagen.setImageResource(resourceId)
        } else {
            holder.ivSintomaImagen.setImageResource(R.drawable.ic_fiebre) // Imagen por defecto
        }

        // Aplicamos el estado visual de selección
        actualizarEstadoVisual(holder.cvSintoma, sintoma.isSelected, context)

        // Delegamos el click al listener que implementará nuestro fragmento
        holder.itemView.setOnClickListener {
            symptomListener.onSintomaClicked(sintoma, position)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(data: List<Sintoma>) {
        listSintomas.clear()
        listSintomas.addAll(data)
        notifyDataSetChanged()
    }

    private fun actualizarEstadoVisual(card: MaterialCardView, seleccionado: Boolean, context: android.content.Context) {
        if (seleccionado) {
            card.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.black)))
            card.strokeWidth = 4
            card.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#F0F4FF")))
        } else {
            card.strokeWidth = 0
            card.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white)))
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cvSintoma = itemView.findViewById<MaterialCardView>(R.id.cvSintoma)
        val ivSintomaImagen = itemView.findViewById<ImageView>(R.id.ivSintomaImagen)
        val tvSintomaDescripcion = itemView.findViewById<TextView>(R.id.tvSintomaDescripcion)
    }
}