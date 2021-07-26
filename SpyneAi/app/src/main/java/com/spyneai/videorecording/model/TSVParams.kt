package com.spyneai.videorecording.model

import android.graphics.drawable.Drawable
class TSVParams{
    lateinit var framesList: List<String>
    var mImageIndex: Int = 0
    var mEndY: Int = 0
    var mEndX: Int = 0
    var mStartY: Int = 0
    var mStartX: Int = 0
    var placeholder : Drawable? = null
    var type : Int = 0

}