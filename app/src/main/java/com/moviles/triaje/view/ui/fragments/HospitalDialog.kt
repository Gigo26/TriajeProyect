package com.moviles.triaje.view.ui.fragments

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import com.moviles.triaje.R
import com.moviles.triaje.model.Hospital
import java.util.Locale

/**
 * Diálogo modular para mostrar el detalle de un hospital.
 * Reutiliza el modelo Hospital y centraliza la lógica de visualización.
 */
class HospitalDialog : DialogFragment() {

    private var hospital: Hospital? = null

    companion object {
        private const val ARG_HOSPITAL = "arg_hospital"

        fun newInstance(hospital: Hospital): HospitalDialog {
            val fragment = HospitalDialog()
            val args = Bundle()
            args.putSerializable(ARG_HOSPITAL, hospital)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        hospital = arguments?.getSerializable(ARG_HOSPITAL) as? Hospital
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_hospital_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        setupViews(view)
        setupMap()
    }

    private fun setupViews(view: View) {
        val hospital = this.hospital ?: return

        val ivClose = view.findViewById<ImageView>(R.id.ivCloseDialog)
        val ivImage = view.findViewById<ImageView>(R.id.ivDetailHospitalImage)
        val tvName = view.findViewById<TextView>(R.id.tvDetailHospitalName)
        val tvAddress = view.findViewById<TextView>(R.id.tvDetailHospitalAddress)
        val tvDistance = view.findViewById<TextView>(R.id.tvDetailHospitalDistance)
        val tvTime = view.findViewById<TextView>(R.id.tvDetailHospitalTime)
        val tvPhone = view.findViewById<TextView>(R.id.tvDetailHospitalPhone)
        val tvWeb = view.findViewById<TextView>(R.id.tvDetailHospitalWeb)
        val btnCall = view.findViewById<MaterialButton>(R.id.btnCallHospital)

        tvName.text = hospital.hos_name
        tvAddress.text = hospital.hos_addres
        tvDistance.text = String.format(Locale.getDefault(), "%.1f KM", hospital.distance)
        tvTime.text = String.format(Locale.getDefault(), " · Aprox. %d min", hospital.duration)
        tvPhone.text = if (hospital.hos_contacto.isNotEmpty()) hospital.hos_contacto else "No disponible"
        tvWeb.text = if (hospital.hos_web.isNotEmpty()) hospital.hos_web else "No disponible"

        // Carga de imagen con Glide para el detalle
        if (hospital.hos_image.isNotEmpty()) {
            Glide.with(this)
                .load(hospital.hos_image)
                .placeholder(R.drawable.ic_hospital)
                .error(R.drawable.ic_hospital)
                .into(ivImage)
        }

        ivClose.setOnClickListener { dismiss() }
        
        btnCall.setOnClickListener {
            if (hospital.hos_contacto.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:${hospital.hos_contacto}")
                startActivity(intent)
            }
        }

        tvWeb.setOnClickListener {
            if (hospital.hos_web.isNotEmpty()) {
                var url = hospital.hos_web
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://$url"
                }
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }
        }
    }

    private fun setupMap() {
        val hospital = this.hospital ?: return
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync { googleMap ->
            val position = LatLng(hospital.hos_latitud, hospital.hos_longitud)
            googleMap.addMarker(MarkerOptions().position(position).title(hospital.hos_name))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))
            
            // Habilitar controles de la API de Google Maps
            googleMap.uiSettings.isZoomControlsEnabled = true
            googleMap.uiSettings.isMapToolbarEnabled = true // Botones de "Ruta" y "Google Maps"
        }
    }
}
