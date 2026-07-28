package com.moviles.triaje.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.switchmaterial.SwitchMaterial
import com.moviles.triaje.R
import com.moviles.triaje.utils.PreferenceManager

class NotificacionesFragment : Fragment() {

    private lateinit var prefManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notificaciones, container, false)
        prefManager = PreferenceManager(requireContext())

        initViews(view)
        return view
    }

    private fun initViews(view: View) {
        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val switchPush = view.findViewById<SwitchMaterial>(R.id.switchPush)
        val switchEmail = view.findViewById<SwitchMaterial>(R.id.switchEmail)
        val switchUpdates = view.findViewById<SwitchMaterial>(R.id.switchUpdates)
        val switchMessages = view.findViewById<SwitchMaterial>(R.id.switchMessages)
        val switchOffers = view.findViewById<SwitchMaterial>(R.id.switchOffers)

        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Cargar estado inicial
        switchPush.isChecked = prefManager.notifPush
        switchEmail.isChecked = prefManager.notifEmail
        switchUpdates.isChecked = prefManager.notifUpdates
        switchMessages.isChecked = prefManager.notifMessages
        switchOffers.isChecked = prefManager.notifOffers

        // Listeners
        switchPush.setOnCheckedChangeListener { _, isChecked -> prefManager.notifPush = isChecked }
        switchEmail.setOnCheckedChangeListener { _, isChecked -> prefManager.notifEmail = isChecked }
        switchUpdates.setOnCheckedChangeListener { _, isChecked -> prefManager.notifUpdates = isChecked }
        switchMessages.setOnCheckedChangeListener { _, isChecked -> prefManager.notifMessages = isChecked }
        switchOffers.setOnCheckedChangeListener { _, isChecked -> prefManager.notifOffers = isChecked }
    }
}