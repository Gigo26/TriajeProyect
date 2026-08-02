package com.moviles.triaje.view.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.moviles.triaje.R

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val ivLogo = findViewById<ImageView>(R.id.ivLogoSplash)
        val tvPlus = findViewById<TextView>(R.id.tvPlus)

        val letters = listOf(
            findViewById<TextView>(R.id.tvCharA),
            findViewById<TextView>(R.id.tvCharU),
            findViewById<TextView>(R.id.tvCharR),
            findViewById<TextView>(R.id.tvCharA2),
            findViewById<TextView>(R.id.tvCharM),
            findViewById<TextView>(R.id.tvCharE),
            findViewById<TextView>(R.id.tvCharD)
        )

        // cargar anim
        val pulse = AnimationUtils.loadAnimation(this, R.anim.anim_pulse)
        val letterReveal = AnimationUtils.loadAnimation(this, R.anim.anim_letter_reveal)
        val plusReveal = AnimationUtils.loadAnimation(this, R.anim.anim_plus_reveal)

        // secuencia animaciones
        
        // animacion pulso para el logo
        ivLogo.startAnimation(pulse)

        // listener para comenzar anim de letras despues del pulso
        pulse.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {

                // muestra letras
                ivLogo.postDelayed({
                    animateLetters(letters, 0, letterReveal, plusReveal, tvPlus)
                }, 100)
            }
        })

        // en milisegundos
        // pulso(600 * 4 = 2400) + Pausa(100) + letras(7 * 250 = 1750) + Plus(600) + espera(800)
        ivLogo.postDelayed({
            val user = FirebaseAuth.getInstance().currentUser
            val intent = if (user != null) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, IntroActivity::class.java)
            }
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 5800)
    }

    private fun animateLetters(
        letters: List<TextView>, 
        index: Int, 
        letterAnim: Animation, 
        plusAnim: Animation, 
        tvPlus: TextView
    ) {
        if (index < letters.size) {
            val letter = letters[index]
            letter.visibility = View.VISIBLE
            letter.startAnimation(letterAnim)
            
            // intervalo entre letras
            letter.postDelayed({
                animateLetters(letters, index + 1, letterAnim, plusAnim, tvPlus)
            }, 250)
        } else {
            // el "+"
            tvPlus.postDelayed({
                tvPlus.visibility = View.VISIBLE
                tvPlus.startAnimation(plusAnim)
            }, 100)
        }
    }
}