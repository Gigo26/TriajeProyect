package com.moviles.triaje.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.switchmaterial.SwitchMaterial
import com.moviles.triaje.R
import com.moviles.triaje.utils.PreferenceManager

class PreferenciasFragment : Fragment() {

    private lateinit var prefManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_preferencias, container, false)
        prefManager = PreferenceManager(requireContext())

        initViews(view)
        return view
    }

    private fun initViews(view: View) {
        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val switchDarkMode = view.findViewById<SwitchMaterial>(R.id.switchDarkMode)
        val switchLanguage = view.findViewById<SwitchMaterial>(R.id.switchLanguage)
        val switchDataSaver = view.findViewById<SwitchMaterial>(R.id.switchDataSaver)
        val tvCurrentLanguage = view.findViewById<TextView>(R.id.tvCurrentLanguage)

        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Estado inicial
        switchDarkMode.isChecked = prefManager.isDarkMode
        switchLanguage.isChecked = prefManager.language == "en"
        switchDataSaver.isChecked = prefManager.isDataSaver
        updateLanguageText(tvCurrentLanguage)

        // Listeners
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefManager.isDarkMode = isChecked
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        switchLanguage.setOnCheckedChangeListener { _, isChecked ->
            val newLang = if (isChecked) "en" else "es"
            if (prefManager.language != newLang) {
                prefManager.language = newLang
                updateLanguageText(tvCurrentLanguage)
                Toast.makeText(requireContext(), R.string.restarting_app, Toast.LENGTH_SHORT).show()
                requireActivity().recreate()
            }
        }

        switchDataSaver.setOnCheckedChangeListener { _, isChecked ->
            prefManager.isDataSaver = isChecked
        }
    }

    private fun updateLanguageText(tv: TextView) {
        tv.text = if (prefManager.language == "en") getString(R.string.lang_english) else getString(R.string.lang_spanish)
    }
}