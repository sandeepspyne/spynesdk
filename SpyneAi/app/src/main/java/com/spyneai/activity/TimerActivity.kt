package com.spyneai.activity

import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.StrictMode
import android.util.Log
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.spyneai.R
import com.spyneai.adapter.MarketplacesAdapter
import com.spyneai.adapter.PhotosAdapter
import com.spyneai.extras.events.ProcessingImagesEvent
import com.spyneai.model.carreplace.CarBackgroundsResponse
import com.spyneai.model.sku.Photos
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.service.Actions
import com.spyneai.service.ProcessImagesService
import com.spyneai.service.getServiceState
import com.spyneai.service.log
import kotlinx.android.synthetic.main.activity_timer.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.concurrent.TimeUnit


class TimerActivity : AppCompatActivity() {
    val progress = 1000
    var maxProgress = 120000
    var i = 0
    lateinit var countDownTimer: CountDownTimer

    private lateinit var photsAdapter: PhotosAdapter
    private lateinit var photoList: List<Photos>
    private lateinit var photoListInteriors: List<Photos>

    lateinit var imageList: ArrayList<String>
    lateinit var imageListAfter: ArrayList<String>
    lateinit var interiorList: ArrayList<String>

    public lateinit var imageFileList: ArrayList<File>
    public lateinit var imageFileListFrames: ArrayList<Int>

    public lateinit var imageInteriorFileList: ArrayList<File>
    public lateinit var imageInteriorFileListFrames: ArrayList<Int>

    private var currentPOsition: Int = 0
    lateinit var carBackgroundList: ArrayList<CarBackgroundsResponse>
    lateinit var carbackgroundsAdapter: MarketplacesAdapter
    var backgroundSelect: String = ""
    var marketplaceId: String = ""
    var backgroundColour: String = ""

    var totalImagesToUPload: Int = 0
    var totalImagesToUPloadIndex: Int = 0
    lateinit var gifList: ArrayList<String>
    var gifLink: String = ""
    lateinit var image_url: ArrayList<String>
    var countGif: Int = 0
    lateinit var t: Thread
    var catName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)


        val manufacturer = "xiaomi"
        if (manufacturer.equals(Build.MANUFACTURER, ignoreCase = true)) {
            //this will open auto start screen where user can enable permission for your app
            val intent = Intent()
            intent.component = ComponentName(
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity"
            )
            startActivity(intent)
        }

        backgroundSelect = intent.getStringExtra(AppConstants.BG_ID)!!
        circular_progress.setInterpolator(LinearInterpolator())

        if (intent.getStringExtra(AppConstants.CATEGORY_NAME) != null)
            catName = intent.getStringExtra(AppConstants.CATEGORY_NAME)!!
        else
            catName = Utilities.getPreference(this, AppConstants.CATEGORY_NAME)!!

        if (intent.getStringExtra(AppConstants.MARKETPLACE_ID) != null)
            marketplaceId = intent.getStringExtra(AppConstants.MARKETPLACE_ID)!!

        if (intent.getStringExtra(AppConstants.BACKGROUND_COLOUR) != null)
            backgroundColour = intent.getStringExtra(AppConstants.BACKGROUND_COLOUR)!!

        imageFileList = ArrayList<File>()
        imageFileListFrames = ArrayList<Int>()
        image_url = ArrayList<String>()

        //Get Intents
        imageFileList.addAll(intent.getParcelableArrayListExtra(AppConstants.ALL_IMAGE_LIST)!!)
        imageFileListFrames.addAll(intent.getIntegerArrayListExtra(AppConstants.ALL_FRAME_LIST)!!)

        setIntents()



        Log.e(
            "Timer  SKU",
            Utilities.getPreference(
                this,
                AppConstants.SKU_NAME
            )!!
        )

        if (Build.VERSION.SDK_INT > 9) {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }
        setCustomTimer()

        setListener()

//        try {
//            llTimer.visibility = View.VISIBLE
//            llNoInternet.visibility = View.GONE
//            uploadImageToBucket()
//        } catch (e: Exception) {
//            llTimer.visibility = View.GONE
//            llNoInternet.visibility = View.VISIBLE
//            countDownTimer.cancel()
//            e.printStackTrace()
//            Log.e("Catched ", e.printStackTrace().toString())
//        }

//        tvRetry.setOnClickListener(View.OnClickListener {
//            setCustomTimer()
//            try {
//                llTimer.visibility = View.VISIBLE
//                llNoInternet.visibility = View.GONE
//                uploadImageToBucket()
//            } catch (e: Exception) {
//                llTimer.visibility = View.GONE
//                llNoInternet.visibility = View.VISIBLE
//                countDownTimer.cancel()
//                e.printStackTrace()
//                Log.e("Catched ", e.printStackTrace().toString())
//            }
//        })
    }

    private fun setListener(){
        llStartNewShoot.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setIntents() {
        backgroundSelect = intent.getStringExtra(AppConstants.BG_ID)!!
        circular_progress.setInterpolator(LinearInterpolator())

        imageFileList = ArrayList<File>()
        imageFileListFrames = ArrayList<Int>()
        image_url = ArrayList<String>()

        imageInteriorFileList = ArrayList<File>()
        imageInteriorFileListFrames = ArrayList<Int>()

        //Get Intents
        imageFileList.addAll(intent.getParcelableArrayListExtra(AppConstants.ALL_IMAGE_LIST)!!)
        imageFileListFrames.addAll(intent.getIntegerArrayListExtra(AppConstants.ALL_FRAME_LIST)!!)

        if (Utilities.getPreference(this, AppConstants.CATEGORY_NAME).equals("Automobiles")) {
            imageInteriorFileList.addAll(intent.getParcelableArrayListExtra(AppConstants.ALL_INTERIOR_IMAGE_LIST)!!)
            imageInteriorFileListFrames.addAll(intent.getIntegerArrayListExtra(AppConstants.ALL_INTERIOR_FRAME_LIST)!!)
        }
        totalImagesToUPload = imageFileList.size

        title = "Process Images Service"

        actionOnService(Actions.START)
    }

    private fun actionOnService(action: Actions) {
        if (getServiceState(this) == com.spyneai.service.ServiceState.STOPPED && action == Actions.STOP)
            return
//        Intent(this, ProcessImagesService::class.java).also {
//
//            intent.putExtra(AppConstants.BG_ID, backgroundSelect)
//            intent.putExtra(AppConstants.ALL_IMAGE_LIST, imageFileList)
//            intent.putExtra(AppConstants.ALL_FRAME_LIST, imageFileListFrames)
//            intent.putExtra(AppConstants.ALL_INTERIOR_IMAGE_LIST, imageInteriorFileList)
//            intent.putExtra(AppConstants.ALL_INTERIOR_FRAME_LIST, imageInteriorFileListFrames)
//            intent.putExtra(AppConstants.CATEGORY_NAME, catName)

//            intent.action = action.name

        val thread: Thread = object : Thread() {
            override fun run() {

                val serviceIntent = Intent(this@TimerActivity, ProcessImagesService::class.java)
                serviceIntent.putExtra(AppConstants.BG_ID, backgroundSelect)
                serviceIntent.putExtra(AppConstants.ALL_IMAGE_LIST, imageFileList)
                serviceIntent.putExtra(AppConstants.ALL_FRAME_LIST, imageFileListFrames)
                serviceIntent.putExtra(AppConstants.ALL_INTERIOR_IMAGE_LIST, imageInteriorFileList)
                serviceIntent.putExtra(AppConstants.ALL_INTERIOR_FRAME_LIST, imageInteriorFileListFrames)
                serviceIntent.putExtra(AppConstants.CATEGORY_NAME, catName)
                serviceIntent.action = action.name
//        ContextCompat.startForegroundService(this, serviceIntent)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    log("Starting the service in >=26 Mode")
                    ContextCompat.startForegroundService(this@TimerActivity, serviceIntent)
                    return
                }
                log("Starting the service in < 26 Mode")
                startService(serviceIntent)
            }
        }
        thread.start()
    }



    private fun setCustomTimer() {
        if (Utilities.getPreference(this, AppConstants.FRAME_SHOOOTS) != null) {
            if (Utilities.getPreference(this, AppConstants.FRAME_SHOOOTS).equals("4")) {
                CountDownTimer(480000)
            } else if (Utilities.getPreference(this, AppConstants.FRAME_SHOOOTS).equals("8")) {
                CountDownTimer(480000 * 2)
            } else if (Utilities.getPreference(this, AppConstants.FRAME_SHOOOTS).equals("12")) {
                CountDownTimer(480000 * 3)
            } else if (Utilities.getPreference(this, AppConstants.FRAME_SHOOOTS).equals("24")) {
                CountDownTimer(480000 * 4)

            } else if (Utilities.getPreference(this, AppConstants.FRAME_SHOOOTS).equals("5")) {
                CountDownTimer(480000)
            } else if (Utilities.getPreference(this, AppConstants.FRAME_SHOOOTS).equals("6")) {
                CountDownTimer(480000)
            } else if (Utilities.getPreference(this, AppConstants.FRAME_SHOOOTS).equals("7")) {
                CountDownTimer(580000)
            }
        }
    }

    private fun CountDownTimer(maxProgress: Long) {
        countDownTimer = object : CountDownTimer(maxProgress.toLong(), progress.toLong()) {
            override fun onTick(millisUntilFinished: Long) {
                i++
                circular_progress.setCurrentProgress((i * 100 / (maxProgress / progress)).toDouble())

                tvMinSec.setText(
                    "" + String.format(
                        "%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(
                            millisUntilFinished
                        ),
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
            }
        }.start()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ProcessingImagesEvent?) {
        event?.getNotificationID()?.let { Toast.makeText(this, it, Toast.LENGTH_LONG).show() }
    }

}