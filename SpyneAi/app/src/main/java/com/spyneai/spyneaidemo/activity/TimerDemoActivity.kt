package com.spyneai.spyneaidemo.activity

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.Window
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.spyneai.R
import com.spyneai.activity.DashboardActivity
import com.spyneai.adapter.MarketplacesAdapter
import com.spyneai.adapter.PhotosAdapter
import com.spyneai.aipack.*
import com.spyneai.interfaces.*
import com.spyneai.model.carreplace.CarBackgroundsResponse
import com.spyneai.model.sku.Photos
import com.spyneai.model.skumap.UpdateSkuResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_timer_demo.*

import java.io.File
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
    lateinit var carbackgroundsAdapter: MarketplacesAdapter
    var backgroundSelect : String = ""

    var totalImagesToUPload : Int = 0
    var totalImagesToUPloadIndex : Int = 0
    lateinit var gifList : ArrayList<String>
    lateinit var gifLink : String
    lateinit var image_url : ArrayList<String>
    var countGif : Int = 0
    private lateinit var handler: Handler
    private var isActive = false

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

//                tvMinSec.setText(
//                    "" + String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
//                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
//                                TimeUnit.MINUTES.toSeconds(
//                                    TimeUnit.MILLISECONDS.toMinutes(
//                                        millisUntilFinished
//                                    )
//                                )
//                    )
//                );
                tvMin.setText(""+ String.format("%02d",TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
                tvSec.setText(""+ String.format("%02d",TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                        TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(
                                millisUntilFinished
                            )
                        )));
            }
            override fun onFinish() {
               // tvMinSec.setText("00:00")
                tvMin.setText("00")
                tvSec.setText("00");
                showImageGif()
            }
        }.start()
    }

    private fun showImageGif(){
            val intent = Intent(this@TimerDemoActivity,
                ShowImagesDemActivity::class.java)
//            intent.putExtra(AppConstants.GIF, gifLink)
            startActivity(intent)

    }

    override fun onBackPressed() {
        //  super.onBackPressed()
        if (isActive)
            showExitDialog()
    }

    override fun onResume() {
        super.onResume()
        isActive = true
    }

    override fun onPause() {
        super.onPause()
        isActive = false
    }

    override fun onStop() {
        super.onStop()
        isActive = false
    }

    fun showExitDialog( ) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_exit)
        val dialogButtonYes: TextView = dialog.findViewById(R.id.btnYes)
        val dialogButtonNo: TextView = dialog.findViewById(R.id.btnNo)

        dialogButtonYes.setOnClickListener(View.OnClickListener {
            Utilities.savePrefrence(this@TimerDemoActivity, AppConstants.SHOOT_ID, "")
            Utilities.savePrefrence(this@TimerDemoActivity, AppConstants.CATEGORY_ID, "")
            Utilities.savePrefrence(this@TimerDemoActivity, AppConstants.PRODUCT_ID, "")
            Utilities.savePrefrence(this@TimerDemoActivity, AppConstants.SKU_NAME, "")
            Utilities.savePrefrence(this@TimerDemoActivity, AppConstants.SKU_ID, "")


            val updateSkuResponseList = ArrayList<UpdateSkuResponse>()
            updateSkuResponseList.clear()

            Utilities.setList(
                this@TimerDemoActivity,
                AppConstants.FRAME_LIST, updateSkuResponseList
            )
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
            dialog.dismiss()

        })
        dialogButtonNo.setOnClickListener(View.OnClickListener { dialog.dismiss() })
        dialog.show()
    }

}