package com.spyneai.spyneaidemo.activity

import UploadPhotoResponse
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.Window
import android.view.animation.LinearInterpolator
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.spyneai.R
import com.spyneai.activity.DashboardActivity
import com.spyneai.activity.ShowGifActivity
import com.spyneai.activity.ShowImagesActivity
import com.spyneai.adapter.CarBackgroundAdapter
import com.spyneai.adapter.PhotosAdapter
import com.spyneai.aipack.*
import com.spyneai.interfaces.*
import com.spyneai.model.ai.UploadGifResponse
import com.spyneai.model.carreplace.CarBackgroundsResponse
import com.spyneai.model.otp.OtpResponse
import com.spyneai.model.sku.Photos
import com.spyneai.model.sku.SkuResponse
import com.spyneai.model.skumap.UpdateSkuResponse
import com.spyneai.model.skustatus.UpdateSkuStatusRequest
import com.spyneai.model.skustatus.UpdateSkuStatusResponse
import com.spyneai.model.upload.UploadResponse
import com.spyneai.model.uploadRough.UploadPhotoRequest
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_generate_gif.*
import kotlinx.android.synthetic.main.activity_otp.*
import kotlinx.android.synthetic.main.activity_timer.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.concurrent.TimeUnit


class TimerDemoActivity : AppCompatActivity() {
    val progress = 1000
    var maxProgress = 15000
    var i = 0
    lateinit var countDownTimer : CountDownTimer

    private lateinit var photsAdapter: PhotosAdapter
    private lateinit var photoList: List<Photos>

    lateinit var imageList : List<String>
    public lateinit var imageFileList : ArrayList<File>
    public lateinit var imageFileListFrames : ArrayList<Int>

    private var currentPOsition : Int = 0
    lateinit var carBackgroundList : ArrayList<CarBackgroundsResponse>
    lateinit var carbackgroundsAdapter: CarBackgroundAdapter
    var backgroundSelect : String = ""

    var totalImagesToUPload : Int = 0
    var totalImagesToUPloadIndex : Int = 0
    lateinit var gifList : ArrayList<String>
    lateinit var gifLink : String
    lateinit var image_url : ArrayList<String>
    var countGif : Int = 0
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer_demo)
        backgroundSelect = intent.getStringExtra(AppConstants.BG_ID)!!
        circular_progress.setInterpolator(LinearInterpolator())

        imageFileList = ArrayList<File>()
        imageFileListFrames = ArrayList<Int>()
        image_url = ArrayList<String>()

        //Get Intents
        imageFileList.addAll(intent.getParcelableArrayListExtra(AppConstants.ALL_IMAGE_LIST)!!)
        imageFileListFrames.addAll(intent.getIntegerArrayListExtra(AppConstants.ALL_FRAME_LIST)!!)

        totalImagesToUPload = imageFileList.size

        Log.e("Timer  SKU",
                Utilities.getPreference(this,
                        AppConstants.SKU_NAME)!!)

        CountDownTimer(5000)

//        handler= Handler()
//        handler.postDelayed({
//            showImageGif()
//        },5500)

    }




    private fun CountDownTimer(maxProgress: Long){
        countDownTimer = object : CountDownTimer(maxProgress.toLong(), progress.toLong()) {
            override fun onTick(millisUntilFinished: Long) {
                i++
                circular_progress.setCurrentProgress((i * 100 / (maxProgress / progress)).toDouble())

                tvMinSec.setText(
                    "" + String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(
                                    TimeUnit.MILLISECONDS.toMinutes(
                                        millisUntilFinished
                                    )
                                )
                    )
                );
            }
            override fun onFinish() {
                tvMinSec.setText("00:00")
                showImageGif()
            }
        }.start()
    }

    private fun showImageGif(){
        if (Utilities.getPreference(this@TimerDemoActivity,
                AppConstants.FRAME_SHOOOTS).equals("4")
            || Utilities.getPreference(this@TimerDemoActivity,
                AppConstants.FRAME_SHOOOTS).equals("8")) {
            val intent = Intent(this@TimerDemoActivity,
                ShowImagesDemoActivity::class.java)
//            intent.putExtra(AppConstants.GIF, gifLink)
            startActivity(intent)
        } else {
            val intent = Intent(this@TimerDemoActivity,
                ShowGifDemoActivity::class.java)
//            intent.putExtra(AppConstants.GIF, gifLink)
            startActivity(intent)
        }
        Toast.makeText(this@TimerDemoActivity, "GIF send to your email!", Toast.LENGTH_SHORT).show()
    }

}