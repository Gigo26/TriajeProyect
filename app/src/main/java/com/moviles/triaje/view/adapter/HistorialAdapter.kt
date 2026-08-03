package com.moviles.triaje.view.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.moviles.triaje.databinding.ItemHistorialBinding
import com.moviles.triaje.model.Consulta
import java.text.SimpleDateFormat
import java.util.Locale

class HistorialAdapter(private val onItemClick: (Consulta) -> Unit) : RecyclerView.Adapter<HistorialAdapter.ViewHolder>() {

    private var lista: List<Consulta> = emptyList()
    private val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

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
        holder.binding.tvCategoria.text = "PRIORIDAD ${item.prioridad}"
        holder.binding.tvFecha.text = formato.format(item.fecha_registro)

        val color = when (item.prioridad.uppercase()) {
            "AZUL" -> Color.parseColor("#2196F3")
            "VERDE" -> Color.parseColor("#4CAF50")
            "AMARILLO" -> Color.parseColor("#FFC107")
            "NARANJA" -> Color.parseColor("#FF9800")
            "ROJA" -> Color.parseColor("#F44336")
            else -> Color.GRAY
        }
        holder.binding.vColorCategoria.backgroundTintList = ColorStateList.valueOf(color)

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = lista.size
}