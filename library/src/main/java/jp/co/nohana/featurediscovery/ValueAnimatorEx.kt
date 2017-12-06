package jp.co.nohana.featurediscovery

import android.animation.ValueAnimator

fun ValueAnimator.animatedValueAsFloat(): Float {
    return animatedValue as Float
}

