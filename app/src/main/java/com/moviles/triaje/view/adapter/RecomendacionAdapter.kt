package com.moviles.triaje.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.moviles.triaje.R
import com.moviles.triaje.model.Recomendacion

class RecomendacionAdapter(private val lista: List<Recomendacion>) :
    RecyclerView.Adapter<RecomendacionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvStepNumber: TextView = view.findViewById(R.id.tvStepNumber)
        val tvStepTitle: TextView = view.findViewById(R.id.tvStepTitle)
        val tvStepDescription: TextView = view.findViewById(R.id.tvStepDescription)
        val ivStepIcon: ImageView = view.findViewById(R.id.ivStepIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommendation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.tvStepNumber.text = (position + 1).toString()
        holder.tvStepTitle.text = item.titulo
        holder.tvStepDescription.text = item.recomendacion
        
        if (item.iconResId != 0) {
            holder.ivStepIcon.setImageResource(item.iconResId)
        } else {
            holder.ivStepIcon.setImageResource(R.drawable.ic_info)
        }
    }

    override fun getItemCount(): Int = lista.size
}