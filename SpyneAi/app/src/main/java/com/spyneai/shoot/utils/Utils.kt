package com.spyneai.shoot.utils

import android.util.Log

val Any.TAG: String
    get() {
        val tag = javaClass.simpleName
        return if (tag.length <= 23) tag else tag.substring(0, 23)
    }

fun log(msg: String) {
    Log.d("TESTING", msg)
}

fun shoot(msg: String){
    Log.d("TESTINGCAMERA", msg)
}