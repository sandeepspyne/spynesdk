package com.spyneai.camera2

import FrameImages
import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.media.AudioManager
import android.media.MediaActionSound
import android.net.Uri
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Rational
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.*
import android.widget.NumberPicker.OnValueChangeListener
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.activity.GenerateGifActivity
import com.spyneai.activity.GenerateMarketplaceActivity
import com.spyneai.adapter.FocusedFramesAdapter
import com.spyneai.adapter.InteriorFramesAdapter
import com.spyneai.adapter.ProgressAdapter
import com.spyneai.adapter.SubCategoriesAdapter
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.dashboard.ui.MainDashboardActivity
import com.spyneai.shoot.ui.dialogs.SubCategoryConfirmationDialog
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClient
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.model.shoot.*
import com.spyneai.model.sku.SkuResponse
import com.spyneai.model.skuedit.EditSkuRequest
import com.spyneai.model.skumap.UpdateSkuResponse
import com.spyneai.model.subcategories.Data
import com.spyneai.needs.AppConstants
import com.spyneai.needs.ImageFilePath
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_camera2.*
import kotlinx.android.synthetic.main.activity_camera_preview.*

import kotlinx.android.synthetic.main.view_images.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList


typealias LumaListener = (luma: Double) -> Unit

class Camera2Activity : AppCompatActivity() {
    private lateinit var gifList: ArrayList<String>
    private var savedUri: Uri? = null
    private lateinit var photoFile: File
    private var pos: Int = 0
    private lateinit var progressList: List<Data>
    private lateinit var progressAdapter: ProgressAdapter
    private lateinit var camera: Camera
    private var flashMode: Int = ImageCapture.FLASH_MODE_OFF

    private val SELECT_PICTURE = 1
    private var selectedImagePath: String? = null
    private lateinit var selectedItemFrame: String

    private var imageCapture: ImageCapture? = null

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    lateinit var cameraProvider: ProcessCameraProvider

    lateinit var subCategoriesList: ArrayList<NewSubCatResponse.Data>
    lateinit var subCategoriesAdapter: SubCategoriesAdapter

    lateinit var interiorFrameList: ArrayList<FrameImages>
    lateinit var interiorFramesAdapter: InteriorFramesAdapter

    lateinit var focusedFrameList: ArrayList<FrameImages>
    lateinit var focusedFramesAdapter: FocusedFramesAdapter
    var catName: String = "Category"
    var selectedSubCategory = ""
    var selectedSubcategoryImage= ""
    var isSubcatgoryConfirmed = false


    var skuId: String = ""
    var skuName: String = "SKU"
    lateinit var shootIds: String
    lateinit var catIds: String
    lateinit var prodIds: String
    var frameNumber: Int = 1
    var totalFrames: Int = 0
    var frameNumberTemp: Int = 0

    var frameImage: String = ""
   // lateinit var frameImageList: ArrayList<FrameImages>
    lateinit var overlaysList : ArrayList<OverlaysResponse.Data>
    lateinit var frameInteriorImageList: ArrayList<FrameImages>
    lateinit var frameFocusedImageList: ArrayList<FrameImages>
    lateinit var frameImageListSelections: ArrayList<Int>


    lateinit var imageFileList: ArrayList<File>
    lateinit var imageFileListFrames: ArrayList<Int>

    lateinit var imageInteriorFileList: ArrayList<File>
    lateinit var imageInteriorFileListFrames: ArrayList<Int>

    lateinit var imageFocusedFileList: ArrayList<File>
    lateinit var imageFocusedFileListFrames: ArrayList<Int>

    var interiorEnabled: Boolean = false
    var focusedEnabled: Boolean = false
    var catId = ""

    var vinNumber: String = ""
    var shootDimensions = ShootDimensions()
    var shootCount = 1

    @SuppressLint("UnsafeOptInUsageError")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera2)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        if (Utilities.getPreference(this, AppConstants.CATEGORY_NAME).equals("Automobiles"))
            showHint()

        setSubCategories()
        setPermissions()
        setCameraActions()


        imageFileList = ArrayList<File>()
        imageFileListFrames = ArrayList<Int>()

        imageInteriorFileList = ArrayList<File>()
        imageInteriorFileListFrames = ArrayList<Int>()

        imageFocusedFileList = ArrayList<File>()
        imageFocusedFileListFrames = ArrayList<Int>()

        gifList = ArrayList<String>()
        frameImageListSelections = ArrayList<Int>()

        //frameImageList = ArrayList<FrameImages>()

        //Get Intents
        gifList.addAll(intent.getParcelableArrayListExtra(AppConstants.GIF_LIST)!!)

        fetchIntents()
    }

    private fun fetchIntents() {
        if (intent.getStringExtra(AppConstants.CATEGORY_NAME) != null)
            catName = intent.getStringExtra(AppConstants.CATEGORY_NAME)!!
        else
            catName = Utilities.getPreference(this, AppConstants.CATEGORY_NAME)!!

        if (intent.getStringExtra(AppConstants.SKU_NAME) != null)
            skuName = intent.getStringExtra(AppConstants.SKU_NAME)!!
        else
            skuName = Utilities.getPreference(this, AppConstants.SKU_NAME)!!

        etSkuName.setText(skuName)

        if(frameNumber == 1){
            imgNext.visibility= View.GONE
            etSkuName!!.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {

                }

                override fun beforeTextChanged(
                    s: CharSequence, start: Int, count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence, start: Int, before: Int,
                    count: Int
                ) {
                    if (!etSkuName.text.toString().trim().isEmpty()) {
                        imgNext.visibility = View.VISIBLE
                    } else {
                        imgNext.visibility = View.GONE
                    }
                }
            })
            selectedItemFrame = Utilities.getPreference(this, AppConstants.FRAME_SHOOOTS).toString()
        }

        if (!etSkuName.text.toString().isEmpty()) {
            rvSubcategories.visibility = View.GONE
            //etSkuName.visibility = View.GONE
        }

        if (!frameImage.isEmpty()) {
            ivPreview.visibility = View.VISIBLE
            imgOverlay.visibility = View.VISIBLE
            Glide.with(this@Camera2Activity).load(
                AppConstants.BASE_IMAGE_URL + frameImage
            ).into(ivPreview)

            Glide.with(this@Camera2Activity).load(
                AppConstants.BASE_IMAGE_URL + frameImage
            ).into(imgOverlay)
        } else {
            ivPreview.visibility = View.GONE
            imgOverlay.visibility = View.INVISIBLE
        }
    }

    private fun getViewDimensions(view : View,type : Int) {
        view.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)

                when (type) {
                    0 -> {
                        shootDimensions.previewWidth = view.width
                        shootDimensions.previewHeight = view.height
                    }
                    1 -> {
                        shootDimensions.overlayWidth = view.width
                        shootDimensions.overlayHeight = view.height
                    }
                }
            }
        })
    }

    private fun setSubCategories() {
        subCategoriesList = ArrayList<NewSubCatResponse.Data>()
        subCategoriesAdapter = SubCategoriesAdapter(
            this,
            subCategoriesList, pos,
            object : SubCategoriesAdapter.BtnClickListener {
                override fun onBtnClick(
                    position: Int,
                    subcategoryName: String,
                    subcategoryImage: String
                ) {
                    Log.d("Sub Position click", position.toString())
                    if (pos != position) {
                        pos = position
                        isSubcatgoryConfirmed = false
                        selectedSubCategory = subcategoryName
                        selectedSubcategoryImage = subcategoryImage
                        etSkuName.setText(vinNumber)

                        setProductMap(
                            Utilities.getPreference(
                                this@Camera2Activity,
                                AppConstants.SHOOT_ID
                            ).toString(), position, false
                        )

                        subCategoriesAdapter.notifyDataSetChanged()
                    }
                }
            })
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        rvSubcategories.setLayoutManager(layoutManager)
        rvSubcategories.setAdapter(subCategoriesAdapter)
        setInteriorFrames()
    }

    private fun setInteriorFrames() {
        frameInteriorImageList = ArrayList<FrameImages>()
        interiorFramesAdapter = InteriorFramesAdapter(
            this,
            frameInteriorImageList, pos,
            object : InteriorFramesAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    Log.d("Position click", position.toString())
                    // pos = position
                    interiorFramesAdapter.notifyDataSetChanged()
                    rvInteriorFrames.scrollToPosition(position)
                }
            })
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        rvInteriorFrames.setLayoutManager(layoutManager)
        rvInteriorFrames.setAdapter(interiorFramesAdapter)


        frameFocusedImageList = ArrayList<FrameImages>()
        focusedFramesAdapter = FocusedFramesAdapter(
            this,
            frameFocusedImageList, pos,
            object : FocusedFramesAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    Log.d("Position click", position.toString())
                    focusedFramesAdapter.notifyDataSetChanged()
                    rvFocusedFrames.scrollToPosition(position)
                }
            })
        val layoutManagers: RecyclerView.LayoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )


        rvFocusedFrames.setLayoutManager(layoutManagers)
        rvFocusedFrames.setAdapter(focusedFramesAdapter)

        setFocusedFrames()
    }

    private fun setFocusedFrames() {
        fetchSubCategories()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setProgressFrame(num: Int) {

        frameImageListSelections.clear()
        if (num == 4) {
            frameImageListSelections .add(0)
            frameImageListSelections .add(9)
            frameImageListSelections .add(18)
            frameImageListSelections .add(27)
            Utilities.savePrefrence(this@Camera2Activity, AppConstants.NO_OF_IMAGES, "4")
        }
        else if (num == 5){
            frameImageListSelections .add(0)
            frameImageListSelections .add(1)
            frameImageListSelections .add(2)
            frameImageListSelections .add(3)
            frameImageListSelections .add(4)
            Utilities.savePrefrence(this@Camera2Activity, AppConstants.NO_OF_IMAGES, "5")
        }
        else if(num == 8)
        {
            frameImageListSelections .add(0)
            frameImageListSelections .add(5)
            frameImageListSelections .add(8)
            frameImageListSelections .add(14)
            frameImageListSelections .add(18)
            frameImageListSelections .add(22)
            frameImageListSelections .add(26)
            frameImageListSelections .add(32)
            Utilities.savePrefrence(this@Camera2Activity, AppConstants.NO_OF_IMAGES, "8")
        }
        else if(num == 9)
        {
            frameImageListSelections .add(0)
            frameImageListSelections .add(1)
            frameImageListSelections .add(2)
            frameImageListSelections .add(3)
            frameImageListSelections .add(4)
            frameImageListSelections .add(5)
            frameImageListSelections .add(6)
            frameImageListSelections .add(7)
            Utilities.savePrefrence(this@Camera2Activity, AppConstants.NO_OF_IMAGES, "9")
        }
        else if(num == 12){
            frameImageListSelections .add(0)
            frameImageListSelections .add(2)
            frameImageListSelections .add(4)
            frameImageListSelections .add(9)
            frameImageListSelections .add(15)
            frameImageListSelections .add(16)
            frameImageListSelections .add(18)
            frameImageListSelections .add(20)
            frameImageListSelections .add(21)
            frameImageListSelections .add(27)
            frameImageListSelections .add(32)
            frameImageListSelections .add(34)
            Utilities.savePrefrence(this@Camera2Activity, AppConstants.NO_OF_IMAGES, "12")
        }
        else if(num == 24){
            frameImageListSelections .add(0)
            frameImageListSelections .add(2)
            frameImageListSelections .add(3)
            frameImageListSelections .add(4)
            frameImageListSelections .add(5)
            frameImageListSelections .add(7)
            frameImageListSelections .add(9)
            frameImageListSelections .add(11)
            frameImageListSelections .add(12)
            frameImageListSelections .add(14)
            frameImageListSelections .add(15)
            frameImageListSelections .add(16)
            frameImageListSelections .add(18)
            frameImageListSelections .add(20)
            frameImageListSelections .add(21)
            frameImageListSelections .add(22)
            frameImageListSelections .add(23)
            frameImageListSelections .add(25)
            frameImageListSelections .add(27)
            frameImageListSelections .add(30)
            frameImageListSelections .add(32)
            frameImageListSelections .add(33)
            frameImageListSelections .add(34)
            frameImageListSelections .add(35)
            Utilities.savePrefrence(this@Camera2Activity, AppConstants.NO_OF_IMAGES, "24")
        }
        showProgressFrames(frameNumberTemp)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun showProgressFrames(frameNumberTemp: Int) {
        if (!interiorEnabled) {
            if (!focusedEnabled) {

                if (frameNumberTemp == 0) {
                    tvshoot.setOnClickListener(View.OnClickListener {
                        if (catName.equals("Automobiles")) {
                            showCustomSelectionDialog()
                        } else
                            if (catName.equals("Footwear")) {
                                showFootwearCustomSelectionDialog()
                            }
                        tvshoot.isEnabled = true
                        tvshoot.isFocusable = true
                    })
                } else {
                    tvshoot.isEnabled = false
                    tvshoot.isFocusable = false
                    rvSubcategories.visibility = View.GONE
                    rvInteriorFrames.visibility = View.GONE
                    rvFocusedFrames.visibility = View.GONE
                }
            }
            else{
                rvSubcategories.visibility = View.GONE
                rvInteriorFrames.visibility = View.GONE
                rvFocusedFrames.visibility = View.VISIBLE

                tvshoot.isEnabled = false
                tvshoot.isFocusable = false

                //start
                focusedFramesAdapter = FocusedFramesAdapter(
                    this,
                    frameFocusedImageList, frameNumberTemp,
                    object : FocusedFramesAdapter.BtnClickListener {
                        override fun onBtnClick(position: Int) {
                            Log.d("Position click", position.toString())
                            // pos = position
                            focusedFramesAdapter.notifyDataSetChanged()
                        }
                    })
                val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(
                    this,
                    LinearLayoutManager.VERTICAL,
                    false
                )
                rvFocusedFrames.setLayoutManager(layoutManager)
                rvFocusedFrames.setAdapter(focusedFramesAdapter)
                rvFocusedFrames.scrollToPosition(frameNumberTemp)

                focusedFramesAdapter.notifyDataSetChanged()
            }
        }
        else if (interiorEnabled){
            rvSubcategories.visibility = View.GONE
            rvInteriorFrames.visibility = View.VISIBLE
            rvFocusedFrames.visibility = View.GONE

            tvshoot.isEnabled = false
            tvshoot.isFocusable = false

            //start
            interiorFramesAdapter = InteriorFramesAdapter(
                this,
                frameInteriorImageList, frameNumberTemp,
                object : InteriorFramesAdapter.BtnClickListener {
                    override fun onBtnClick(position: Int) {
                        Log.d("Position click", position.toString())
                        // pos = position
                        interiorFramesAdapter.notifyDataSetChanged()
                    }
                })
            val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
            )
            rvInteriorFrames.setLayoutManager(layoutManager)
            rvInteriorFrames.setAdapter(interiorFramesAdapter)
            rvInteriorFrames.scrollToPosition(frameNumberTemp)

            interiorFramesAdapter.notifyDataSetChanged()

        }

        frameNumber = frameImageListSelections[frameNumberTemp] + 1
        totalFrames = frameImageListSelections.size

        if (!interiorEnabled) {
            if (!focusedEnabled) {
//                if (frameImageList.size > 0) {
                if (overlaysList.size > 0) {
                    /*Glide.with(this@Camera2Activity).load(
                        AppConstants.BASE_IMAGE_URL +
                                frameImageList[frameNumber - 1].displayImage
                    ).into(ivPreview)

                    Glide.with(this@Camera2Activity).load(
                        AppConstants.BASE_IMAGE_URL +
                                frameImageList[frameNumber - 1].displayImage
                    ).into(imgOverlay)*/

                    Glide.with(this@Camera2Activity).load(
                        AppConstants.BASE_IMAGE_URL +
                                overlaysList[0].display_thumbnail
                    ).into(ivPreview)

                    Glide.with(this@Camera2Activity).load(
                        AppConstants.BASE_IMAGE_URL +
                                overlaysList[0].display_thumbnail
                    ).into(imgOverlay)

                    ivPreview.visibility = View.VISIBLE
                    imgOverlay.visibility = View.VISIBLE
                }
            }
            else{
                if (frameFocusedImageList.size > 0){
                    ivPreview.visibility = View.GONE
                    imgOverlay.visibility = View.GONE
                }
            }
        } else {
            if (frameInteriorImageList.size > 0) {
                ivPreview.visibility = View.GONE
                imgOverlay.visibility = View.GONE
            }
        }

        progressList = ArrayList<Data>()

        tvshoot.setText("Shots " + (frameNumberTemp + 1) + "/" + totalFrames)
        val framesList: List<Int> = ArrayList(totalFrames)

        progressAdapter = ProgressAdapter(this, framesList as ArrayList<Int>)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        rvProgress.setLayoutManager(layoutManager)
        rvProgress.setAdapter(progressAdapter)

        for (i in 0..frameNumberTemp)
            (framesList as ArrayList).add(0)
        for (i in frameNumberTemp + 1..totalFrames - 1)
            (framesList as ArrayList).add(1)

        progressAdapter.notifyDataSetChanged()

        setCameraActions()
        startCamera()
    }

    private fun fetchSubCategories() {
        Utilities.showProgressDialog(this)
        subCategoriesList.clear()

        if (intent.getStringExtra(AppConstants.CATEGORY_ID) != null)
            catIds = intent.getStringExtra(AppConstants.CATEGORY_ID)!!
        else
            catIds = Utilities.getPreference(this, AppConstants.CATEGORY_ID)!!

        val request = RetrofitClients.buildService(APiService::class.java)
//        val call = request.getSubCategories(
//            Utilities.getPreference(this, AppConstants.tokenId), catIds
//        )

        val call = request.getSubCategories(
            Utilities.getPreference(this,AppConstants.AUTH_KEY).toString(), catIds
        )

        call?.enqueue(object : Callback<NewSubCatResponse> {
            override fun onResponse(
                call: Call<NewSubCatResponse>,
                response: Response<NewSubCatResponse>
            ) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful) {


                    if (response.body()?.status == 200 && response.body()?.data?.isNotEmpty()!!) {

                        subCategoriesList.addAll(response.body()?.data!!)

                        selectedSubCategory = subCategoriesList.get(pos).sub_cat_name
                        selectedSubcategoryImage = subCategoriesList.get(pos).display_thumbnail

                        subCategoriesAdapter.notifyDataSetChanged()
                    }


                    if (Utilities.getPreference(this@Camera2Activity, AppConstants.FROM)
                            .equals("BA")
                    )
                        setProductMap(
                            Utilities.getPreference(
                                this@Camera2Activity,
                                AppConstants.SHOOT_ID
                            ).toString(), 0, true
                        )
                }
            }

            override fun onFailure(call: Call<NewSubCatResponse>, t: Throwable) {
                Log.e("ok", "no way")
                Utilities.hideProgressDialog()
                Toast.makeText(
                    applicationContext,
                    "Server not responding(getSubCategories)",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun setPermissions() {
        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setCameraActions() {
        imgNext.setOnClickListener(View.OnClickListener {
            if (frameNumber == 1) {
                if (Utilities.isNetworkAvailable(this)) {
                    vinNumber = etSkuName.text.toString().trim()
                    updateSkus()
                } else {
                    Toast.makeText(
                        this,
                        "Please check your internet connection!!!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })


        imgBack.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })

        ivPreview.setOnClickListener(View.OnClickListener {
            if (frameNumber == 1) {
                if (imgOverlay.visibility == View.INVISIBLE) {
                    imgOverlay.visibility = View.VISIBLE
                    rvSubcategories.visibility = View.GONE
                } else {
                    imgOverlay.visibility = View.INVISIBLE
                    rvSubcategories.visibility = View.VISIBLE
                }
            } else {
                rvSubcategories.visibility = View.GONE
                if (imgOverlay.visibility == View.INVISIBLE) {
                    imgOverlay.visibility = View.VISIBLE
                } else {
                    imgOverlay.visibility = View.INVISIBLE
                }
            }
        })

        outputDirectory = getOutputDirectory()

        photoFile = File(
            outputDirectory,
            SimpleDateFormat(
                FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".png"
        )

        // Set up the listener for take photo button
        camera_capture_button.setOnClickListener {
            if (!isSubcatgoryConfirmed){
                var dialog = SubCategoryConfirmationDialog()

                var arguments =  Bundle()
                arguments.putString("subcat_name", selectedSubCategory)
                arguments.putString("subcat_image", selectedSubcategoryImage)

                dialog.arguments = arguments

                //dialog.show(supportFragmentManager, "SubCategoryConfirmationDialog")

                isSubcatgoryConfirmed = true
                startCapturing()
            }else{
                startCapturing()
            }
        }


        ivGallery.setOnClickListener(View.OnClickListener {
            Toast.makeText(
                applicationContext,
                "Coming Soon...",
                Toast.LENGTH_SHORT
            ).show()
        })

        cameraExecutor = Executors.newSingleThreadExecutor()

        tvSkipShoot.setOnClickListener(View.OnClickListener {
            if (!interiorEnabled) {
                if (!focusedEnabled) {
                    rvSubcategories.visibility = View.VISIBLE

                    //   imageFileList.add(photoFile!!)
                    // imageFileListFrames.add(frameImageList[frameImageListSelections[frameNumberTemp]].frameNumber)
                    if (frameNumberTemp < frameImageListSelections.size - 1) {
                        showProgressFrames(++frameNumberTemp)
                        camera_capture_button.isEnabled = true
                        camera_capture_button.isFocusable = true
                    } else {
                        showInteriorDialog()
                    }
                } else {
                    rvSubcategories.visibility = View.GONE
                    //imageFocusedFileList.add(photoFile!!)
                    //imageFocusedFileListFrames.add(frameFocusedImageList[frameImageListSelections[frameNumberTemp]].frameNumber)
                    if (frameNumberTemp < frameImageListSelections.size - 1) {
                        showProgressFrames(++frameNumberTemp)
                        camera_capture_button.isEnabled = true
                        camera_capture_button.isFocusable = true
                    } else {
                        val intent = Intent(
                            this,
                            GenerateGifActivity::class.java
                        )

                        intent.putExtra(AppConstants.ALL_IMAGE_LIST, imageFileList)
                        intent.putExtra(AppConstants.ALL_FRAME_LIST, imageFileListFrames)
                        intent.putExtra(AppConstants.ALL_INTERIOR_IMAGE_LIST, imageInteriorFileList)
                        intent.putExtra(
                            AppConstants.ALL_INTERIOR_FRAME_LIST,
                            imageInteriorFileListFrames
                        )
                        intent.putExtra(AppConstants.ALL_FOCUSED_IMAGE_LIST, imageFocusedFileList)
                        intent.putExtra(
                            AppConstants.ALL_FOCUSED_FRAME_LIST,
                            imageFocusedFileListFrames
                        )
                        intent.putExtra(AppConstants.GIF_LIST, gifList)

                        Log.e("All focused Image", imageFocusedFileList.toString())
                        Log.e("All focused Frames", imageFocusedFileListFrames.toString())

                        Utilities.savePrefrence(this, AppConstants.SKU_NAME, skuName)
                        Log.e(
                            "Camera  SKU",
                            Utilities.getPreference(
                                this,
                                AppConstants.SKU_NAME
                            )!!
                        )
                        startActivity(intent)
                        finish()
                    }
                }
            } else if (interiorEnabled) {
                rvSubcategories.visibility = View.GONE
                // imageInteriorFileList.add(photoFile!!)
                //imageInteriorFileListFrames.add(frameInteriorImageList[frameImageListSelections[frameNumberTemp]].frameNumber)
                if (frameNumberTemp < frameImageListSelections.size - 1) {
                    showProgressFrames(++frameNumberTemp)
                    camera_capture_button.isEnabled = true
                    camera_capture_button.isFocusable = true
                } else {
                    showFocusedDialog()
                }
            }
        })
    }

    fun startCapturing() {
        camera_capture_button.isEnabled = false
        camera_capture_button.isFocusable = false

        val audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        when (audio.ringerMode) {
            AudioManager.RINGER_MODE_NORMAL -> {
                val sound = MediaActionSound()
                sound.play(MediaActionSound.SHUTTER_CLICK)
            }
            AudioManager.RINGER_MODE_SILENT -> {
            }
            AudioManager.RINGER_MODE_VIBRATE -> {
            }
        }

        camera_overlay.visibility = View.VISIBLE

        val fadeOutAnimation = AlphaAnimation(1.0f, 0.0f).apply {
            duration = 300
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                    camera_overlay.alpha = 0.0f
                    camera_overlay.isVisible = false
                }

                override fun onAnimationStart(animation: Animation?) {
                }
            })
        }
        val fadeInAnimation = AlphaAnimation(0.0f, 1.0f).apply {
            duration = 300
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                    camera_overlay.alpha = 1.0f
                    Handler(Looper.getMainLooper()).postDelayed({
                        camera_overlay.startAnimation(fadeOutAnimation)
                    }, 300)
                }

                override fun onAnimationStart(animation: Animation?) {
                    camera_overlay.isVisible = true
                }
            })
        }
        camera_overlay.startAnimation(fadeInAnimation)

        if (!etSkuName.text.toString().isEmpty()){
            //get preview dimensions
            getViewDimensions(viewFinder,0)

            //get overlay dimensions
            getViewDimensions(imgOverlay,1)

            takePhoto()
        } else
            Toast.makeText(
                this,
                "Please select product for shoot",
                Toast.LENGTH_SHORT
            ).show()
    }

    //Edit and update skus
    private fun updateSkus() {
        Utilities.showProgressDialog(this)

        val request = RetrofitClient.buildService(APiService::class.java)

        if (vinNumber.equals("")){
            vinNumber = "sku-000"
            etSkuName.setText("sku-000")

        }

        val editSkuRequest = EditSkuRequest(skuId, vinNumber)

        val call = request.editSku(
            Utilities.getPreference(this, AppConstants.TOKEN_ID),
            editSkuRequest
        )

        call?.enqueue(object : Callback<SkuResponse> {
            override fun onResponse(
                call: Call<SkuResponse>,
                response: Response<SkuResponse>
            ) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful) {
                    if (response.body()?.payload?.data != null) {
                        etSkuName.setText(vinNumber)
                        skuName = etSkuName.text.toString().trim()
                        Utilities.savePrefrence(
                            this@Camera2Activity,
                            AppConstants.SKU_NAME,
                            skuName
                        )
                        imgNext.visibility = View.GONE
                        Toast.makeText(
                            applicationContext,
                            "Updated VIN successfully!!!",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                } else {
                    Utilities.hideProgressDialog()
                    Toast.makeText(
                        applicationContext,
                        "Please try again later!!!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<SkuResponse>, t: Throwable) {
                Log.e("ok", "no way")
                Utilities.hideProgressDialog()
                Toast.makeText(
                    applicationContext,
                    "Server not responding(editSku)",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getOutputDirectory(): File {
        val mediaDir = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            externalMediaDirs.firstOrNull()?.let {
                File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }
        } else {
            TODO("VERSION.SDK_INT < LOLLIPOP")
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    @SuppressLint("RestrictedApi", "UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner

            try {
                cameraProvider = cameraProviderFuture.get()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val preview = Preview.Builder()
                //.setTargetResolution(Size(1280, 720))
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            // Preview
            val viewPort = ViewPort.Builder(
                Rational(4, 3),
                display!!.rotation
            ).build()

            //for exact image cropping
           // val viewPort = findViewById<PreviewView>(R.id.viewFinder).viewPort

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setFlashMode(flashMode).build()

            val useCaseGroup = UseCaseGroup.Builder()
                .addUseCase(preview)
                .addUseCase(imageCapture!!)
                .setViewPort(viewPort!!)
                .build()


            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, useCaseGroup
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))

    }


    private fun takePhoto() {

        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        Utilities.savePrefrence(
            this@Camera2Activity,
            AppConstants.IMAGE_FILE,
            ""
        )
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                @RequiresApi(Build.VERSION_CODES.M)
                @SuppressLint("RestrictedApi")
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {



//                    val myBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath())
//                    imgv_test.setImageBitmap(myBitmap)

                    savedUri = Uri.fromFile(photoFile)

                    //Open camera preview
                    Utilities.savePrefrence(
                        this@Camera2Activity,
                        AppConstants.IMAGE_FILE,
                        photoFile.toString()
                    )

                    Utilities.savePrefrence(
                        this@Camera2Activity,
                        AppConstants.CATEGORY_NAME,
                        catName
                    )

                    uploadImage()
                }
            })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun setImageRaw() {
        val myBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath())

        showSuggestionDialog(myBitmap)

//        var ei: ExifInterface? = null
//        try {
//            ei = ExifInterface(photoFile.getAbsolutePath())
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
////        assert(ei != null)
//        if (ei != null){
//           val orientation = ei!!.getAttributeInt(
//               ExifInterface.TAG_ORIENTATION,
//               ExifInterface.ORIENTATION_UNDEFINED
//           )
//
//        val rotatedBitmap: Bitmap?
//        when (orientation) {
//            ExifInterface.ORIENTATION_ROTATE_90 -> rotatedBitmap = rotateImage(myBitmap!!, 90f)
//            ExifInterface.ORIENTATION_ROTATE_180 -> rotatedBitmap = rotateImage(myBitmap!!, 180f)
//            ExifInterface.ORIENTATION_ROTATE_270 -> rotatedBitmap = rotateImage(myBitmap!!, 270f)
//            ExifInterface.ORIENTATION_NORMAL -> rotatedBitmap = myBitmap
//            else -> rotatedBitmap = myBitmap
//        }
//            showSuggestionDialog(rotatedBitmap)
//        }

        //  imageFile = persistImage(rotatedBitmap!!)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showSuggestionDialog(rotatedBitmap: Bitmap?) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        if (Utilities.getPreference(this, AppConstants.CATEGORY_NAME).equals("Automobiles"))
            dialog.setContentView(R.layout.dialog_suggestion)
        else
            dialog.setContentView(R.layout.footwear_dialog_suggestion)

        val window: Window = dialog.getWindow()!!
        window.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        val flAfter: FrameLayout = dialog.findViewById(R.id.flAfter)
        val ivClickedImage: ImageView = dialog.findViewById(R.id.ivClickedImage)
        val ivClickedImageover: ImageView = dialog.findViewById(R.id.ivClickedImageover)
        val ivClickedImageoverlay: ImageView = dialog.findViewById(R.id.ivClickedImageoverlay)
        val tvReshoot: TextView = dialog.findViewById(R.id.tvReshoot)
        val tvConfirm: TextView = dialog.findViewById(R.id.tvConfirm)

        if (!interiorEnabled) {
            if (!focusedEnabled) {
                ivClickedImage.setImageBitmap(rotatedBitmap)
                ivClickedImageover.setImageBitmap(rotatedBitmap)

                ivClickedImageover.viewTreeObserver.addOnGlobalLayoutListener(object :
                    OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        ivClickedImageover.viewTreeObserver.removeOnGlobalLayoutListener(this)


                        var prw = shootDimensions.previewWidth
                        var prh = shootDimensions.previewHeight

                        var ow = shootDimensions.overlayWidth
                        var oh = shootDimensions.overlayHeight

                        var newW =
                            ow!!.toFloat().div(prw!!.toFloat()).times(ivClickedImageover.width)
                        var newH =
                            oh!!.toFloat().div(prh!!.toFloat()).times(ivClickedImageover.height)


                        var equlizer = (30 * resources.displayMetrics.density).toInt()

                        var params = FrameLayout.LayoutParams(newW.toInt(), newH.toInt())
                        params.gravity = Gravity.CENTER

                        ivClickedImageoverlay.layoutParams = params

                        if (overlaysList != null)
                            Glide.with(this@Camera2Activity).load(
                                AppConstants.BASE_IMAGE_URL + overlaysList[0].display_thumbnail
                            ).into(ivClickedImageoverlay)

//                        if (overlaysList != null)
//                            Glide.with(this@Camera2Activity).load(
//                                AppConstants.BASE_IMAGE_URL + overlaysList[frameNumber - 1].display_thumbnail
//                            ).into(ivClickedImageoverlay)
                    }
                })
            }
            else{
                ivClickedImage.setImageBitmap(rotatedBitmap)
                ivClickedImageover.visibility = View.GONE
                flAfter.visibility = View.GONE
            }
        } else {
            ivClickedImage.setImageBitmap(rotatedBitmap)
            ivClickedImageover.visibility = View.GONE
            flAfter.visibility = View.GONE
        }

        tvReshoot.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
            camera_capture_button.isEnabled = true
            camera_capture_button.isFocusable = true
        })

        tvConfirm.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
            shootCount++

            if (catName.equals("Footwear")) {
                val intent = Intent(
                    this,
                    GenerateMarketplaceActivity::class.java
                )

                intent.putExtra(AppConstants.ALL_IMAGE_LIST, imageFileList)
                intent.putExtra(AppConstants.CATEGORY_NAME, catName)
                intent.putExtra(AppConstants.ALL_FRAME_LIST, imageFileListFrames)
                intent.putExtra(AppConstants.GIF_LIST, gifList)

                Utilities.savePrefrence(this, AppConstants.SKU_NAME, skuName)
                Log.e(
                    "Camera  SKU",
                    Utilities.getPreference(
                        this,
                        AppConstants.SKU_NAME
                    )!!
                )
                startActivity(intent)
                finish()
            } else if (!interiorEnabled) {
                if (!focusedEnabled) {
                    rvSubcategories.visibility = View.VISIBLE

                    imageFileList.add(photoFile!!)

//                    imageFileListFrames.add(frameImageList[frameImageListSelections[frameNumberTemp]].frameNumber)
                   //update this
                    imageFileListFrames.add(4)

                    if (frameNumberTemp < frameImageListSelections.size - 1) {
                        showProgressFrames(++frameNumberTemp)
                        camera_capture_button.isEnabled = true
                        camera_capture_button.isFocusable = true
                    } else {
                        showInteriorDialog()
                    }
                } else {
                    rvSubcategories.visibility = View.GONE
                    imageFocusedFileList.add(photoFile!!)
                    imageFocusedFileListFrames.add(frameFocusedImageList[frameImageListSelections[frameNumberTemp]].frameNumber)
                    if (frameNumberTemp < frameImageListSelections.size - 1) {
                        showProgressFrames(++frameNumberTemp)
                        camera_capture_button.isEnabled = true
                        camera_capture_button.isFocusable = true
                    } else {
                        val intent = Intent(
                            this,
                            GenerateGifActivity::class.java
                        )

                        intent.putExtra(AppConstants.ALL_IMAGE_LIST, imageFileList)
                        intent.putExtra(AppConstants.ALL_FRAME_LIST, imageFileListFrames)
                        intent.putExtra(AppConstants.ALL_INTERIOR_IMAGE_LIST, imageInteriorFileList)
                        intent.putExtra(
                            AppConstants.ALL_INTERIOR_FRAME_LIST,
                            imageInteriorFileListFrames
                        )
                        intent.putExtra(AppConstants.ALL_FOCUSED_IMAGE_LIST, imageFocusedFileList)
                        intent.putExtra(
                            AppConstants.ALL_FOCUSED_FRAME_LIST,
                            imageFocusedFileListFrames
                        )
                        intent.putExtra(AppConstants.GIF_LIST, gifList)

                         Utilities.savePrefrence(this, AppConstants.SKU_NAME, skuName)

                        startActivity(intent)
                        finish()
                    }
                }
            } else if (interiorEnabled) {
                rvSubcategories.visibility = View.GONE
                imageInteriorFileList.add(photoFile!!)
                imageInteriorFileListFrames.add(frameInteriorImageList[frameImageListSelections[frameNumberTemp]].frameNumber)
                if (frameNumberTemp < frameImageListSelections.size - 1) {
                    showProgressFrames(++frameNumberTemp)
                    camera_capture_button.isEnabled = true
                    camera_capture_button.isFocusable = true
                } else {
                    showFocusedDialog()
                }
            }
        })

        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showHint() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        var dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_gif_hint, null)
        dialog.setContentView(dialogView)

        dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

        val ivBeforeShootGif: ImageView = dialog.findViewById(R.id.ivBeforeShootGif)
        val tvContinue: TextView = dialog.findViewById(R.id.tvContinue)

        Glide.with(this).asGif().load(R.raw.before_shoot).into(ivBeforeShootGif)

        tvContinue.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
            showVin()
        })

        dialog.show()
    }

    private fun showVin(){

            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            var dialogView = LayoutInflater.from(this).inflate(R.layout.vin_dialog, null)
            dialog.setContentView(dialogView)

            dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
            val llSubmit: LinearLayout = dialog.findViewById(R.id.llSubmit)
            var etVin: EditText = dialogView.findViewById(R.id.etVin)

            dialog.show()

        llSubmit.setOnClickListener(View.OnClickListener {
            if (etVin.text.isNullOrEmpty()) {
                etVin.setError("Please enter any unique number")
            } else {
                dialog.dismiss()
                vinNumber = etVin.text.toString()
                etSkuName.setText(vinNumber)
                updateSkus()
            }
        })

        }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }


    private fun setProductMap(shootId: String, position: Int, updateShoots: Boolean) {
        Utilities.showProgressDialog(this)

        val updateShootProductRequest = UpdateShootProductRequest(
            shootId,
            subCategoriesList[position].prod_sub_cat_id,
            subCategoriesList[position].sub_cat_name
        )

        val request = RetrofitClient.buildService(APiService::class.java)
        val call = request.updateShootProduct(
            Utilities.getPreference(this, AppConstants.TOKEN_ID),
            updateShootProductRequest
        )

        call?.enqueue(object : Callback<CreateCollectionResponse> {
            override fun onResponse(
                call: Call<CreateCollectionResponse>,
                response: Response<CreateCollectionResponse>
            ) {
                if (response.isSuccessful) {
                    Log.e(
                        "Product map",
                        subCategoriesList[position].prod_cat_id + " " + response.body()!!.msgInfo.msgDescription
                    )
                    setSkuIdMap(shootId, subCategoriesList[position].prod_cat_id, subCategoriesList[position].prod_sub_cat_id, updateShoots)
                }
            }

            override fun onFailure(call: Call<CreateCollectionResponse>, t: Throwable) {
                Log.e("ok", "no way")
                Toast.makeText(
                    applicationContext,
                    "Server not responding(updateShootProduct)",
                    Toast.LENGTH_SHORT
                ).show()

            }
        })
    }

    private fun setSkuIdMap(shootId: String, catId: String, prodId: String, updateShots: Boolean) {
        val updateSkuRequest = UpdateSkuRequest(shootId, prodId)

        val request = RetrofitClient.buildService(APiService::class.java)
        val call = request.updateSku(
            Utilities.getPreference(this, AppConstants.TOKEN_ID),
            updateSkuRequest
        )

        call?.enqueue(object : Callback<UpdateSkuResponse> {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onResponse(
                call: Call<UpdateSkuResponse>,
                response: Response<UpdateSkuResponse>
            ) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful && response.body()!!.payload != null) {
                    Log.e("Sku map", prodId + " " + response.body()!!.msgInfo.msgDescription)
                    etSkuName.setText(vinNumber)
                    skuName = response.body()!!.payload.data.skuName
                    skuId = response.body()!!.payload.data.skuId
                    shootIds = shootId
                    prodIds = prodId
                    catIds = catId

                    Utilities.savePrefrence(
                        this@Camera2Activity, AppConstants.SHOOT_ID, shootIds
                    )
                    Utilities.savePrefrence(
                        this@Camera2Activity, AppConstants.SKU_ID, skuId
                    )


                    /*   if (response.body()!!.payload.data.frames != null)
                           totalFrames = response.body()!!.payload.data.frames.totalFrames
   */
                    //fetch new overlay call here
                   // frameImageList = ArrayList<FrameImages>()
                    //frameImageList.clear()
                    //frameImageList.addAll(response.body()?.payload!!.data.frames.frameImages)

                    frameInteriorImageList = ArrayList()
                    frameFocusedImageList = ArrayList()
                    interiorFrameList = ArrayList()
                    focusedFrameList = ArrayList()

                    frameInteriorImageList.clear()

                    frameInteriorImageList.addAll(response.body()?.payload!!.data.frames.interiorImages)
                    frameFocusedImageList.addAll(response.body()?.payload!!.data.frames.focusImages)
                    interiorFrameList.addAll(response.body()?.payload!!.data.frames.interiorImages)
                    focusedFrameList.addAll(response.body()?.payload!!.data.frames.focusImages)


                    // Interior Frames
                    interiorFramesAdapter = InteriorFramesAdapter(
                        this@Camera2Activity,
                        frameInteriorImageList, pos,
                        object : InteriorFramesAdapter.BtnClickListener {
                            override fun onBtnClick(position: Int) {

                                interiorFramesAdapter.notifyDataSetChanged()
                                rvInteriorFrames.scrollToPosition(position)

                            }
                        })
                    val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(
                        this@Camera2Activity,
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                    rvInteriorFrames.setLayoutManager(layoutManager)
                    rvInteriorFrames.setAdapter(interiorFramesAdapter)

                    Log.e("Interior List ", frameInteriorImageList.toString())
                    interiorFramesAdapter.notifyDataSetChanged()


                    //Focused Frames
                    focusedFramesAdapter = FocusedFramesAdapter(
                        this@Camera2Activity,
                        frameFocusedImageList, pos,
                        object : FocusedFramesAdapter.BtnClickListener {
                            override fun onBtnClick(position: Int) {
                                Log.d("Position click", position.toString())
                                // pos = position

                                focusedFramesAdapter.notifyDataSetChanged()
                                rvFocusedFrames.scrollToPosition(position)
                            }
                        })

                    val layoutManagers: RecyclerView.LayoutManager = LinearLayoutManager(
                        this@Camera2Activity,
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                    rvFocusedFrames.setLayoutManager(layoutManagers)
                    rvFocusedFrames.setAdapter(focusedFramesAdapter)


                    Log.e("focused List ", frameFocusedImageList.toString())
                    focusedFramesAdapter.notifyDataSetChanged()

                    //update as per new overlay api
                   /* if (response.body()?.payload!!.data.frames != null && response.body()?.payload!!.data.frames.frameImages.size > 0) {
                        frameImageList = ArrayList<FrameImages>()
                        frameImageList.clear()
                        frameImageList.addAll(response.body()?.payload!!.data.frames.frameImages)
                    }

                    Glide.with(this@Camera2Activity).load(
                        AppConstants.BASE_IMAGE_URL +
                                frameImageList[frameNumber - 1].displayImage
                    ).into(ivPreview)

                    Glide.with(this@Camera2Activity).load(
                        AppConstants.BASE_IMAGE_URL +
                                frameImageList[frameNumber - 1].displayImage
                    ).into(imgOverlay)*/

                    Glide.with(this@Camera2Activity).load(
                        AppConstants.BASE_IMAGE_URL +
                                overlaysList[frameNumber - 1].display_thumbnail
                    ).into(ivPreview)

                    Glide.with(this@Camera2Activity).load(
                        AppConstants.BASE_IMAGE_URL +
                                overlaysList[frameNumber - 1].display_thumbnail
                    ).into(imgOverlay)

                    Utilities.savePrefrence(this@Camera2Activity, AppConstants.SHOOT_ID, shootIds)
                    Utilities.savePrefrence(this@Camera2Activity, AppConstants.CATEGORY_ID, catIds)
                    Utilities.savePrefrence(this@Camera2Activity, AppConstants.PRODUCT_ID, prodIds)
                    Utilities.savePrefrence(this@Camera2Activity, AppConstants.SKU_NAME, skuName)
                    Utilities.savePrefrence(this@Camera2Activity, AppConstants.SKU_ID, skuId)

                    if (Utilities.getPreference(this@Camera2Activity, AppConstants.FROM)
                            .equals("BA")
                    ) {
                        rvSubcategories.visibility = View.VISIBLE
                    } else
                        rvSubcategories.visibility = View.GONE

                    if (Utilities.getPreference(
                            this@Camera2Activity,
                            AppConstants.FRAME_SHOOOTS
                        ).isNullOrEmpty()
                    ) {
                        Utilities.savePrefrence(
                            this@Camera2Activity,
                            AppConstants.FRAME_SHOOOTS,
                            "8"
                        )
                    }


                    if (catName.equals("Automobiles")) {
                        Utilities.savePrefrence(
                            this@Camera2Activity,
                            AppConstants.FRAME_SHOOOTS,
                            "8"
                        )
                        if (updateShots)
                            setProgressFrame(8)
                    } else if (catName.equals("Footwear")) {
                        Utilities.savePrefrence(
                            this@Camera2Activity,
                            AppConstants.FRAME_SHOOOTS,
                            "5"
                        );
                        setProgressFrame(5)
                    }


                    // etSkuName.visibility = View.GONE

                } else {
                    Toast.makeText(
                        applicationContext,
                        "Server not responding!!!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<UpdateSkuResponse>, t: Throwable) {
                Log.e("ok", "no way")
                Toast.makeText(
                    applicationContext,
                    "Server not responding(updateSku)",
                    Toast.LENGTH_SHORT
                ).show()
                Utilities.hideProgressDialog()
            }
        })
    }

    override fun onBackPressed() {
        showExitDialog()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode === RESULT_OK) {
            if (requestCode === SELECT_PICTURE) {
                val selectedImageUri: Uri = data!!.getData()!!
                selectedImagePath = ImageFilePath.getPath(applicationContext, data.getData()!!);
                photoFile = File(selectedImagePath)
                savedUri = Uri.fromFile(photoFile)

                //  photoFilePath = compressFileFromBitmap()

                Utilities.savePrefrence(
                    this@Camera2Activity,
                    AppConstants.IMAGE_FILE,
                    photoFile.toString()
                )

            }
        }
    }

    //Custom frame selection

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    fun showCustomSelectionDialog() {

        var lastSelectedAngles = Utilities.getPreference(this, AppConstants.FRAME_SHOOOTS)
        var newSelectedAngles = Utilities.getPreference(this, AppConstants.FRAME_SHOOOTS)

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_angle_selection)
        val window: Window = dialog.getWindow()!!
        window.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        val tvProceed : TextView = dialog.findViewById(R.id.tvProceed)
        val npShoots : NumberPicker = dialog.findViewById(R.id.npShoots)

        val valuesShoots = arrayOf("4 Angles", "8 Angles", "12 Angles"/*, "24 Angles"*/)

        when(Utilities.getPreference(this, AppConstants.FRAME_SHOOOTS)){
            "4" -> npShoots.setMinValue(0)
            "8" -> npShoots.setMinValue(1)
            "12" -> npShoots.setMinValue(2)
        }

        npShoots.setMinValue(0) //from array first value
        //Specify the maximum value/number of NumberPicker
        npShoots.setMaxValue(valuesShoots.size - 1); //to array last value

        //Specify the NumberPicker data source as array elements
        npShoots.setDisplayedValues(valuesShoots);

        //Set a value change listener for NumberPicker

        npShoots.setOnValueChangedListener(OnValueChangeListener { picker, oldVal, newVal -> //Display the newly selected value from picker
            newSelectedAngles = valuesShoots[newVal]

            if (valuesShoots[newVal].equals("4 Angles")) {
                Utilities.savePrefrence(
                    this@Camera2Activity,
                    AppConstants.FRAME_SHOOOTS,
                    "4"
                )
                //   setNumberPickerTextColor(npShoots, getColor(R.color.primary))
                setProgressFrame(4)
            } else if (valuesShoots[newVal].equals("8 Angles")) {
                Utilities.savePrefrence(
                    this@Camera2Activity,
                    AppConstants.FRAME_SHOOOTS,
                    "8"
                );
                //   setNumberPickerTextColor(npShoots, getColor(R.color.primary))
                setProgressFrame(8)
            } else if (valuesShoots[newVal].equals("12 Angles")) {
                Utilities.savePrefrence(
                    this@Camera2Activity,
                    AppConstants.FRAME_SHOOOTS,
                    "12"
                );
                setProgressFrame(12)
            } else if (valuesShoots[newVal].equals("24 Angles")) {
                Utilities.savePrefrence(
                    this@Camera2Activity,
                    AppConstants.FRAME_SHOOOTS,
                    "24"
                );
                setProgressFrame(24)
            }
        })

        tvProceed.setOnClickListener(View.OnClickListener {
            if (lastSelectedAngles != newSelectedAngles)
                isSubcatgoryConfirmed = false

            dialog.dismiss()
        })
        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    fun showFootwearCustomSelectionDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_angle_selection)
        val window: Window = dialog.getWindow()!!
        window.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        val tvProceed: TextView = dialog.findViewById(R.id.tvProceed)
        val npShoots: NumberPicker = dialog.findViewById(R.id.npShoots)

        val valuesShoots = arrayOf("5 Angles"/*, "8 Angles", "12 Angles", "24 Angles"*/)

        npShoots.setMinValue(0); //from array first value
        //Specify the maximum value/number of NumberPicker
        npShoots.setMaxValue(valuesShoots.size - 1); //to array last value

        //Specify the NumberPicker data source as array elements
        npShoots.setDisplayedValues(valuesShoots);

        npShoots.setOnValueChangedListener(OnValueChangeListener { picker, oldVal, newVal -> //Display the newly selected value from picker
            if (valuesShoots[newVal].equals("5 Angles")) {
                Utilities.savePrefrence(
                    this@Camera2Activity,
                    AppConstants.FRAME_SHOOOTS,
                    "5"
                );
                setProgressFrame(5)
            }
        })

        Log.e(
            "VAlue selected  ",
            Utilities.getPreference(this, AppConstants.FRAME_SHOOOTS).toString()
        )

        tvProceed.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
        })
        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun showInteriorDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_interior_hint)
        val window: Window = dialog.getWindow()!!
        window.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        val tvSkip: TextView = dialog.findViewById(R.id.tvSkip)
        val tvShootNowInterior: TextView = dialog.findViewById(R.id.tvShootNowInterior)

        tvSkip.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
            showFocusedDialog()

        })

        tvShootNowInterior.setOnClickListener(View.OnClickListener {
            tvSkipShoot.visibility = View.VISIBLE
            cardOverlay.visibility = View.GONE
            camera_capture_button.isEnabled = true
            camera_capture_button.isFocusable = true
            interiorEnabled = true
            frameNumberTemp = 0
            setProgressFrame(9)
            rvSubcategories.visibility = View.GONE
            rvInteriorFrames.visibility = View.VISIBLE
            dialog.dismiss()
        })

        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun showFocusedDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_focused_hint)
        val window: Window = dialog.getWindow()!!
        window.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        val tvSkipFocused: TextView = dialog.findViewById(R.id.tvSkipFocused)
        val tvShootNowFocused: TextView = dialog.findViewById(R.id.tvShootNowFocused)

        tvSkipFocused.setOnClickListener(View.OnClickListener {
            val intent = Intent(
                this,
                GenerateGifActivity::class.java
            )

            intent.putExtra(AppConstants.ALL_IMAGE_LIST, imageFileList)
            intent.putExtra(AppConstants.ALL_FRAME_LIST, imageFileListFrames)
            intent.putExtra(AppConstants.ALL_INTERIOR_IMAGE_LIST, imageInteriorFileList)
            intent.putExtra(AppConstants.ALL_INTERIOR_FRAME_LIST, imageInteriorFileListFrames)
            intent.putExtra(AppConstants.ALL_FOCUSED_IMAGE_LIST, imageFocusedFileList)
            intent.putExtra(AppConstants.ALL_FOCUSED_FRAME_LIST, imageFocusedFileListFrames)
            intent.putExtra(AppConstants.GIF_LIST, gifList)

            Utilities.savePrefrence(this, AppConstants.SKU_NAME, skuName)
            Log.e(
                "Camera  SKU",
                Utilities.getPreference(
                    this,
                    AppConstants.SKU_NAME
                )!!
            )
            startActivity(intent)
            finish()
            dialog.dismiss()
        })

        tvShootNowFocused.setOnClickListener(View.OnClickListener {
            tvSkipShoot.visibility = View.VISIBLE
            cardOverlay.visibility = View.GONE
            camera_capture_button.isEnabled = true
            camera_capture_button.isFocusable = true
            interiorEnabled = false
            focusedEnabled = true
            frameNumberTemp = 0
            setProgressFrame(9)
            rvSubcategories.visibility = View.GONE
            rvInteriorFrames.visibility = View.GONE
            rvFocusedFrames.visibility = View.VISIBLE
            dialog.dismiss()
        })

        dialog.show()
    }

    fun showExitDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_exit)
        val dialogButtonYes: TextView = dialog.findViewById(R.id.btnYes)
        val dialogButtonNo: TextView = dialog.findViewById(R.id.btnNo)

        dialogButtonYes.setOnClickListener(View.OnClickListener {
            Utilities.savePrefrence(this@Camera2Activity, AppConstants.SHOOT_ID, "")
            Utilities.savePrefrence(this@Camera2Activity, AppConstants.CATEGORY_ID, "")
            Utilities.savePrefrence(this@Camera2Activity, AppConstants.PRODUCT_ID, "")
            Utilities.savePrefrence(this@Camera2Activity, AppConstants.SKU_NAME, "")
            Utilities.savePrefrence(this@Camera2Activity, AppConstants.SKU_ID, "")
            val intent = Intent(this, MainDashboardActivity::class.java)
            startActivity(intent)
            finish()

            val updateSkuResponseList = ArrayList<UpdateSkuResponse>()
            updateSkuResponseList.clear()

            Utilities.setList(
                this@Camera2Activity,
                AppConstants.FRAME_LIST, updateSkuResponseList
            )
            dialog.dismiss()

        })
        dialogButtonNo.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
        })
        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun uploadImage() {
        Utilities.savePrefrence(this, AppConstants.REPLACED_IMAGE, "")

        photoFile = File(
            Utilities.getPreference(
                this,
                AppConstants.IMAGE_FILE
            )
        )
        setImageRaw()
    }
}