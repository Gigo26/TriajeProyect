package com.moviles.triaje.view.ui.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.moviles.triaje.R
import android.widget.ImageView
import androidx.navigation.fragment.findNavController

class GuiaFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_guia, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnPrimerosAuxilios = view.findViewById<MaterialCardView>(R.id.cvPrimerosAuxilios)
        val btnSignosVitales = view.findViewById<MaterialCardView>(R.id.cvSignosVitales)
        val btnEmergencias = view.findViewById<MaterialCardView>(R.id.cvEmergencias)
        val btnPrevencion = view.findViewById<MaterialCardView>(R.id.cvPrevencion)
        val btnMedicamentos = view.findViewById<MaterialCardView>(R.id.cvMedicamentos)
        val btnContactos = view.findViewById<MaterialCardView>(R.id.cvContactos)
        val btnRegresar = view.findViewById<ImageView>(R.id.ivRegresar)

        btnRegresar.setOnClickListener {
            findNavController().popBackStack()
        }

        btnPrimerosAuxilios.setOnClickListener {
            mostrarVentana(
                getString(R.string.guide_1st_aid_title),
                getString(R.string.guide_1st_aid_info)
            )
        }

        btnSignosVitales.setOnClickListener {
            mostrarVentana(
                getString(R.string.guide_vital_signs_title),
                getString(R.string.guide_vital_signs_info)
            )
        }

        btnEmergencias.setOnClickListener {
            mostrarVentana(
                getString(R.string.guide_emergency_triage_title),
                getString(R.string.guide_emergency_triage_info)
            )
        }

        btnPrevencion.setOnClickListener {
            mostrarVentana(
                getString(R.string.guide_prevention_title),
                getString(R.string.guide_prevention_info)
            )
        }

        btnMedicamentos.setOnClickListener {
            mostrarVentana(
                getString(R.string.guide_meds_safety_title),
                getString(R.string.guide_meds_safety_info)
            )
        }

        btnContactos.setOnClickListener {
            mostrarVentana(
                getString(R.string.guide_contacts_title),
                getString(R.string.guide_contacts_info)
            )
        }
    }

    private fun mostrarVentana(titulo: String, informacion: String) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_guia)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        val tvTitulo = dialog.findViewById<TextView>(R.id.tvPopupTitulo)
        val tvInfo = dialog.findViewById<TextView>(R.id.tvPopupInfo)
        val contenedorRaiz = dialog.findViewById<FrameLayout>(R.id.contenedorRaizPopup)

        tvTitulo.text = titulo
        tvInfo.text = informacion

        contenedorRaiz.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
