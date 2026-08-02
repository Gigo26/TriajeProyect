package com.moviles.triaje.view.adapter

import com.moviles.triaje.model.Hospital

interface HospitalListener {
    fun onHospitalClick(hospital: Hospital)
    fun onFavoriteClick(hospital: Hospital, isFavorite: Boolean)
}
