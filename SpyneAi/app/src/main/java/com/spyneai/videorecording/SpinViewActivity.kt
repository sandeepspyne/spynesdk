package com.spyneai.videorecording

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.LevelListDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.MotionEventCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.bumptech.glide.request.transition.Transition
import com.spyneai.R
import kotlinx.android.synthetic.main.activity_spin_view.*

import java.io.File
import java.io.FileOutputStream


class SpinViewActivity : AppCompatActivity(),View.OnTouchListener,PreLoadListener {
    private var array: Array<String> = emptyArray()
    private var mImageIndex: Int = 0
    private var mEndY: Int = 0
    private var mEndX: Int = 0
    private var mStartY: Int = 0
    private var mStartX: Int = 0
    var list: ArrayList<Uri> = ArrayList()
    var TAG = "spin"
    lateinit var levelList : LevelListDrawable
    var handler = Handler()
    lateinit var placeholder : Drawable


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spin_view)



         array = arrayOf(
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-001.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-002.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-003.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-004.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-005.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-006.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-007.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-008.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-009.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-010.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-011.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-012.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-013.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-014.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-015.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-016.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-017.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-018.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-019.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-020.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-021.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-022.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-023.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-024.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-025.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-026.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-027.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-028.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-029.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-030.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-031.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-032.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-033.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-034.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-035.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-036.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-037.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-038.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-039.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-040.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-041.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-042.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-043.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-044.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-045.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-046.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-047.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-048.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-049.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-050.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-051.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-052.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-053.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-054.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-055.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-056.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-057.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-058.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-059.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-060.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-061.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-062.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-063.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-064.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-065.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-066.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-067.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-068.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-069.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-070.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-071.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-072.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-073.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-074.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-075.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-076.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-077.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-078.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-079.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-080.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-081.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-082.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-083.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-084.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-085.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-086.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-087.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-088.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-089.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-090.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-091.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-092.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-093.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-094.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-095.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-096.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-097.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-098.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-099.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-100.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-101.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-102.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-103.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-104.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-105.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-106.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-107.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-108.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-109.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-110.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-111.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-112.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-113.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-114.jpg",
             "https://storage.googleapis.com/spyne-website/landing-page/static/360/Interior/front/ezgif-frame-115.jpg"
         )

        iv.setOnTouchListener(this)

        levelList = LevelListDrawable()

        for (i in 1..18) {
            list.add(Uri.parse("file:///android_asset/images/image1_" + i + ".jpg"))
        }

        mImageIndex = list.size / 2

        var requestOptions =  RequestOptions();
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)


//
//        for ((index, url) in array.withIndex()) {
//           // loadFromGlide(url, index)
//            Glide.with(this)
//                .load(url)
//                .listener(object : RequestListener<Drawable> {
//                    override fun onLoadFailed(
//                        e: GlideException?,
//                        model: Any?,
//                        target: Target<Drawable>?,
//                        isFirstResource: Boolean
//                    ): Boolean {
//                        Log.d(TAG, "onResourceReady: failed")
//                        return true
//                    }
//
//                    override fun onResourceReady(
//                        resource: Drawable?,
//                        model: Any?,
//                        target: Target<Drawable>?,
//                        dataSource: DataSource?,
//                        isFirstResource: Boolean
//                    ): Boolean {
//                        Log.d(TAG, "onResourceReady: paseed "+index)
//                        if(index == array.size - 1){
//                            loadImage(mImageIndex)
//                        }
//
//                        return true
//                    }
//
//                })
//                .apply(requestOptions)
//                .preload()
//        }

        //three_sixty_view.init(array,this)




        iv.visibility = View.GONE

        for ((index, url) in array.withIndex()) {
            // loadFromGlide(url, index)
            Glide.with(this)
                .load(url)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d(TAG, "onResourceReady: failed")
                        return true
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d(TAG, "onResourceReady: paseed " + index)

                        if (index == mImageIndex)
                            placeholder = resource!!

                        if (index == array.size - 1) {
                            progress_bar.visibility = View.GONE
                            iv.visibility = View.VISIBLE
                            loadImage(mImageIndex)
                        }

                        return true
                    }

                })
                //.override(300, 300)
                .dontAnimate()
                .apply(requestOptions)
                //.into(iv)
                .preload()

     }


    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        var action = MotionEventCompat.getActionMasked(event)

        when(action){
            MotionEvent.ACTION_DOWN -> {
                mStartX = event!!.x.toInt()
                mStartY = event.y.toInt()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                mEndX = event!!.x.toInt()
                mEndY = event.y.toInt()

                if (mEndX - mStartX > 3) {
                    mImageIndex++
                    if (mImageIndex >= array.size) mImageIndex = array.size - 1

                    //iv.setImageLevel(mImageIndex)
                    loadImage(mImageIndex)
                }
                if (mEndX - mStartX < -3) {
                    mImageIndex--
                    if (mImageIndex < 0) mImageIndex = 0
                    loadImage(mImageIndex)
                    //iv.setImageLevel(mImageIndex)
                }
                mStartX = event.x.toInt()
                mStartY = event.y.toInt()

                return true
            }

            MotionEvent.ACTION_UP -> {
                mEndX = event!!.x.toInt()
                mEndY = event.y.toInt()

                return true
            }

            MotionEvent.ACTION_CANCEL -> return true
            MotionEvent.ACTION_OUTSIDE -> return true
        }

        return super.onTouchEvent(event)
    }



    fun loadImage(index: Int){

        handler.removeCallbacksAndMessages(null)

        handler.postDelayed({
            Log.d(TAG, "loadImage: " + index)



            Glide.with(this)
                .load(array.get(index))
                .placeholder(placeholder)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d(TAG, "onResourceReady: failed")
                        return true
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        placeholder = resource!!

                        return true
                    }

                })
                .override(250, 250)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(iv)
        }, 10)

    }

    override fun onPreLoaded() {
        progress_bar.visibility = View.GONE
    }
}