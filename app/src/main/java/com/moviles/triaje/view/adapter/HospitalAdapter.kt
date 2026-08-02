package com.moviles.triaje.view.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.moviles.triaje.R
import com.moviles.triaje.model.Hospital
import java.util.Locale

class HospitalAdapter(
    private val listener: HospitalListener? = null,
    private val userFavorites: List<String> = emptyList()
) : RecyclerView.Adapter<HospitalAdapter.ViewHolder>() {

    private var listHospitales = mutableListOf<Hospital>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hospital, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = listHospitales.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val hospital = listHospitales[position]

        holder.tvHospitalName.text = hospital.hos_name
        holder.tvHospitalAddress.text = hospital.hos_addres
        holder.tvHospitalDistance.text = String.format(Locale.getDefault(), "%.1f KM", hospital.distance)
        holder.tvHospitalTime.text = String.format(Locale.getDefault(), "· %d min", hospital.duration)

        holder.ivHospitalIcon.setImageResource(R.drawable.ic_hospital)

        // Lógica de Favorito
        var isFavorite = userFavorites.contains(hospital.hos_name)
        updateFavoriteUI(holder.ivHospitalStar, isFavorite)

        holder.ivHospitalStar.setOnClickListener {
            isFavorite = !isFavorite
            updateFavoriteUI(holder.ivHospitalStar, isFavorite)
            listener?.onFavoriteClick(hospital, isFavorite)
        }

        holder.itemView.setOnClickListener {
            listener?.onHospitalClick(hospital)
        }
    }

    private fun updateFavoriteUI(imageView: ImageView, isFavorite: Boolean) {
        if (isFavorite) {
            imageView.setColorFilter(Color.parseColor("#FFD700")) // Dorado
        } else {
            imageView.setColorFilter(Color.GRAY)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(data: List<Hospital>) {
        listHospitales.clear()
        listHospitales.addAll(data)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivHospitalIcon: ImageView = itemView.findViewById(R.id.ivHospitalIcon)
        val ivHospitalStar: ImageView = itemView.findViewById(R.id.ivHospitalStar)
        val tvHospitalName: TextView = itemView.findViewById(R.id.tvHospitalName)
        val tvHospitalDistance: TextView = itemView.findViewById(R.id.tvHospitalDistance)
        val tvHospitalAddress: TextView = itemView.findViewById(R.id.tvHospitalAddress)
        val tvHospitalTime: TextView = itemView.findViewById(R.id.tvHospitalTime)
    }
}
