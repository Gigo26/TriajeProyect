package com.moviles.triaje.view.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat
import com.moviles.triaje.R
import com.moviles.triaje.databinding.ItemHistorialBinding
import com.moviles.triaje.model.Consulta
import com.moviles.triaje.model.Prioridad
import java.text.SimpleDateFormat
import java.util.Locale

class HistorialAdapter(private val onItemClick: (Consulta) -> Unit) : RecyclerView.Adapter<HistorialAdapter.ViewHolder>() {

    private var lista: List<Consulta> = emptyList()

    fun actualizarLista(nuevaLista: List<Consulta>) {
        lista = nuevaLista
        notifyDataSetChanged() // para mejor performance usar DiffUtil
    }

    inner class ViewHolder(val binding: ItemHistorialBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistorialBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        val context = holder.itemView.context
        
        // Determinar prioridad para localización y color
        val prioridadEnum = try {
            Prioridad.valueOf(item.prioridad.uppercase()
                .replace("AMARILLO", "AMARILLO") // Mapeo de seguridad para BD vieja
                .replace("ROJO", "ROJA")) 
        } catch (e: Exception) {
            when (item.prioridad.uppercase()) {
                "ROJO" -> Prioridad.ROJA
                "AMARILLA" -> Prioridad.AMARILLO
                else -> Prioridad.AZUL
            }
        }

        holder.binding.tvCategoria.text = context.getString(R.string.priority_label, context.getString(prioridadEnum.stringResId))
        
        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        holder.binding.tvFecha.text = formato.format(item.fecha_registro)

        val color = ContextCompat.getColor(context, prioridadEnum.colorResId)
        holder.binding.vColorCategoria.backgroundTintList = ColorStateList.valueOf(color)

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = lista.size
}