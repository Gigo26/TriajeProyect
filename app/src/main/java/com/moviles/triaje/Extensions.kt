package com.moviles.triaje

import android.text.Editable
import android.text.TextWatcher
import com.google.android.material.textfield.TextInputEditText

// Centralizamos la función aquí para que la use CUALQUIER fragment de tu proyecto
fun TextInputEditText.onTextChanged(onChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            onChanged(s.toString())
        }
        override fun afterTextChanged(s: Editable?) {}
    })
}