package com.spyneai.threesixty.ui

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.TextView
import kotlin.math.roundToInt

class MyIndicator @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(
    context,
    attrs
) {

    private var mWidth: Int = 0
    private var mHeight: Int = 0

    private var frameCountStartedAt: Long = 0
    private var frameCount: Long = 0

    private var pitch = 0f // Degrees
    private var roll = 0f // Degrees, left roll is positive

    private val mXfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

    private val mBitmapPaint = Paint().apply {
        isFilterBitmap = false
    }

    private val TAG = "My Indicator"

    private val mSkyColor = Color.parseColor("#26000000")
    public var tvDemo : TextView? = null


    private val mArrowPaint = Paint().apply {
        color = Color.parseColor("#F92C2C")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val mPitchLadderPaint = Paint().apply {
        color = Color.parseColor("#F92C2C")
        strokeWidth = 3f
        isAntiAlias = true
    }


    private val mSrcBitmap: Bitmap by lazy {
        Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
    }

    private val mSrcCanvas: Canvas by lazy {
        Canvas(mSrcBitmap)
    }

    private val mDstBitmap: Bitmap by lazy {
        val bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.TRANSPARENT)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.RED
        canvas.drawRect(RectF(0f, 0f, mWidth.toFloat(), mHeight.toFloat()), paint)
        bitmap
    }

    fun setAttitude(pitch: Float, roll: Float) {
        this.pitch = pitch
        this.roll = roll
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val sc = saveLayer(canvas)
        canvas.drawBitmap(mDstBitmap, 0f, 0f, mBitmapPaint)
        mBitmapPaint.xfermode = mXfermode

        canvas.drawBitmap(getSrc(), 0f, 0f, mBitmapPaint)

        mBitmapPaint.xfermode = null
        canvas.restoreToCount(sc)
    }

    private fun getSrc(): Bitmap {
        val canvas = mSrcCanvas
        val width = mWidth.toFloat()
        val height = mHeight.toFloat()
        val centerX = width / 2
        val centerY = height / 2

        // Background
        canvas.drawColor(mSkyColor)

        // Save the state without any rotation/translation so
        // we can revert back to it to draw the fixed components.
        canvas.save()

        // Orient the earth to reflect the pitch and roll angles
        canvas.rotate(roll, centerX, centerY)

        val totalVisiblePitchDegrees = 45f * 2 // +/- 45 degrees

        var dy = pitch / totalVisiblePitchDegrees * height

        Log.d(TAG, "getSrc: "+"-------------------------")
        Log.d(TAG, "getSrc: "+roll)
        Log.d(TAG, "getSrc: "+dy)
        // Log.d(TAG, "getSrc: "+pitch)
        //Log.d(TAG, "getSrc: "+dy)
        Log.d(TAG, "getSrc: "+"-------------------------")

        canvas.translate(0f, dy)

        canvas.drawRect(-width, centerY + 10, width * 2, centerY + 10, mArrowPaint)

        val paint = Paint()

        if ((roll > 0 && roll < 1.5) && dy in -25.0..10.0){
            if (tvDemo != null)
                tvDemo!!.visibility = View.INVISIBLE
            paint.setColor(Color.parseColor("#40CE4E"));
        }else{
            if (tvDemo != null)
                tvDemo!!.visibility = View.VISIBLE

            paint.setColor(Color.parseColor("#F92C2C"));
        }

        paint.setStyle(Paint.Style.FILL);

        // Half-circle of miniature plane
        val minPlaneCircleRadiusX = width / 6
        val minPlaneCircleRadiusY = height / 15

        //centerX - minPlaneCircleRadiusX - ((centerY - minPlaneCircleRadiusY) / 2)+dpToPx(10)
        val wingsCircleBounds = RectF(
            centerX - minPlaneCircleRadiusX,
            centerY - minPlaneCircleRadiusY,
            centerX + minPlaneCircleRadiusX - dpToPx(10),
            centerY + minPlaneCircleRadiusY)


        canvas.drawRect(wingsCircleBounds, paint)

        drawTriangle(canvas, paint,
            wingsCircleBounds.right.roundToInt(),
            wingsCircleBounds.top.roundToInt(), (wingsCircleBounds.bottom - wingsCircleBounds.top).roundToInt()
        )

        // Return to normal to draw the miniature plane
        canvas.restore()

        // Draw the nose dot
        paint.setStyle(Paint.Style.STROKE)
        paint.setStrokeWidth(6F)
        canvas.drawLine(-width, centerY, width * 2, centerY, paint)

        return mSrcBitmap
    }

    private fun drawTriangle(canvas: Canvas, paint: Paint?, x: Int, top : Int, width: Int) {
        val path = Path()
        path.moveTo(x.toFloat(), top.toFloat()-dpToPx(10)) // Top
        path.lineTo(x.toFloat(), (top+width).toFloat()+dpToPx(10)) // Bottom left
        path.lineTo((x+top/2).toFloat()+dpToPx(10), (top + width/2).toFloat()) // Bottom right
        path.lineTo(x.toFloat(), top.toFloat()-dpToPx(10)) // Back to Top

        path.close()
        canvas.drawPath(path, paint!!)
    }

    fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h
    }


    private fun saveLayer(canvas: Canvas): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return canvas.saveLayer(0f, 0f, mWidth.toFloat(), mHeight.toFloat(), null)
        } else {
            @Suppress("DEPRECATION")
            return canvas.saveLayer(
                0f,
                0f,
                mWidth.toFloat(),
                mHeight.toFloat(),
                null,
                Canvas.ALL_SAVE_FLAG
            )
        }
    }
}