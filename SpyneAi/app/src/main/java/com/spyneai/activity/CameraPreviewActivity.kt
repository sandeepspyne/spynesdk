package com.spyneai.activity

import UploadPhotoResponse
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import com.google.gson.Gson
import com.spyneai.R
import com.spyneai.adapter.FiltersAdapters
import com.spyneai.adapter.ProgressAdapter
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClient
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.model.carreplace.AddCarLogoResponse
import com.spyneai.model.subcategories.Data
import com.spyneai.model.upload.PreviewResponse
import com.spyneai.model.upload.UploadResponse
import com.spyneai.model.uploadRough.UploadPhotoRequest
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_camera.*
import kotlinx.android.synthetic.main.activity_camera_preview.*
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


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
public class CameraPreviewActivity : AppCompatActivity() {
    private lateinit var rawphotoFile: File
    public var imageFile: File? = null
    public var myBitmap: Bitmap? = null
    public lateinit var outputDirectory: File
    public lateinit var photoFile : File
    //public lateinit var imgUrls : String
    public var replacedUrl : String = ""


    private lateinit var progressList: List<Data>
    private lateinit var progressAdapter: ProgressAdapter

    var frameNumber : Int = 1
    var totalFrames: Int = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_preview)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        Utilities.savePrefrence(this@CameraPreviewActivity,
            AppConstants.MAIN_IMAGE, "")

        getImageFromFile()
        setTabs()
       // setImage()
        setPreview()
        uploadImage()
        setProgress()

    }
    private fun setProgress() {
        progressList = ArrayList<Data>()

        frameNumber = intent.getIntExtra(AppConstants.FRAME, 1)
        totalFrames = intent.getIntExtra(AppConstants.TOTAL_FRAME, 1)

        tvshoots.setText("Shoot " + frameNumber + "/" + totalFrames)
        val framesList : List<Int> = ArrayList(totalFrames)

        progressAdapter = ProgressAdapter(this, framesList as ArrayList<Int>)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvProgresss.setLayoutManager(layoutManager)
        rvProgresss.setAdapter(progressAdapter)

        for (i in 0..frameNumber-1)
            (framesList as ArrayList).add(0)
        for (i in frameNumber..totalFrames-1)
            (framesList as ArrayList).add(1)

        progressAdapter.notifyDataSetChanged()
    }

/*
    public fun setImage() {
*/
/*
        Utilities.savePrefrence(
            this,
            AppConstants.IMAGE_FILE,
            intent.getStringExtra(AppConstants.RAW_IMAGE_FILE)!!
        )
*//*


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

        imgPreview.setImageBitmap(rotatedBitmap)
    }
*/



    //Get image from file directory
    public fun getImageFromFile() {
        btnBlurOk.startShimmer()

        outputDirectory = getOutputDirectory()
        photoFile = File(Utilities.getPreference(this, AppConstants.IMAGE_FILE))
        rawphotoFile = File(Utilities.getPreference(this, AppConstants.RAW_IMAGE_FILE))

        myBitmap = BitmapFactory.decodeFile(photoFile.toString())
        // imgPreview.setImageBitmap(myBitmap)
    }

    @JvmName("getOutputDirectory1")
    public fun getOutputDirectory(): File {
        val mediaDir = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            externalMediaDirs.firstOrNull()?.let {
                File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }
        } else {
            TODO("VERSION.SDK_INT < LOLLIPOP")
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    //set tabs for channels and backgrounds
    public fun setTabs() {
        if (Utilities.getPreference(this,AppConstants.CATEGORY_NAME)!!.equals("Automobiles")) {
            tabLayout.addTab(tabLayout.newTab().setText(R.string.backgrounds))
            tabLayout.addTab(tabLayout.newTab().setText(R.string.dealership))
        }
        else{
            tabLayout.addTab(tabLayout.newTab().setText(R.string.channels))
            tabLayout.addTab(tabLayout.newTab().setText(R.string.backgrounds))
        }

        val viewPagerAdapter = FiltersAdapters(
            applicationContext,
            supportFragmentManager,
            tabLayout.getTabCount(),
            Utilities.getPreference(this, AppConstants.CATEGORY_ID)!!,
            Utilities.getPreference(this, AppConstants.PRODUCT_ID)!!,
            Utilities.getPreference(this, AppConstants.CATEGORY_NAME)!!
        )

        viewpager.adapter = viewPagerAdapter
        viewpager.addOnPageChangeListener(TabLayoutOnPageChangeListener(tabLayout))

        tabLayout.setOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewpager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

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

        imgPreview.setImageBitmap(rotatedBitmap)
        imageFile = persistImage(rotatedBitmap!!)
    }

    //Preview Set for clicked camera image
    fun setPreview() {
        Utilities.savePrefrence(this,AppConstants.REPLACED_IMAGE,"")
        //  Utilities.showProgressDialog(this)
        setImageRaw()

        val request = RetrofitClients.buildService(APiService::class.java)

        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), imageFile)
        val body = MultipartBody.Part.createFormData("image", imageFile!!.name, requestFile)
        var descriptionString = "true"

        if (Utilities.getPreference(this,AppConstants.CATEGORY_NAME)!!.equals("Footwear"))
            descriptionString = "true"
        else
            descriptionString = "false"

        val optimization = RequestBody.create(MultipartBody.FORM, descriptionString)

        val call = request.uploadPhoto(body, optimization)

        call?.enqueue(object : Callback<UploadResponse> {
            override fun onResponse(
                call: Call<UploadResponse>,
                response: Response<UploadResponse>) {
                //  Utilities.hideProgressDialog()
                if (response.isSuccessful) {
                    Log.e("Respo Image ", response.body()?.image.toString())

                    if (Utilities.getPreference(this@CameraPreviewActivity, AppConstants.MAIN_IMAGE).equals(""))
                        Utilities.savePrefrence(this@CameraPreviewActivity,
                            AppConstants.MAIN_IMAGE,
                            response.body()?.image.toString())

                    btnOk.visibility = View.VISIBLE
                    btnBlurOk.visibility = View.GONE

/*
                    Toast.makeText(this@CameraPreviewActivity,
                        "Previews Ready for use",
                        Toast.LENGTH_SHORT).show()
*/

                }
            }

            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                //   Utilities.hideProgressDialog()
                Log.e("Respo Image ", "Image error")
                Toast.makeText(this@CameraPreviewActivity,
                        "Please take the last image again",
                        Toast.LENGTH_SHORT).show()
            }
        })
    }

    //Footwear preview
    fun showPreview() {
        Utilities.showProgressDialog(this)
        setImageRaw()

        val request = RetrofitClients.buildService(APiService::class.java)
        val img_url = RequestBody.create(MultipartBody.FORM,
            Utilities.getPreference(this@CameraPreviewActivity, AppConstants.MAIN_IMAGE))

        val call = request.previewPhoto(img_url)
        //val imgPreview = findViewById<ImageView>(R.id.imgPreview)
        call?.enqueue(object : Callback<PreviewResponse> {
            override fun onResponse(
                call: Call<PreviewResponse>,
                response: Response<PreviewResponse>) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful) {
                    if (response.body() != null)
                        Glide.with(this@CameraPreviewActivity)
                            .load(response.body()!!.url)
                            .into(imgPreview)
                    Log.e("Respo Image Preview ", response.body().toString())
                }
            }

            override fun onFailure(call: Call<PreviewResponse>, t: Throwable) {
                Utilities.hideProgressDialog()
                Toast.makeText(this@CameraPreviewActivity,
                    "Some issue in image...",
                    Toast.LENGTH_SHORT).show()
                Log.e("Respo Image ", "Image error")
            }
        })
    }

    // --  REMOVE BACKGROUNDS FOR CAR PREVIEW --
    fun showPreviewCar() {
        Utilities.showProgressDialogPreview(this)
        setImageRaw()

        val img_url = RequestBody.create(MultipartBody.FORM,
            Utilities.getPreference(this@CameraPreviewActivity, AppConstants.MAIN_IMAGE))

        val bg_replacement_backgound_id = RequestBody.create(MultipartBody.FORM, Utilities.getPreference(this,AppConstants.IMAGE_ID)!!)

        Log.e("bg ID ",Utilities.getPreference(this,AppConstants.IMAGE_ID)!!.toString())
        var front : String = "front"
        val car_bg_replacement_angle = RequestBody.create(MultipartBody.FORM, front  )

        val request = RetrofitClients.buildService(APiService::class.java)

        val call = request.previewPhotoCar( bg_replacement_backgound_id,img_url,car_bg_replacement_angle)
        //val imgPreview = findViewById<ImageView>(R.id.imgPreview)
         call?.enqueue(object : Callback<PreviewResponse> {
            override fun onResponse(
                call: Call<PreviewResponse>,
                response: Response<PreviewResponse>) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        Glide.with(this@CameraPreviewActivity)
                            .load(response.body()!!.url)
                            .into(imgPreview)

                        Utilities.savePrefrence(this@CameraPreviewActivity,AppConstants.REPLACED_IMAGE,response.body()!!.url )
                    }
                    Log.e("Respo Image Preview ", response.body().toString())
                }
            }

            override fun onFailure(call: Call<PreviewResponse>, t: Throwable) {
                Utilities.hideProgressDialog()
                Toast.makeText(this@CameraPreviewActivity,
                    "Some issue in image...",
                    Toast.LENGTH_SHORT).show()

                Log.e("Respo Image ", "Image error")
            }
        })

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

    private fun uploadImage() {

        btnBlurOk.setOnClickListener(View.OnClickListener {
            Toast.makeText(
                    this@CameraPreviewActivity,
                    "Please wait while we are processing your image...",
                    Toast.LENGTH_SHORT
            ).show()

        })
        btnOk.setOnClickListener(View.OnClickListener {
            Utilities.showProgressDialog(this)
            setImageRaw()

            val request = RetrofitClient.buildService(APiService::class.java)

            val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), imageFile)
            val body = MultipartBody.Part.createFormData("photo", imageFile!!.name, requestFile)

            val uploadPhotoName : String =
                Utilities.getPreference(this@CameraPreviewActivity, AppConstants.MAIN_IMAGE)
                    .toString().split("/")[Utilities.getPreference(this@CameraPreviewActivity,
                    AppConstants.MAIN_IMAGE)
                    .toString().split("/").size - 1]


            val uploadPhotoRequest = UploadPhotoRequest(
                Utilities.getPreference(this, AppConstants.SKU_NAME)!!,
                Utilities.getPreference(this, AppConstants.SKU_ID)!!,
                "raw",
                intent.getIntExtra(AppConstants.FRAME, 1),
                Utilities.getPreference(this, AppConstants.SHOOT_ID)!!,
                Utilities.getPreference(this@CameraPreviewActivity, AppConstants.MAIN_IMAGE).toString(),
                uploadPhotoName, "EXTERIOR")

            Log.e("Frame Number", intent.getIntExtra(AppConstants.FRAME, 1).toString())
            val gson = Gson()
            //    val personString = gson.toJson(uploadPhotoRequest)
            //  val skuName = RequestBody.create(MediaType.parse("application/json"), personString)

            val call = request.uploadPhotoRough(
                Utilities.getPreference(this, AppConstants.tokenId), uploadPhotoRequest)

            call?.enqueue(object : Callback<UploadPhotoResponse> {
                override fun onResponse(
                    call: Call<UploadPhotoResponse>,
                    response: Response<UploadPhotoResponse>) {
                    Utilities.hideProgressDialog()
                    if (response.isSuccessful) {
                        if (response.body()?.payload!!.data.currentFrame <=
                            response.body()?.payload!!.data.totalFrames) {

                            val intent = Intent(this@CameraPreviewActivity, CameraActivity::class.java)
                            intent.putExtra(AppConstants.FRAME, response.body()?.payload!!.data.currentFrame)
                            intent.putExtra(AppConstants.TOTAL_FRAME, response.body()?.payload!!.data.totalFrames)
                            intent.putExtra(AppConstants.FRAME_IMAGE, response.body()?.payload!!.data.frame.displayImage)
                            startActivity(intent)

                            Log.e("Success", response.body().toString())

                            finish()
                        } else {
                            val intent = Intent(this@CameraPreviewActivity, OrderActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        finish()
                        Log.e("IMage uploaded ", response.body()?.msgInfo.toString())
                        Log.e("Frame 1", response.body()?.payload!!.data.currentFrame.toString())
                        Log.e("Frame 2", response.body()?.payload!!.data.totalFrames.toString())
                        Log.e("SKU ID", Utilities.getPreference(this@CameraPreviewActivity,
                            AppConstants.SKU_ID).toString())
                    }
                }

                override fun onFailure(call: Call<UploadPhotoResponse>, t: Throwable) {
                    Utilities.hideProgressDialog()
                    Log.e("Respo Image ", "Image error")
                }
            })
        })

        btnCancel.setOnClickListener(View.OnClickListener {
            finish()
        })

        imgBackPreview.setOnClickListener(View.OnClickListener {
            finish()
        })
    }

    public fun changeLogoPosition(positionLogo : Int)
    {
        val myBitmapLogo = BitmapFactory.decodeFile(
            Utilities.getPreference(this, AppConstants.LOGO_FILE))

        ivLogoPlace1.setImageBitmap(myBitmapLogo)
        ivLogoPlace2.setImageBitmap(myBitmapLogo)
        ivLogoPlace3.setImageBitmap(myBitmapLogo)
        ivLogoPlace4.setImageBitmap(myBitmapLogo)

        if (positionLogo == 0) {
            ivLogoPlace1.visibility = View.GONE
            ivLogoPlace2.visibility = View.GONE
            ivLogoPlace3.visibility = View.GONE
            ivLogoPlace4.visibility = View.GONE
            showPreviewCarLogo("leftTop")
        }
        else if (positionLogo == 1) {
            ivLogoPlace1.visibility = View.GONE
            ivLogoPlace2.visibility = View.GONE
            ivLogoPlace3.visibility = View.GONE
            ivLogoPlace4.visibility = View.GONE
            showPreviewCarLogo("rightTop")
        }
        else if (positionLogo == 2) {
            ivLogoPlace1.visibility = View.GONE
            ivLogoPlace2.visibility = View.GONE
            ivLogoPlace3.visibility = View.GONE
            ivLogoPlace4.visibility = View.GONE
            showPreviewCarLogo("rightBottom")
        }
        else if (positionLogo == 3) {
            ivLogoPlace1.visibility = View.GONE
            ivLogoPlace2.visibility = View.GONE
            ivLogoPlace3.visibility = View.GONE
            ivLogoPlace4.visibility = View.GONE
            showPreviewCarLogo("leftBottom")
        }
    }


    fun showPreviewCarLogo(rotatePosition : String) {
        if (!Utilities.getPreference(this,AppConstants.REPLACED_IMAGE)!!.isEmpty()) {
            Utilities.showProgressDialogPreview(this)
            val myBitmap = BitmapFactory.decodeFile(File(Utilities.getPreference(this, AppConstants.LOGO_FILE)).toString())

            var ei: ExifInterface? = null
            try {
                ei = ExifInterface(File(Utilities.getPreference(this, AppConstants.LOGO_FILE)).toString()!!)
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
                ExifInterface.ORIENTATION_ROTATE_180 -> rotatedBitmap =
                    rotateImage(myBitmap!!, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotatedBitmap =
                    rotateImage(myBitmap!!, 270f)
                ExifInterface.ORIENTATION_NORMAL -> rotatedBitmap = myBitmap
                else -> rotatedBitmap = myBitmap
            }

            val imageFiles = persistImage(rotatedBitmap!!)

            val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), imageFiles)
            val body = MultipartBody.Part.createFormData("logo", imageFiles!!.name, requestFile)
            val logo_position = RequestBody.create(MultipartBody.FORM, rotatePosition)
            val img_url = RequestBody.create(MultipartBody.FORM, Utilities.getPreference(this,AppConstants.REPLACED_IMAGE)!!)

            val request = RetrofitClients.buildService(APiService::class.java)

            val call = request.previewPhotoCarLogo(body, logo_position, img_url)

            call?.enqueue(object : Callback<AddCarLogoResponse> {
                override fun onResponse(
                    call: Call<AddCarLogoResponse>,
                    response: Response<AddCarLogoResponse>
                ) {
                    Utilities.hideProgressDialog()
                    if (response.isSuccessful) {
                        if (response.body() != null)
                            Glide.with(this@CameraPreviewActivity)
                                .load(response.body()!!.org_url)
                                .into(imgPreview)
                        else
                            Toast.makeText(
                                this@CameraPreviewActivity,
                                "Logo addition is not available right now",
                                Toast.LENGTH_SHORT
                            ).show()

                        Log.e("Respo Image Logo ", response.body().toString())
                    }
                }

                override fun onFailure(call: Call<AddCarLogoResponse>, t: Throwable) {
                    Utilities.hideProgressDialog()
                    Toast.makeText(
                        this@CameraPreviewActivity,
                        "Some issue in image...",
                        Toast.LENGTH_SHORT
                    ).show()

                    Log.e("Respo Image ", "Image error")
                }
            })

        }
        else{
            Toast.makeText(
                this@CameraPreviewActivity,
                "Please choose your car background first",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

}