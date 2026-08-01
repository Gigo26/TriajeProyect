package com.moviles.triaje.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.moviles.triaje.R
import com.moviles.triaje.model.Hospital

interface HospitalListener {
    fun onHospitalClick(hospital: Hospital)
}

class HospitalAdapter(
    private val listener: HospitalListener? = null
) : RecyclerView.Adapter<HospitalAdapter.ViewHolder>() {

    var listHospitales = ArrayList<Hospital>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hospital, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = listHospitales.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val hospital = listHospitales[position]

        holder.tvHospitalName.text = hospital.nombre
        holder.tvHospitalDistance.text = hospital.distancia
        holder.tvHospitalAddress.text = hospital.direccion

        // Formatear el tiempo de viaje con punto separador si no lo trae
        val tiempoFormateado = if (hospital.tiempoEstimado.startsWith("·") || hospital.tiempoEstimado.startsWith(".")) {
            hospital.tiempoEstimado
        } else {
            "· ${hospital.tiempoEstimado}"
        }
        holder.tvHospitalTime.text = tiempoFormateado

        // Cargar imagen con Glide si hay URL, o usar icono predeterminado ic_hospital
        if (hospital.imagenUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(hospital.imagenUrl)
                .placeholder(R.drawable.ic_hospital)
                .error(R.drawable.ic_hospital)
                .into(holder.ivHospitalImage)
        } else {
            holder.ivHospitalImage.setImageResource(R.drawable.ic_hospital)
        }

        holder.itemView.setOnClickListener {
            listener?.onHospitalClick(hospital)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(data: List<Hospital>) {
        listHospitales.clear()
        listHospitales.addAll(data)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivHospitalImage: ImageView = itemView.findViewById(R.id.ivHospitalImage)
        val tvHospitalName: TextView = itemView.findViewById(R.id.tvHospitalName)
        val tvHospitalDistance: TextView = itemView.findViewById(R.id.tvHospitalDistance)
        val tvHospitalAddress: TextView = itemView.findViewById(R.id.tvHospitalAddress)
        val tvHospitalTime: TextView = itemView.findViewById(R.id.tvHospitalTime)
        val ivGoArrow: ImageView = itemView.findViewById(R.id.ivGoArrow)
    }
}
