package com.spyneai.activity

import SubcategoriesResponse
import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Bitmap.createScaledBitmap
import android.graphics.BitmapFactory.decodeFile
import android.graphics.Matrix
import android.media.AudioManager
import android.media.ExifInterface
import android.media.MediaActionSound
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Toast
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
import com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage
import com.iceteck.silicompressorr.SiliCompressor
import com.spyneai.BuildConfig
import com.spyneai.R
import com.spyneai.adapter.ProgressAdapter
import com.spyneai.adapter.SubCategoriesAdapter
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClient
import com.spyneai.model.shoot.*
import com.spyneai.model.skumap.UpdateSkuResponse
import com.spyneai.model.subcategories.Data
import com.spyneai.needs.AppConstants
import com.spyneai.needs.ImageFilePath
import com.spyneai.needs.ScalingUtilities
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_camera.*
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


class CameraActivity : AppCompatActivity() , SubCategoriesAdapter.BtnClickListener {
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
    var frameImage: String = ""


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        setSubCategories()
        fetchSubCategories()
        //  recyclerItemClick()

        setPermissions()
        setCameraActions()
        setSku()

        fetchIntents()
        setProgress()
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

        frameNumber = intent.getIntExtra(AppConstants.FRAME, 1)
        totalFrames = intent.getIntExtra(AppConstants.TOTAL_FRAME, 1)
        if(intent.getStringExtra(AppConstants.FRAME_IMAGE) != null)
            frameImage = intent.getStringExtra(AppConstants.FRAME_IMAGE)!!

        if (!etSkuName.text.toString().isEmpty()) {
            rvSubcategories.visibility = View.INVISIBLE
            //etSkuName.visibility = View.GONE
        }

        if (!frameImage.isEmpty()) {
            ivPreview.visibility = View.VISIBLE
            imgOverlay.visibility = View.VISIBLE
            Glide.with(this@CameraActivity).load(
                AppConstants.BASE_IMAGE_URL + frameImage
            ).into(ivPreview)

            Glide.with(this@CameraActivity).load(
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
                            this@CameraActivity,
                            AppConstants.SHOOT_ID
                        ).toString(), position
                    )
                    // pos = position
                    subCategoriesAdapter.notifyDataSetChanged()
                }
            })
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        rvSubcategories.setLayoutManager(layoutManager)
        rvSubcategories.setAdapter(subCategoriesAdapter)
    }

    private fun setProgress() {
        progressList = ArrayList<Data>()

        tvshoot.setText("Angle " + (frameNumber - 1) + "/" + totalFrames)
        val framesList : List<Int> = ArrayList(totalFrames)

        progressAdapter = ProgressAdapter(this, framesList as ArrayList<Int>)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        rvProgress.setLayoutManager(layoutManager)
        rvProgress.setAdapter(progressAdapter)

        for (i in 1..frameNumber-1)
            (framesList as ArrayList).add(0)
        for (i in frameNumber-1..totalFrames-1)
            (framesList as ArrayList).add(1)

        progressAdapter.notifyDataSetChanged()
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
                    }
                    subCategoriesAdapter.notifyDataSetChanged()

                    if (Utilities.getPreference(this@CameraActivity, AppConstants.FROM)
                            .equals("BA")
                    )
                        setProductMap(
                            Utilities.getPreference(
                                this@CameraActivity,
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
        imgBack.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })

        ivPreview.setOnClickListener(View.OnClickListener {
            if (frameNumber == 1) {
                if (imgOverlay.visibility == View.INVISIBLE) {
                    imgOverlay.visibility = View.VISIBLE
                    rvSubcategories.visibility = View.INVISIBLE
                } else {
                    imgOverlay.visibility = View.INVISIBLE
                    rvSubcategories.visibility = View.VISIBLE
                }
            } else {
                rvSubcategories.visibility = View.INVISIBLE
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
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        // Set up the listener for take photo button
        camera_capture_button.setOnClickListener(View.OnClickListener {
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
            val fadeOutAnimation = AlphaAnimation(1.0f, 0.0f).apply {
                duration = 250
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
                duration = 20
                setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(animation: Animation?) {
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        camera_overlay.alpha = 1.0f
                        Handler(Looper.getMainLooper()).postDelayed({
                            camera_overlay.startAnimation(fadeOutAnimation)
                        }, 20)
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
            val checkSelfPermission = ContextCompat.checkSelfPermission(
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
            }
        })


        cameraExecutor = Executors.newSingleThreadExecutor()
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

    @SuppressLint("RestrictedApi")
    private fun  startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            cameraProvider = cameraProviderFuture.get()

            // Preview
             val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setFlashMode(flashMode).build()


            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
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
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                @SuppressLint("RestrictedApi")
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    savedUri = Uri.fromFile(photoFile)

                   //   photoFile = getCompresedFile(savedUri)

                   // val filePathString : String = decodeFile(photoFile.toString())!!

                    //Open camera preview
                    Utilities.savePrefrence(
                        this@CameraActivity,
                        AppConstants.IMAGE_FILE,
                            photoFile.toString()
                    )

/*
                    Utilities.savePrefrence(
                        this@CameraActivity,
                        AppConstants.RAW_IMAGE_FILE,
                        photoFile.toString()
                    )
*/

                    Utilities.savePrefrence(
                        this@CameraActivity,
                        AppConstants.CATEGORY_NAME,
                        catName
                    )

                    val intent = Intent(this@CameraActivity, CameraPreviewActivity::class.java)

                    /* intent.putExtra(AppConstants.SKU_NAME, skuName)
                        intent.putExtra(AppConstants.SKU_ID, skuId)
                        intent.putExtra(AppConstants.SHOOT_ID, shootIds)
                        intent.putExtra(AppConstants.CATEGORY_ID, catIds)
                        intent.putExtra(AppConstants.SUB_CAT_ID, prodIds)*/

                    Utilities.savePrefrence(this@CameraActivity, AppConstants.FROM, "AB")

                    intent.putExtra(AppConstants.FRAME, frameNumber)
                    intent.putExtra(AppConstants.TOTAL_FRAME, totalFrames)


                    startActivity(intent)
                    //finish()

                    val msg = "Photo capture succeeded: $savedUri"
                    // Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
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

                    if (response.body()!!.payload.data.frames != null)
                        totalFrames = response.body()!!.payload.data.frames.totalFrames

                    Utilities.savePrefrence(this@CameraActivity, AppConstants.SHOOT_ID, shootIds)
                    Utilities.savePrefrence(this@CameraActivity, AppConstants.CATEGORY_ID, catIds)
                    Utilities.savePrefrence(this@CameraActivity, AppConstants.PRODUCT_ID, prodIds)
                    Utilities.savePrefrence(this@CameraActivity, AppConstants.SKU_NAME, skuName)
                    Utilities.savePrefrence(this@CameraActivity, AppConstants.SKU_ID, skuId)

                    setProgress()

                    if (Utilities.getPreference(this@CameraActivity, AppConstants.FROM)
                            .equals("BA")
                    ) {
                        rvSubcategories.visibility = View.VISIBLE
                    } else
                        rvSubcategories.visibility = View.INVISIBLE

                    // etSkuName.visibility = View.GONE

                    if (response.body()!!.payload.data.frames != null) {
                        Glide.with(this@CameraActivity).load(
                            AppConstants.BASE_IMAGE_URL +
                                    response.body()!!.payload.data.frames.frameImages[0].displayImage
                        ).into(ivPreview)

                        Glide.with(this@CameraActivity).load(
                            AppConstants.BASE_IMAGE_URL +
                                    response.body()!!.payload.data.frames.frameImages[0].displayImage
                        ).into(imgOverlay)
                        ivPreview.visibility = View.VISIBLE
                        imgOverlay.visibility = View.VISIBLE

                    }

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

    public fun getInstance(): CameraActivity {
        return this
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Utilities.savePrefrence(this@CameraActivity, AppConstants.SHOOT_ID, "")
        Utilities.savePrefrence(this@CameraActivity, AppConstants.CATEGORY_ID, "")
        Utilities.savePrefrence(this@CameraActivity, AppConstants.PRODUCT_ID, "")
        Utilities.savePrefrence(this@CameraActivity, AppConstants.SKU_NAME, "")
        Utilities.savePrefrence(this@CameraActivity, AppConstants.SKU_ID, "")
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
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
                    this@CameraActivity,
                    AppConstants.IMAGE_FILE,
                    photoFile.toString()
                )

                val intent = Intent(this@CameraActivity, CameraPreviewActivity::class.java)
                Utilities.savePrefrence(this@CameraActivity, AppConstants.FROM, "AB")
                intent.putExtra(AppConstants.FRAME, frameNumber)
                intent.putExtra(AppConstants.TOTAL_FRAME, totalFrames)
                startActivity(intent)
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
                ExifInterface.ORIENTATION_ROTATE_180 -> rotatedBitmap = rotateImage(tempBitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotatedBitmap = rotateImage(tempBitmap, 270f)
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
            imageFile = File(filesDir, "photo" + ".jpg")
            val os: OutputStream
            try {
                os = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 10, os)
                os.flush()
                os.close()
            } catch (e: Exception) {
                Log.e(javaClass.simpleName, "Error writing bitmap", e)
            }
        }
        return imageFile
    }
}