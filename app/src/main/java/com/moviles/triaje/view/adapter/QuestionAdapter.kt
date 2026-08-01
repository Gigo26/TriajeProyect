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
import com.moviles.triaje.model.Pregunta
import com.moviles.triaje.utils.PreferenceManager
import com.moviles.triaje.utils.TranslationManager

class QuestionAdapter(
    private val questionListener: QuestionListener
) : RecyclerView.Adapter<QuestionAdapter.ViewHolder>() {

    var listPreguntas = ArrayList<Pregunta>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_question, parent, false)
        )

    override fun getItemCount() = listPreguntas.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pregunta = listPreguntas[position]
        val context = holder.itemView.context
        val prefManager = PreferenceManager(context)

        // Lógica de traducción dinámica
        val isEnglish = prefManager.language == "en"
        
        if (isEnglish) {
            // Traducir Título de la Pregunta
            if (!pregunta.text_en.isNullOrBlank()) {
                holder.tvQuestion.text = pregunta.text_en
            } else {
                TranslationManager.translate(pregunta.text) { translated ->
                    holder.tvQuestion.text = translated
                }
            }
        } else {
            holder.tvQuestion.text = pregunta.text
        }

        // 1. Limpiamos cualquier RadioButton previo
        holder.rgOptions.removeAllViews()
        holder.rgOptions.setOnCheckedChangeListener(null)

        // 2. Obtener lista de opciones (Prioriza BD, si no hay usa la de español para traducir)
        val rawOptions = if (isEnglish && !pregunta.options_en.isNullOrEmpty()) pregunta.options_en else pregunta.options

        rawOptions.forEachIndexed { index, optionText ->
            val radioButton = MaterialRadioButton(context).apply {
                id = View.generateViewId()
                text = optionText // Texto inicial
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
                buttonTintList = ColorStateList.valueOf(Color.parseColor("#6200EE"))
            }

            // Si es inglés y no hay traducción en BD, traducimos dinámicamente
            if (isEnglish && (pregunta.options_en == null || pregunta.options_en.isEmpty())) {
                TranslationManager.translate(optionText) { translated ->
                    radioButton.text = translated
                }
            }

            holder.rgOptions.addView(radioButton)

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
    fun updateData(data: List<Pregunta>) {
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