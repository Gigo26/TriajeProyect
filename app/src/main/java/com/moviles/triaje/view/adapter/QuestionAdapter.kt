package com.moviles.triaje.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.radiobutton.MaterialRadioButton
import com.moviles.triaje.R
import com.moviles.triaje.model.Question

class QuestionAdapter(
    private val questionListener: QuestionListener
) : RecyclerView.Adapter<QuestionAdapter.ViewHolder>() {

    var listPreguntas = ArrayList<Question>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_question, parent, false)
        )

    override fun getItemCount() = listPreguntas.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pregunta = listPreguntas[position]
        val context = holder.itemView.context

        holder.tvQuestion.text = pregunta.text

        // 1. Limpiamos cualquier RadioButton previo para evitar bugs visuales al reciclar vistas
        holder.rgOptions.removeAllViews()
        holder.rgOptions.setOnCheckedChangeListener(null) // Quitamos el listener temporalmente

        // 2. Creamos los RadioButtons dinámicamente según las opciones del banco de preguntas
        pregunta.options.forEachIndexed { index, optionText ->
            val radioButton = MaterialRadioButton(context).apply {
                id = View.generateViewId() // Genera ID dinámico y seguro
                text = optionText
                textSize = 16f
                setTextColor(Color.BLACK)
                setTypeface(null, Typeface.BOLD)
                setPadding(8.toPx(context), 0, 0, 0)

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 8.toPx(context))
                layoutParams = params

                // Estilo púrpura por defecto para tus controles de selección
                buttonTintList = ColorStateList.valueOf(Color.parseColor("#6200EE"))
            }

            holder.rgOptions.addView(radioButton)

            // 3. Restaurar el estado si el usuario ya había marcado una opción
            if (index == pregunta.selectedOptionIndex) {
                radioButton.isChecked = true
            }
        }

        // 4. Asignar el listener para capturar la respuesta del usuario usando la interfaz delegada
        holder.rgOptions.setOnCheckedChangeListener { group, checkedId ->
            val selectedRadioButton = group.findViewById<RadioButton>(checkedId)
            if (selectedRadioButton != null) {
                val clickedIndex = group.indexOfChild(selectedRadioButton)
                questionListener.onOptionSelected(pregunta, clickedIndex, position)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(data: List<Question>) {
        listPreguntas.clear()
        listPreguntas.addAll(data)
        notifyDataSetChanged()
    }

    // Extensión utilitaria interna para conversión de unidades DP a PX
    private fun Int.toPx(context: Context): Int =
        (this * context.resources.displayMetrics.density).toInt()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvQuestion = itemView.findViewById<TextView>(R.id.tvQuestion)
        val rgOptions = itemView.findViewById<RadioGroup>(R.id.rgOptions)
    }
}