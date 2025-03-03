package com.example.managecontact.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.view.animation.AnticipateOvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.example.managecontact.R
import com.example.managecontact.databinding.ActivitySplashBinding
import com.example.managecontact.ui.auth.LoginActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply animations
        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in).apply {
            duration = 1000
        }
        
        binding.ivLogo.apply {
            alpha = 0f
            scaleX = 0.5f
            scaleY = 0.5f
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1000)
                .setInterpolator(AnticipateOvershootInterpolator())
                .start()
        }

        binding.tvAppName.apply {
            alpha = 0f
            translationY = 50f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(1000)
                .setStartDelay(500)
                .start()
        }

        binding.tvTagline.apply {
            alpha = 0f
            translationY = 50f
            animate()
                .alpha(0.8f)
                .translationY(0f)
                .setDuration(1000)
                .setStartDelay(700)
                .start()
        }

        // Navigate to LoginActivity after delay
        CoroutineScope(Dispatchers.Main).launch {
            delay(2500) // 2.5 seconds delay
            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            finish()
            // Add slide transition animation
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
} 