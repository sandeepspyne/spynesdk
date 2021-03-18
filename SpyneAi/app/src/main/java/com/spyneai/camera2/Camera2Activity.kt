package com.spyneai.camera2

import FrameImages
import SubcategoriesResponse
import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Paint
import android.media.AudioManager
import android.media.ExifInterface
import android.media.MediaActionSound
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.text.Editable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.Rational
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.*
import android.widget.NumberPicker.OnValueChangeListener
import android.widget.TextView.BufferType
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.activity.DashboardActivity
import com.spyneai.activity.GenerateGifActivity
import com.spyneai.activity.GenrateMarketplaceActivity
import com.spyneai.adapter.ProgressAdapter
import com.spyneai.adapter.SubCategoriesAdapter
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClient
import com.spyneai.model.shoot.*
import com.spyneai.model.sku.SkuResponse
import com.spyneai.model.skuedit.EditSkuRequest
import com.spyneai.model.skumap.UpdateSkuResponse
import com.spyneai.model.subcategories.Data
import com.spyneai.needs.AppConstants
import com.spyneai.needs.ImageFilePath
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_camera.*
import kotlinx.android.synthetic.main.activity_camera.camera_capture_button
import kotlinx.android.synthetic.main.activity_camera.camera_overlay
import kotlinx.android.synthetic.main.activity_camera.etSkuName
import kotlinx.android.synthetic.main.activity_camera.imgBack
import kotlinx.android.synthetic.main.activity_camera.imgNext
import kotlinx.android.synthetic.main.activity_camera.imgOverlay
import kotlinx.android.synthetic.main.activity_camera.ivGallery
import kotlinx.android.synthetic.main.activity_camera.ivPreview
import kotlinx.android.synthetic.main.activity_camera.rvProgress
import kotlinx.android.synthetic.main.activity_camera.rvSubcategories
import kotlinx.android.synthetic.main.activity_camera.tvshoot
import kotlinx.android.synthetic.main.activity_camera.viewFinder
import kotlinx.android.synthetic.main.activity_camera2.*
import kotlinx.android.synthetic.main.activity_camera_preview.*
import kotlinx.android.synthetic.main.dialog_spinner.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.lang.reflect.Field
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList


typealias LumaListener = (luma: Double) -> Unit

class Camera2Activity : AppCompatActivity() , SubCategoriesAdapter.BtnClickListener {
    private lateinit var gifList: ArrayList<String>
    private lateinit var photoFilePath: File
    private var savedUri: Uri? = null
    private lateinit var photoFile: File
    private val PICK_IMAGE: Int = 1
    private var pos: Int = 0
    private lateinit var progressList: List<Data>
    private lateinit var progressAdapter: ProgressAdapter
    private lateinit var camera: Camera
    private var flashMode: Int = ImageCapture.FLASH_MODE_OFF

    private val SELECT_PICTURE = 1
    private var selectedImagePath: String? = null
    private lateinit var selectedItemFrame : String

    //   private var btnlistener: SubCategoriesAdapter.BtnClickListener? = null
    private var imageCapture: ImageCapture? = null

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    lateinit var cameraProvider : ProcessCameraProvider

    lateinit var subCategoriesList : ArrayList<Data>
    lateinit var subCategoriesAdapter: SubCategoriesAdapter
    var catName : String = "Category"

    lateinit var skuId : String
    var skuName : String = "SKU"
    lateinit var shootIds : String
    lateinit var catIds : String
    lateinit var prodIds : String
    var frameNumber : Int = 1
    var totalFrames: Int = 0
    var frameNumberTemp: Int = 0

    var frameImage: String = ""
    lateinit var frameImageList: ArrayList<FrameImages>
    lateinit var frameImageListSelections : ArrayList<Int>
    lateinit var frameINteriorImageListSelections : ArrayList<Int>

    public var imageFile: File? = null

    public lateinit var imageFileList : ArrayList<File>
    public lateinit var imageFileListFrames : ArrayList<Int>

    var catId = ""


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera2)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        showHint()
        setSubCategories()
        fetchSubCategories()
        setPermissions()
        setCameraActions()
        setSku()

        imageFileList = ArrayList<File>()
        imageFileListFrames = ArrayList<Int>()
        gifList = ArrayList<String>()
        frameImageListSelections = ArrayList<Int>()

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

        /*frameNumber = intent.getIntExtra(AppConstants.FRAME, 1)
        totalFrames = intent.getIntExtra(AppConstants.TOTAL_FRAME, 1)
        if(intent.getStringExtra(AppConstants.FRAME_IMAGE) != null)
            frameImage = intent.getStringExtra(AppConstants.FRAME_IMAGE)!!
*/
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
        }
        else{
            ivPreview.visibility = View.GONE
            imgOverlay.visibility = View.INVISIBLE
        }
    }

    private fun setSubCategories() {
        subCategoriesList = ArrayList<Data>()
        subCategoriesAdapter = SubCategoriesAdapter(
                this,
                subCategoriesList, pos,
                object : SubCategoriesAdapter.BtnClickListener {
                    override fun onBtnClick(position: Int) {
                        Log.d("Position click", position.toString())
                        setProductMap(
                                Utilities.getPreference(
                                        this@Camera2Activity,
                                        AppConstants.SHOOT_ID
                                ).toString(), position
                        )
                        // pos = position
                        subCategoriesAdapter.notifyDataSetChanged()
                    }
                })
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
        )
        rvSubcategories.setLayoutManager(layoutManager)
        rvSubcategories.setAdapter(subCategoriesAdapter)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setProgressFrame(num: Int) {

        frameImageListSelections.clear()
        if (num == 4) {
            frameImageListSelections .add(0)
            frameImageListSelections .add(9)
            frameImageListSelections .add(18)
            frameImageListSelections .add(27)
        } else if (num == 5){
            frameImageListSelections .add(0)
            frameImageListSelections .add(1)
            frameImageListSelections .add(2)
            frameImageListSelections .add(3)
            frameImageListSelections .add(4)
        }
        showProgressFrames(frameNumberTemp)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    public fun showProgressFrames(frameNumberTemp: Int) {
        if (frameNumberTemp == 0)
        {
            tvshoot.setOnClickListener(View.OnClickListener {
                if (catName.equals("Automobiles")) {
                    showCustomSelectionDialog()
                } else if (catName.equals("Footwear")) {
                    showFootwearCustomSelectionDialog()
                }
                tvshoot.isEnabled = true
                tvshoot.isFocusable = true
            })
        }
        else{
            tvshoot.isEnabled = false
            tvshoot.isFocusable = false
            rvSubcategories.visibility = View.GONE
        }
        frameNumber = frameImageListSelections[frameNumberTemp] +1
        totalFrames = frameImageListSelections.size

        if (frameImageList.size > 0) {
            Glide.with(this@Camera2Activity).load(
                    AppConstants.BASE_IMAGE_URL +
                            frameImageList[frameNumber - 1].displayImage
            ).into(ivPreview)

            Glide.with(this@Camera2Activity).load(
                    AppConstants.BASE_IMAGE_URL +
                            frameImageList[frameNumber - 1].displayImage
            ).into(imgOverlay)

            ivPreview.visibility = View.VISIBLE
            imgOverlay.visibility = View.VISIBLE
        }

        progressList = ArrayList<Data>()

        tvshoot.setText("Shots " + (frameNumberTemp + 1) + "/" + totalFrames)
        val framesList : List<Int> = ArrayList(totalFrames)

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
        for (i in frameNumberTemp+1..totalFrames-1)
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

        val request = RetrofitClient.buildService(APiService::class.java)
        val call = request.getSubCategories(
                Utilities.getPreference(this, AppConstants.tokenId), catIds
        )

        call?.enqueue(object : Callback<SubcategoriesResponse> {
            override fun onResponse(
                    call: Call<SubcategoriesResponse>,
                    response: Response<SubcategoriesResponse>
            ) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful) {
                    if (response.body()?.payload?.data?.size!! > 0) {
                        subCategoriesList.addAll(response.body()?.payload?.data!!)
                        Log.e("subCategoriesList..", subCategoriesList.toString())

                    }
                    subCategoriesAdapter.notifyDataSetChanged()

                    if (Utilities.getPreference(this@Camera2Activity, AppConstants.FROM)
                                    .equals("BA")
                    )
                        setProductMap(
                                Utilities.getPreference(
                                        this@Camera2Activity,
                                        AppConstants.SHOOT_ID
                                ).toString(), 0
                        )


                }
            }

            override fun onFailure(call: Call<SubcategoriesResponse>, t: Throwable) {
                Log.e("ok", "no way")
                Utilities.hideProgressDialog()
                Toast.makeText(
                        applicationContext,
                        "Server not responding!!!",
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

/*
        llCam.setOnClickListener(View.OnClickListener {
            if (!my_toolbar2.isVisible) {
                my_toolbar2.visibility = View.VISIBLE
                rvSubcategories.visibility = View.VISIBLE
                llProgress2.visibility = View.VISIBLE

            */
/*    my_toolbar2.setAlpha(0.0f)
                rvSubcategories.setAlpha(0.0f)
                llProgress2.setAlpha(0.0f)

                my_toolbar2.animate()
                        .translationY(my_toolbar2.getHeight().toFloat())
                        .alpha(0.5f)
                        .setListener(null)
                rvSubcategories.animate()
                        .translationY(my_toolbar2.getHeight().toFloat())
                        .alpha(0.5f)
                        .setListener(null)
                llProgress2.animate()
                        .translationY(my_toolbar2.getHeight().toFloat())
                        .alpha(0.5f)
                        .setListener(null)*//*

            } else {
                my_toolbar2.visibility = View.GONE
                rvSubcategories.visibility = View.GONE
                llProgress2.visibility = View.GONE
               */
/* my_toolbar2.animate()
                        .translationY(0F)
                        .alpha(0.0f)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator?) {
                                super.onAnimationEnd(animation)
                                my_toolbar2.setVisibility(View.GONE)
                            }
                        })
                rvSubcategories.animate()
                        .translationY(0F)
                        .alpha(0.0f)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator?) {
                                super.onAnimationEnd(animation)
                                rvSubcategories.setVisibility(View.GONE)
                            }
                        })
                llProgress2.animate()
                        .translationY(0F)
                        .alpha(0.0f)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator?) {
                                super.onAnimationEnd(animation)
                                llProgress2.setVisibility(View.GONE)
                            }
                        })*//*

            }
        })
*/

        imgNext.setOnClickListener(View.OnClickListener {
            if (frameNumber == 1) {
                if (Utilities.isNetworkAvailable(this)) {
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
        camera_capture_button.setOnClickListener(View.OnClickListener {
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

            if (!etSkuName.text.toString().isEmpty())
                takePhoto()
            else
                Toast.makeText(
                        this,
                        "Please select product for shoot",
                        Toast.LENGTH_SHORT
                ).show()
        })

        ivGallery.setOnClickListener(View.OnClickListener {
            Toast.makeText(
                    applicationContext,
                    "Coming Soon...",
                    Toast.LENGTH_SHORT
            ).show()

            /* val checkSelfPermission = ContextCompat.checkSelfPermission(
                 this,
                 android.Manifest.permission.WRITE_EXTERNAL_STORAGE
             )
             if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
                 //Requests permissions to be granted to this application at runtime
                 ActivityCompat.requestPermissions(
                     this,
                     arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1
                 )
             } else {
                 defaultSet()
             }*/
        })


        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    //Edit and update skus
    private fun updateSkus() {
        Utilities.showProgressDialog(this)

        val request = RetrofitClient.buildService(APiService::class.java)

        val editSkuRequest = EditSkuRequest(skuId, etSkuName.text.toString().trim())

        val call = request.editSku(
                Utilities.getPreference(this, AppConstants.tokenId),
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
                        etSkuName.setText(etSkuName.text.toString().trim())
                        skuName = etSkuName.text.toString().trim()
                        Utilities.savePrefrence(this@Camera2Activity,
                                AppConstants.SKU_NAME,
                                skuName)
                        imgNext.visibility = View.GONE
                        Toast.makeText(
                                applicationContext,
                                "Updated SKU name successfully!!!",
                                Toast.LENGTH_SHORT
                        ).show()

                    }
                } else {
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
                        "Server not responding!!!",
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

    @SuppressLint("RestrictedApi", "UnsafeExperimentalUsageError")
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            cameraProvider = cameraProviderFuture.get()

            // Preview
            val viewPort = ViewPort.Builder(
                    Rational(4, 3),
                    display!!.rotation
            ).build()

            val preview = Preview.Builder()
                    //.setTargetResolution(Size(1280, 720))
                    .build()
                    .also {
                        it.setSurfaceProvider(viewFinder.surfaceProvider)
                    }

            imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setFlashMode(flashMode).build()

            val useCaseGroup = UseCaseGroup.Builder()
                    .addUseCase(preview)
                    .addUseCase(imageCapture!!)
                    .setViewPort(viewPort)
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

                        savedUri = Uri.fromFile(photoFile)

                        //   photoFile = getCompresedFile(savedUri)

                        // val filePathString : String = decodeFile(photoFile.toString())!!

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

                        val msg = "Photo capture succeeded: $savedUri"
                        Log.d(TAG, msg)
                    }
                })
    }


    @RequiresApi(Build.VERSION_CODES.M)
    fun setImageRaw()
    {
        val myBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath())
        var ei: ExifInterface? = null
        try {
            ei = ExifInterface(photoFile.getAbsolutePath())
        } catch (e: IOException) {
            e.printStackTrace()
        }
        assert(ei != null)
        val orientation = ei!!.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
        )
        val rotatedBitmap: Bitmap?
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotatedBitmap = rotateImage(myBitmap!!, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotatedBitmap = rotateImage(myBitmap!!, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotatedBitmap = rotateImage(myBitmap!!, 270f)
            ExifInterface.ORIENTATION_NORMAL -> rotatedBitmap = myBitmap
            else -> rotatedBitmap = myBitmap
        }

        showSuggestionDialog(rotatedBitmap)

        //  imageFile = persistImage(rotatedBitmap!!)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showSuggestionDialog(rotatedBitmap: Bitmap?) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_suggestion)

        val window: Window = dialog.getWindow()!!
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT)

        val ivClickedImage : ImageView = dialog.findViewById(R.id.ivClickedImage)
        val ivClickedImageover : ImageView = dialog.findViewById(R.id.ivClickedImageover)
        val ivClickedImageoverlay : ImageView = dialog.findViewById(R.id.ivClickedImageoverlay)
        val tvReshoot : TextView = dialog.findViewById(R.id.tvReshoot)
        val tvConfirm : TextView = dialog.findViewById(R.id.tvConfirm)

        ivClickedImage.setImageBitmap(rotatedBitmap)
        ivClickedImageover.setImageBitmap(rotatedBitmap)
        Glide.with(this@Camera2Activity).load(
                AppConstants.BASE_IMAGE_URL + frameImageList[frameNumber - 1].displayImage
        ).into(ivClickedImageoverlay)


        tvReshoot.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
            camera_capture_button.isEnabled = true
            camera_capture_button.isFocusable = true
        })

        tvConfirm.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
            imageFileList.add(photoFile!!)
            imageFileListFrames.add(frameImageList[frameImageListSelections[frameNumberTemp]].frameNumber)
            if (frameNumberTemp < frameImageListSelections.size - 1) {
                showProgressFrames(++frameNumberTemp)

                camera_capture_button.isEnabled = true
                camera_capture_button.isFocusable = true

            } else if (catName.equals("Automobiles")) {
                val intent = Intent(
                        this,
                        GenerateGifActivity::class.java)

                intent.putExtra(AppConstants.ALL_IMAGE_LIST, imageFileList)
                intent.putExtra(AppConstants.ALL_FRAME_LIST, imageFileListFrames)
                intent.putExtra(AppConstants.GIF_LIST, gifList)
                intent.putExtra(AppConstants.CATEGORY_NAME, catName)

                Utilities.savePrefrence(this, AppConstants.SKU_NAME, skuName)
                Log.e("Camera  SKU",
                        Utilities.getPreference(this,
                                AppConstants.SKU_NAME)!!)
                startActivity(intent)
                finish()
            } else if (catName.equals("Footwear")){
                val intent = Intent(
                    this,
                    GenrateMarketplaceActivity::class.java
                )

                intent.putExtra(AppConstants.ALL_IMAGE_LIST, imageFileList)
                intent.putExtra(AppConstants.CATEGORY_NAME, catName)
                intent.putExtra(AppConstants.ALL_FRAME_LIST, imageFileListFrames)
                intent.putExtra(AppConstants.GIF_LIST, gifList)
                intent.putExtra(AppConstants.CATEGORY_NAME, catName)

                Utilities.savePrefrence(this, AppConstants.SKU_NAME, skuName)
                Log.e("Camera  SKU",
                    Utilities.getPreference(this,
                        AppConstants.SKU_NAME)!!)
                startActivity(intent)
                finish()

            }
        })
        dialog.show()
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun showHint() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_hint)

        val window: Window = dialog.getWindow()!!
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT)

        val tvHint : TextView = dialog.findViewById(R.id.tvHint)
        val tvHintMove : TextView = dialog.findViewById(R.id.tvHintMove)
        val tvContinue : TextView = dialog.findViewById(R.id.tvContinue)
        val builder = SpannableStringBuilder()
        val builders = SpannableStringBuilder()

        //First text
        val black = "Shoot "
        val blackSpannable = SpannableString(black)
        blackSpannable.setSpan(ForegroundColorSpan(getColor(R.color.black)), 0, black.length, 0)
        builder.append(blackSpannable)

        val red = "Outdoors"
        val redSpannable = SpannableString(red)
        redSpannable.setSpan(ForegroundColorSpan(getColor(R.color.primary)), 0, red.length, 0)
        builder.append(redSpannable)

        val blue = " to avoid irregular reflections"
        val blueSpannable = SpannableString(blue)
        blueSpannable.setSpan(ForegroundColorSpan(getColor(R.color.black)), 0, blue.length, 0)
        builder.append(blueSpannable)

        tvHint.setText(builder, BufferType.SPANNABLE)


        //Second text
        val blacks = "Move "
        val blacksSpannable = SpannableString(blacks)
        blacksSpannable.setSpan(ForegroundColorSpan(getColor(R.color.black)), 0, blacks.length, 0)
        builders.append(blacksSpannable)

        val reds = "Left"
        val redsSpannable = SpannableString(reds)
        redsSpannable.setSpan(ForegroundColorSpan(getColor(R.color.primary)), 0, reds.length, 0)
        builders.append(redsSpannable)

        val blues = " after each shot"
        val bluesSpannable = SpannableString(blues)
        bluesSpannable.setSpan(ForegroundColorSpan(getColor(R.color.black)), 0, blues.length, 0)
        builders.append(bluesSpannable)

        tvHintMove.setText(builders, BufferType.SPANNABLE)

        tvContinue.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
        })

        dialog.show()
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

    private fun setSku() {
        etSkuName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
/*
                if (!s.isNullOrEmpty())
                    imgNext.visibility = View.VISIBLE
                else
                    imgNext.visibility = View.INVISIBLE
*/

            }
        })

/*
        imgNext.setOnClickListener(View.OnClickListener {
            val createCollectionRequest = UpdateShootCategoryRequest("Spyne SKU");

            val request = RetrofitClient.buildService(APiService::class.java)
            val call = request.createCollection(Utilities.getPreference(this,AppConstants.tokenId),createCollectionRequest)

            call?.enqueue(object : Callback<UpdateShootCategoryResponse>{
                override fun onResponse(call: Call<UpdateShootCategoryResponse>, response: Response<UpdateShootCategoryResponse>) {
                    if (response.isSuccessful){
                        Log.e("ok", response.body()?.payload?.data?.shootId.toString())
                    }
                }
                override fun onFailure(call: Call<UpdateShootCategoryResponse>, t: Throwable) {
                    Log.e("ok", "no way")

                }
            })
        })
*/
    }


    private fun setProductMap(shootId: String, position: Int) {
        Utilities.showProgressDialog(this)

        val updateShootProductRequest = UpdateShootProductRequest(
                shootId,
                subCategoriesList[position].prodId,
                subCategoriesList[position].displayName
        )

        val request = RetrofitClient.buildService(APiService::class.java)
        val call = request.updateShootProduct(
                Utilities.getPreference(this, AppConstants.tokenId),
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
                            subCategoriesList[position].prodId + " " + response.body()!!.msgInfo.msgDescription
                    )
                    setSkuIdMap(
                            shootId,
                            subCategoriesList[position].catId, subCategoriesList[position].prodId
                    )
                }
            }

            override fun onFailure(call: Call<CreateCollectionResponse>, t: Throwable) {
                Log.e("ok", "no way")

            }
        })
    }

    private fun setSkuIdMap(shootId: String, catId: String, prodId: String) {
        val updateSkuRequest = UpdateSkuRequest(shootId, prodId)

        val request = RetrofitClient.buildService(APiService::class.java)
        val call = request.updateSku(
                Utilities.getPreference(this, AppConstants.tokenId),
                updateSkuRequest
        )

        call?.enqueue(object : Callback<UpdateSkuResponse> {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onResponse(
                    call: Call<UpdateSkuResponse>,
                    response: Response<UpdateSkuResponse>
            ) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful && response.body()!!.payload.data != null) {
                    Log.e("Sku map", prodId + " " + response.body()!!.msgInfo.msgDescription)
                    etSkuName.setText(response.body()!!.payload.data.skuName)
                    skuName = response.body()!!.payload.data.skuName
                    skuId = response.body()!!.payload.data.skuId
                    shootIds = shootId
                    prodIds = prodId
                    catIds = catId

                    Utilities.savePrefrence(this@Camera2Activity, AppConstants.SHOOT_ID, shootIds
                    )
                    Utilities.savePrefrence(this@Camera2Activity, AppConstants.SKU_ID, skuId
                    )


                    /*   if (response.body()!!.payload.data.frames != null)
                           totalFrames = response.body()!!.payload.data.frames.totalFrames
   */
                    if (response.body()?.payload!!.data.frames != null && response.body()?.payload!!.data.frames.frameImages.size > 0) {
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

                    if (Utilities.getPreference(this@Camera2Activity,
                                    AppConstants.FRAME_SHOOOTS).isNullOrEmpty()) {
                        Utilities.savePrefrence(
                                this@Camera2Activity,
                                AppConstants.FRAME_SHOOOTS,
                                "4"
                        );
                    }
                    if (catName.equals("Automobiles")) {
                        setProgressFrame(4)
                    } else if (catName.equals("Footwear")) {
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
                        "Server not responding!!!",
                        Toast.LENGTH_SHORT
                ).show()
                Utilities.hideProgressDialog()
            }
        })
    }

    override fun onBtnClick(position: Int) {
        Log.d("Position", position.toString())
    }

    public fun getInstance(): Camera2Activity {
        return this
    }

    override fun onBackPressed() {
//        super.onBackPressed()
        showExitDialog()
    }

    private fun defaultSet() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Select Picture"
                ), SELECT_PICTURE
        )
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

                // uploadImage()
                /*  val intent = Intent(this@Camera2Activity, CameraPreviewActivity::class.java)
                  Utilities.savePrefrence(this@Camera2Activity, AppConstants.FROM, "AB")
                  intent.putExtra(AppConstants.FRAME, frameNumber)
                  intent.putExtra(AppConstants.TOTAL_FRAME, totalFrames)
                  startActivity(intent)*/
            }
        }
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
        val path = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        return Uri.parse(path.toString())
    }

    fun getRealPathFromURI(uri: Uri?): String? {
        val cursor: Cursor? = contentResolver.query(uri!!, null, null, null, null)
        cursor!!.moveToFirst()
        val idx: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
        return cursor.getString(idx)
    }

/*
    private fun decodeFile(path: String): String? {
        var strMyImagePath: String? = null
        var scaledBitmap: Bitmap? = null
        try {
            // Part 1: Decode image
            val unscaledBitmap: Bitmap =
                    ScalingUtilities().decodeFile(path, 700, 700, ScalingUtilities.ScalingLogic.FIT)!!
            scaledBitmap = if (!(unscaledBitmap.width <= 500 && unscaledBitmap.height <= 500)) {
                // Part 2: Scale image
                ScalingUtilities().createScaledBitmap(
                        unscaledBitmap,
                        700,
                        700,
                        ScalingUtilities.ScalingLogic.FIT
                )
            } else {
                unscaledBitmap.recycle()
                return path
            }

            // Store to tmp file
            val extr: String = Environment.getExternalStorageDirectory().toString()
            val mFolder = File("$extr/myTmpDir")
            if (!mFolder.exists()) {
                mFolder.mkdir()
            }
            val s = "tmp.png"
            val f = File(mFolder.absolutePath, s)
            strMyImagePath = f.absolutePath
            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(f)
                scaledBitmap!!.compress(Bitmap.CompressFormat.PNG, 70, fos)
                fos.flush()
                fos.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            scaledBitmap!!.recycle()

            //Rotate if phone rotates
            val tempBitmap = scaledBitmap

            var ei: ExifInterface? = null
            try {
                ei = ExifInterface(photoFile.getAbsolutePath())
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (BuildConfig.DEBUG && ei == null) {
                error("Assertion failed")
            }
            val orientation = ei!!.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED
            )
            val rotatedBitmap: Bitmap?
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotatedBitmap = rotateImage(tempBitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotatedBitmap = rotateImage(
                        tempBitmap,
                        180f
                )
                ExifInterface.ORIENTATION_ROTATE_270 -> rotatedBitmap = rotateImage(
                        tempBitmap,
                        270f
                )
                ExifInterface.ORIENTATION_NORMAL -> rotatedBitmap = tempBitmap
                else -> rotatedBitmap = tempBitmap
            }

            scaledBitmap = rotatedBitmap
            scaledBitmap!!.recycle()
            //End of rotations

        } catch (e: Throwable) {
        }
        return strMyImagePath ?: path
    }
*/

    public fun rotateImage(source: Bitmap, angle: Float): Bitmap? {
        val matrix = Matrix()

        matrix.postRotate(angle)
        return Bitmap.createBitmap(
                source, 0, 0, source.width, source.height,
                matrix, true
        )
    }

    public fun persistImage(bitmap: Bitmap): File? {
        var imageFile: File? = null
        if (applicationContext != null) {
            val filesDir: File = applicationContext.getFilesDir()
            imageFile = File(filesDir, "photo" + ".png")
            val os: OutputStream
            try {
                os = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
                os.flush()
                os.close()
            } catch (e: Exception) {
                Log.e(javaClass.simpleName, "Error writing bitmap", e)
            }
        }
        return imageFile
    }

    //Custom frame selection


    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    fun showCustomSelectionDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_spinner)
        val window: Window = dialog.getWindow()!!
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT)

        val tvProceed : TextView = dialog.findViewById(R.id.tvProceed)
        val npShoots : NumberPicker = dialog.findViewById(R.id.npShoots)

        val valuesShoots = arrayOf("4 Angles"/*, "8 Angles", "12 Angles", "24 Angles"*/)

        npShoots.setMinValue(0); //from array first value
        //Specify the maximum value/number of NumberPicker
        npShoots.setMaxValue(valuesShoots.size - 1); //to array last value

        //Specify the NumberPicker data source as array elements
        npShoots.setDisplayedValues(valuesShoots);

        //Set a value change listener for NumberPicker
        //Set a value change listener for NumberPicker

//        if (catName.equals("Automobiles")) {
//            setProgressFrame(4)
//        } else if (catName.equals("Footwear")) {
//            setProgressFrame(5)
//        }

        npShoots.setOnValueChangedListener(OnValueChangeListener { picker, oldVal, newVal -> //Display the newly selected value from picker
            if (valuesShoots[newVal].equals("4 Angles")) {
                Utilities.savePrefrence(
                        this@Camera2Activity,
                        AppConstants.FRAME_SHOOOTS,
                        "4"
                );
                //   setNumberPickerTextColor(npShoots, getColor(R.color.primary))
                setProgressFrame(4)
            } /*else if (valuesShoots[newVal].equals("8 Angles")) {
                Utilities.savePrefrence(
                        this@Camera2Activity,
                        AppConstants.FRAME_SHOOOTS,
                        "8"
                );
                //setNumberPickerTextColor(npShoots, getColor(R.color.primary))
                setProgressFrame(8)
            } else if (valuesShoots[newVal].equals("12 Angles")) {
                setProgressFrame(12)
                Utilities.savePrefrence(
                        this@Camera2Activity,
                        AppConstants.FRAME_SHOOOTS,
                        "12"
                );
            } else if (valuesShoots[newVal].equals("24 Angles")) {
                setProgressFrame(24)
                Utilities.savePrefrence(
                        this@Camera2Activity,
                        AppConstants.FRAME_SHOOOTS,
                        "24"
                );
//                setNumberPickerTextColor(npShoots, getColor(R.color.primary))

            }*/ /*else if (valuesShoots[newVal].equals("36 Angles")) {
                Utilities.savePrefrence(
                        this@Camera2Activity,
                        AppConstants.FRAME_SHOOOTS,
                        "36"
                );
                setNumberPickerTextColor(npShoots, getColor(R.color.primary))

                setProgressFrame(36)
            }*/

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
    @SuppressLint("SetTextI18n")
    fun showFootwearCustomSelectionDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_spinner)
        val window: Window = dialog.getWindow()!!
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT)

        val tvProceed : TextView = dialog.findViewById(R.id.tvProceed)
        val npShoots : NumberPicker = dialog.findViewById(R.id.npShoots)

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

    fun setNumberPickerTextColor(numberPicker: NumberPicker, color: Int) {
        try {
            val selectorWheelPaintField: Field = numberPicker.javaClass
                    .getDeclaredField("mSelectorWheelPaint")
            selectorWheelPaintField.setAccessible(true)
            (selectorWheelPaintField.get(numberPicker) as Paint)
                    .setColor(ContextCompat.getColor(this, R.color.primary))
        } catch (e: NoSuchFieldException) {
            Log.w("o", e)
        } catch (e: IllegalAccessException) {
            Log.w("p", e)
        } catch (e: IllegalArgumentException) {
            Log.w("q", e)
        }
        val count = numberPicker.childCount
        for (i in 0 until count) {
            val child = numberPicker.getChildAt(i)
            if (child is EditText) child.
            setTextColor(ContextCompat.getColor(this, R.color.black))
        }
        numberPicker.invalidate()
    }

    fun showExitDialog( ) {
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
            val intent = Intent(this, DashboardActivity::class.java)
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
        dialogButtonNo.setOnClickListener(View.OnClickListener { dialog.dismiss() })
        dialog.show()
    }

    fun showSpinner()
    {
        /*val spinner = findViewById<View>(R.id.pioedittxt5) as Spinner
        val adapter = ArrayAdapter.createFromResource(this,
                R.array.travelreasons, R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter*/
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun uploadImage() {
        Utilities.savePrefrence(this, AppConstants.REPLACED_IMAGE, "")
        //  Utilities.showProgressDialog(this)
        photoFile = File(
                Utilities.getPreference(
                        this,
                        AppConstants.IMAGE_FILE
                )
        )
        setImageRaw()

    }


}