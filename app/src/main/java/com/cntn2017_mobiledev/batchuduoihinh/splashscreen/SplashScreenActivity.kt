package com.cntn2017_mobiledev.batchuduoihinh.splashscreen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import com.cntn2017_mobiledev.batchuduoihinh.R
import com.cntn2017_mobiledev.batchuduoihinh.extension.openWithFadeInAnimation
import com.cntn2017_mobiledev.batchuduoihinh.extension.openWithFadeInAnimation2
import com.cntn2017_mobiledev.batchuduoihinh.home.MainActivity

class SplashScreenActivity : AppCompatActivity() {
    var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        Handler().postDelayed({
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                openWithFadeInAnimation2()
                finish();
        }, 3000)
    }

    override fun onBackPressed() {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed()
                return
            }
            this.doubleBackToExitPressedOnce = true
            Toast.makeText(this,"Click lan nua de thoat",Toast.LENGTH_SHORT).show()
            Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }
}
