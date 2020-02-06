package com.cntn2017_mobiledev.batchuduoihinh.extension

import androidx.appcompat.app.AppCompatActivity
import com.cntn2017_mobiledev.batchuduoihinh.R

fun AppCompatActivity.openWithFadeInAnimation() {
    overridePendingTransition(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit)
}
fun AppCompatActivity.openWithFadeInAnimation2() {
    overridePendingTransition(R.anim.fragment_fade_in, R.anim.fragment_fade_out)
}