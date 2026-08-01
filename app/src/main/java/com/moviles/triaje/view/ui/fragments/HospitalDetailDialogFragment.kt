package com.moviles.triaje.view.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.moviles.triaje.R
import com.moviles.triaje.model.Hospital

class HospitalDetailDialogFragment : BottomSheetDialogFragment() {

    private var hospital: Hospital? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        hospital = arguments?.getSerializable(ARG_HOSPITAL) as? Hospital
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_hospital_detail, container, false)

        val ivClose: ImageView = view.findViewById(R.id.ivCloseDialog)
        val ivImage: ImageView = view.findViewById(R.id.ivDetailHospitalImage)
        val tvName: TextView = view.findViewById(R.id.tvDetailHospitalName)
        val tvAddress: TextView = view.findViewById(R.id.tvDetailHospitalAddress)
        val tvDistanceAndTime: TextView = view.findViewById(R.id.tvDetailHospitalDistanceAndTime)
        val tvPhone: TextView = view.findViewById(R.id.tvDetailHospitalPhone)
        val btnCall: MaterialButton = view.findViewById(R.id.btnCallHospital)
        val btnMap: MaterialButton = view.findViewById(R.id.btnOpenMap)

        hospital?.let { item ->
            tvName.text = item.nombre
            tvAddress.text = item.direccion.ifEmpty { "Dirección no especificada" }

            val tiempoLimpio = item.tiempoEstimado.replace("·", "").trim()
            tvDistanceAndTime.text = "${item.distancia} · Aprox. ${tiempoLimpio} en auto"

            val telefonoContacto = if (item.telefono.isNotEmpty()) item.telefono else "(01) 456-7890 / Emergencias"
            tvPhone.text = telefonoContacto

            if (item.imagenUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(item.imagenUrl)
                    .placeholder(R.drawable.ic_hospital)
                    .error(R.drawable.ic_hospital)
                    .into(ivImage)
            } else {
                ivImage.setImageResource(R.drawable.ic_hospital)
            }

            // Acción: Llamar por teléfono
            btnCall.setOnClickListener {
                val numTel = if (item.telefono.isNotEmpty()) item.telefono else "106"
                try {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$numTel"))
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "No se pudo realizar la llamada", Toast.LENGTH_SHORT).show()
                }
            }

            // Acción: Abrir ubicación en mapas
            btnMap.setOnClickListener {
                val query = if (item.direccion.isNotEmpty()) item.direccion else item.nombre
                try {
                    val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(query)}"))
                    mapIntent.setPackage("com.google.android.apps.maps")
                    if (mapIntent.resolveActivity(requireActivity().packageManager) != null) {
                        startActivity(mapIntent)
                    } else {
                        // Fallback a navegador o cualquier app de mapas si Google Maps no está instalado
                        val browserMapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}"))
                        startActivity(browserMapIntent)
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "No se pudo abrir el mapa", Toast.LENGTH_SHORT).show()
                }
            }
        }

        ivClose.setOnClickListener {
            dismiss()
        }

        return view
    }

    companion object {
        private const val ARG_HOSPITAL = "arg_hospital"

        fun newInstance(hospital: Hospital): HospitalDetailDialogFragment {
            val fragment = HospitalDetailDialogFragment()
            val args = Bundle().apply {
                putSerializable(ARG_HOSPITAL, hospital)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
