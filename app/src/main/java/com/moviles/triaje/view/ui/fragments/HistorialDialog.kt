package com.moviles.triaje.view.ui.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.moviles.triaje.R
import com.moviles.triaje.model.Consulta
import com.moviles.triaje.model.Prioridad
import java.text.SimpleDateFormat
import java.util.Locale

class HistorialDialog : DialogFragment() {

    private var consulta: Consulta? = null
    private val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    companion object {
        private const val ARG_CONSULTA = "arg_consulta"

        fun newInstance(consulta: Consulta): HistorialDialog {
            val fragment = HistorialDialog()
            val args = Bundle()
            args.putSerializable(ARG_CONSULTA, consulta)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        consulta = arguments?.getSerializable(ARG_CONSULTA) as? Consulta
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_historial_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        setupViews(view)
    }

    private fun setupViews(view: View) {
        val consulta = this.consulta ?: return

        val ivClose = view.findViewById<ImageView>(R.id.ivCloseDialog)
        val ivPriorityIcon = view.findViewById<ImageView>(R.id.ivPriorityIcon)
        val tvPriorityName = view.findViewById<TextView>(R.id.tvPriorityName)
        val tvHistoryDate = view.findViewById<TextView>(R.id.tvHistoryDate)
        val tvHistoryDiagnosis = view.findViewById<TextView>(R.id.tvHistoryDiagnosis)
        val tvHistoryDescription = view.findViewById<TextView>(R.id.tvHistoryDescription)
        val ivHistoryImage = view.findViewById<ImageView>(R.id.ivHistoryImage)
        val cvHistoryImage = view.findViewById<View>(R.id.cvHistoryImage)
        val tvPatientType = view.findViewById<TextView>(R.id.tvPatientType)
        val tvSymptoms = view.findViewById<TextView>(R.id.tvSymptoms)
        val llRecommendations = view.findViewById<LinearLayout>(R.id.llRecommendationsContainer)
        val btnOk = view.findViewById<MaterialButton>(R.id.btnOk)

        // Prioridad
        val prioridadEnum = try {
            Prioridad.valueOf(consulta.prioridad.uppercase()
                .replace("AMARILLO", "AMARILLO")
                .replace("ROJO", "ROJA"))
        } catch (e: Exception) {
            when (consulta.prioridad.uppercase()) {
                "ROJO" -> Prioridad.ROJA
                "AMARILLA" -> Prioridad.AMARILLO
                else -> Prioridad.AZUL
            }
        }

        tvPriorityName.text = getString(R.string.priority_label, getString(prioridadEnum.stringResId))
        tvPriorityName.setTextColor(ContextCompat.getColor(requireContext(), prioridadEnum.colorResId))
        ivPriorityIcon.setImageResource(prioridadEnum.iconResId)
        btnOk.setBackgroundColor(ContextCompat.getColor(requireContext(), prioridadEnum.colorResId))

        // Datos Generales
        tvHistoryDate.text = formato.format(consulta.fecha_registro)
        tvHistoryDiagnosis.text = consulta.resultado_titulo
        tvHistoryDescription.text = consulta.resultado_descripcion
        tvPatientType.text = getString(R.string.history_patient, consulta.tipo_paciente)
        tvSymptoms.text = getString(R.string.history_symptoms, consulta.sintomas.joinToString(", "))

        // Imagen
        if (!consulta.url_imagen_evidencia.isNullOrEmpty()) {
            cvHistoryImage.visibility = View.VISIBLE
            Glide.with(this)
                .load(consulta.url_imagen_evidencia)
                .into(ivHistoryImage)
        }

        // Recomendaciones
        llRecommendations.removeAllViews()
        consulta.recomendaciones.entries.sortedBy { it.key.toIntOrNull() ?: 0 }.forEach { entry ->
            val step = entry.key
            val recMap = entry.value
            val titulo = recMap["titulo"] as? String ?: ""
            val desc = recMap["descripcion"] as? String ?: ""
            
            val itemView = LayoutInflater.from(context).inflate(R.layout.item_recommendation, llRecommendations, false)
            itemView.findViewById<TextView>(R.id.tvStepNumber).text = step
            itemView.findViewById<TextView>(R.id.tvStepTitle).text = titulo
            itemView.findViewById<TextView>(R.id.tvStepDescription).text = desc
            
            // Buscar icono dinámicamente si es posible
            val iconResId = context?.resources?.getIdentifier("ic_info", "drawable", context?.packageName) ?: R.drawable.ic_info
            itemView.findViewById<ImageView>(R.id.ivStepIcon).setImageResource(iconResId)

            llRecommendations.addView(itemView)
        }

        ivClose.setOnClickListener { dismiss() }
        btnOk.setOnClickListener { dismiss() }
    }
}
