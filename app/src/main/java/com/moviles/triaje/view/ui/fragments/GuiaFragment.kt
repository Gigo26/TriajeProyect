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

    // 2. Usamos onViewCreated en lugar de onCreate para buscar e interactuar con las vistas
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Usamos 'view.findViewById' en lugar de 'findViewById' directamente
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
            mostrarVentana("Guía de 1eros A", "Protocolo de Actuación:\n1. Evaluación primaria (ABCDE).\n2. Asegurar vía aérea y control de columna cervical.\n3. Evaluación de ventilación y circulación.\n4. Identificación de déficit neurológico.\n*Priorice el soporte vital básico ante colapso.*")
        }

        btnSignosVitales.setOnClickListener {
            mostrarVentana("Signos Vitales", "Valores de referencia (Adulto):\n• Presión Arterial: 120/80 mmHg (Normal).\n• Frecuencia Cardíaca: 60-100 lpm.\n• Frecuencia Respiratoria: 12-20 rpm.\n• Temperatura: 36.5°C - 37.5°C.\n• SatO2: >95% aire ambiente.")
        }

        btnEmergencias.setOnClickListener {
            mostrarVentana("Triaje - Priorización", "Clasificación de Emergencia:\n• Rojo: Reanimación inmediata.\n• Naranja: Muy urgente (10 min).\n• Amarillo: Urgente (60 min).\n• Verde: Menos urgente.\n• Azul: No urgente (atención diferida).")
        }

        btnPrevencion.setOnClickListener {
            mostrarVentana("Protocolos Prevención", "Normas de Bioseguridad:\n1. Higiene de manos (técnica OMS).\n2. Uso estricto de EPP (guantes, mascarilla, mandil).\n3. Clasificación de residuos (Bolsa roja para biocontaminados).\n4. Desinfección de superficies tras cada paciente.")
        }

        btnMedicamentos.setOnClickListener {
            mostrarVentana("Seguridad Farmacológica", "Regla de los 5 Correctos:\n1. Paciente correcto.\n2. Medicamento correcto.\n3. Dosis correcta.\n4. Vía de administración correcta.\n5. Hora correcta.\n*Verificar siempre alergias previas en historia clínica.*")
        }

        btnContactos.setOnClickListener {
            mostrarVentana("Directorio Interno", "Unidades de Emergencia:\n• Médico de Guardia: #501\n• Enfermería Triaje: #502\n• Laboratorio Clínico: #505\n• Banco de Sangre: #508\n• Admisión/Seguridad: #500")
        }
    }

    private fun mostrarVentana(titulo: String, informacion: String) {
        // 3. Usamos 'requireContext()' porque 'this' referenciaría al Fragment, no al contexto de la app
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