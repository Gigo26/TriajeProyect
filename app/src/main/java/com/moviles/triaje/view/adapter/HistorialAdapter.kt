package com.moviles.triaje.view.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.moviles.triaje.databinding.ItemHistorialBinding
import com.moviles.triaje.model.Historial
import java.text.SimpleDateFormat
import java.util.Locale

class HistorialAdapter : RecyclerView.Adapter<HistorialAdapter.ViewHolder>() {

    private var lista: List<Historial> = emptyList()
    private val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    fun actualizarLista(nuevaLista: List<Historial>) {
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
        holder.binding.tvCategoria.text = item.categoria.replaceFirstChar { it.uppercase() }
        holder.binding.tvFecha.text = formato.format(item.fechaHora.toDate())

        val color = when (item.categoria.lowercase()) {
            "azul" -> Color.parseColor("#2196F3")
            "verde" -> Color.parseColor("#4CAF50")
            "amarillo" -> Color.parseColor("#FFEB3B")
            "naranja" -> Color.parseColor("#FF9800")
            "rojo" -> Color.parseColor("#F44336")
            else -> Color.GRAY
        }
        holder.binding.vColorCategoria.backgroundTintList = ColorStateList.valueOf(color)
    }

    override fun getItemCount() = lista.size
}