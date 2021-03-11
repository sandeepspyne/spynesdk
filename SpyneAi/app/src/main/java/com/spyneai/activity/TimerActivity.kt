package com.spyneai.activity

import UploadPhotoResponse
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Bundle
import android.os.CountDownTimer
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
import com.spyneai.adapter.CarBackgroundAdapter
import com.spyneai.adapter.PhotosAdapter
import com.spyneai.aipack.*
import com.spyneai.interfaces.*
import com.spyneai.model.ai.SendEmailRequest
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


class TimerActivity : AppCompatActivity() {
    val progress = 1000
    var maxProgress = 120000
    var i = 0
    lateinit var countDownTimer : CountDownTimer

    private lateinit var photsAdapter: PhotosAdapter
    private lateinit var photoList: List<Photos>

    lateinit var imageList : ArrayList<String>
    lateinit var imageListAfter : ArrayList<String>
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)
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

        setCustomTimer()
        try {
            llTimer.visibility = View.VISIBLE
            llNoInternet.visibility = View.GONE
            uploadImageToBucket()
        }
        catch ( e : Exception)
        {
            llTimer.visibility = View.GONE
            llNoInternet.visibility = View.VISIBLE
            countDownTimer.cancel()
            e.printStackTrace()
            Log.e("Catched ",e.printStackTrace().toString())
        }

        tvRetry.setOnClickListener(View.OnClickListener {
            setCustomTimer()
            try {
                llTimer.visibility = View.VISIBLE
                llNoInternet.visibility = View.GONE
                uploadImageToBucket()
            }
            catch ( e : Exception)
            {
                llTimer.visibility = View.GONE
                llNoInternet.visibility = View.VISIBLE
                countDownTimer.cancel()
                e.printStackTrace()
                Log.e("Catched ",e.printStackTrace().toString())
            }
        })
    }

/*
    private fun listeners() {
        cbSend.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                if (isChecked)
                    sendEmail()
                else
                    moveNext()
            }
        })
    }
*/


    //Upload image to bucket
    fun uploadImageToBucket() {
        if (Utilities.isNetworkAvailable(this)) {
            llTimer.visibility = View.VISIBLE
            llNoInternet.visibility = View.GONE

            Log.e("Fisrt execution", "UploadImage Bucket")
            //Get All Data to be uploaded

            val imageFile = setImageRaw(imageFileList[totalImagesToUPloadIndex])

            val request = RetrofitClients.buildService(APiService::class.java)
            val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), imageFile)
            val body = MultipartBody.Part.createFormData("image", imageFile!!.name, requestFile)
            val descriptionString = "false"

            val optimization = RequestBody.create(MultipartBody.FORM, descriptionString)
            val call = request.uploadPhoto(body, optimization)

            call?.enqueue(object : Callback<UploadResponse> {
                override fun onResponse(
                    call: Call<UploadResponse>,
                    response: Response<UploadResponse>
                ) {
                    //  Utilities.hideProgressDialog()
                    if (response.isSuccessful) {
                        Log.e("Fisrt execution", "UploadImage Bucket")


                        Log.e("uploadImageToBucket", response.body()?.image.toString())
                        Log.e("uploadImageToBucket", totalImagesToUPloadIndex.toString())

                        //  if (Utilities.getPreference(this@CameraActivity, AppConstants.MAIN_IMAGE).equals(""))
                        Utilities.savePrefrence(
                            this@TimerActivity,
                            AppConstants.MAIN_IMAGE,
                            response.body()?.image.toString()
                        )
                        uploadImageURLs()
                    }
                    else{
                        llTimer.visibility = View.GONE
                        llNoInternet.visibility = View.VISIBLE
                        countDownTimer.cancel()
                    }
                }

                override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                    //   Utilities.hideProgressDialog()
                    Log.e("Respo Image ", "Image error")
                    Toast.makeText(
                        this@TimerActivity,
                        "Server not responding",
                        Toast.LENGTH_SHORT
                    ).show()
                    llTimer.visibility = View.GONE
                    llNoInternet.visibility = View.VISIBLE
                    countDownTimer.cancel()
                }
            })
        }
        else{
            Utilities.hideProgressDialog()
            llTimer.visibility = View.GONE
            llNoInternet.visibility = View.VISIBLE
            countDownTimer.cancel()
        }
    }

    fun uploadImageURLs()
    {
        if (Utilities.isNetworkAvailable(this)) {
            llTimer.visibility = View.VISIBLE
            llNoInternet.visibility = View.GONE
            val request = RetrofitClient.buildService(APiService::class.java)
            val uploadPhotoName : String =
                Utilities.getPreference(this, AppConstants.MAIN_IMAGE)
                    .toString().split("/")[Utilities.getPreference(
                    this,
                    AppConstants.MAIN_IMAGE
                ).toString().split("/").size - 1]

            val uploadPhotoRequest = UploadPhotoRequest(
                Utilities.getPreference(this, AppConstants.SKU_NAME)!!,
                Utilities.getPreference(this, AppConstants.SKU_ID)!!,
                "raw",
                imageFileListFrames[totalImagesToUPloadIndex],
                Utilities.getPreference(this, AppConstants.SHOOT_ID)!!,
                Utilities.getPreference(this, AppConstants.MAIN_IMAGE).toString(),
                uploadPhotoName
            )

            Log.e("Frame Number", intent.getIntExtra(AppConstants.FRAME, 1).toString())
            val gson = Gson()
            //    val personString = gson.toJson(uploadPhotoRequest)
            //  val skuName = RequestBody.create(MediaType.parse("application/json"), personString)

            val call = request.uploadPhotoRough(
                Utilities.getPreference(this, AppConstants.tokenId), uploadPhotoRequest
            )

            call?.enqueue(object : Callback<UploadPhotoResponse> {
                override fun onResponse(
                    call: Call<UploadPhotoResponse>,
                    response: Response<UploadPhotoResponse>
                ) {
                    if (response.isSuccessful) {
                        if (totalImagesToUPloadIndex < totalImagesToUPload - 1) {
                            totalImagesToUPloadIndex = totalImagesToUPloadIndex + 1
                            uploadImageToBucket()
                        } else {
                            markSkuComplete()
                        }
                        Log.e("uploadImageURLs", totalImagesToUPloadIndex.toString())

                        Log.e("IMage uploaded ", response.body()?.msgInfo.toString())
                        Log.e("Frame 1", response.body()?.payload!!.data.currentFrame.toString())
                        Log.e("Frame 2", response.body()?.payload!!.data.totalFrames.toString())
                        Log.e(
                            "SKU ID", Utilities.getPreference(
                                this@TimerActivity,
                                AppConstants.SKU_ID
                            ).toString()
                        )
                    } else {
                        llTimer.visibility = View.GONE
                        llNoInternet.visibility = View.VISIBLE
                        countDownTimer.cancel()
                        Toast.makeText(
                            this@TimerActivity,
                            "Server not responding",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<UploadPhotoResponse>, t: Throwable) {
                    Utilities.hideProgressDialog()
                    Toast.makeText(
                        this@TimerActivity,
                        "Server not responding",
                        Toast.LENGTH_SHORT
                    ).show()
                    llTimer.visibility = View.GONE
                    llNoInternet.visibility = View.VISIBLE
                    countDownTimer.cancel()
                    Log.e("Respo Image ", "Image error")
                }
            })
        }
        else{
            Utilities.hideProgressDialog()
            llTimer.visibility = View.GONE
            llNoInternet.visibility = View.VISIBLE
            countDownTimer.cancel()
        }
    }

    fun setImageRaw(photoFile: File): File? {
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

        return persistImage(rotatedBitmap!!)
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

    //MArk the SKu as complete
    private fun markSkuComplete() {
        if (Utilities.isNetworkAvailable(this)) {
            val request = RetrofitClient.buildService(APiService::class.java)

            val updateSkuStatusRequest = UpdateSkuStatusRequest(
                Utilities.getPreference(this, AppConstants.SKU_ID)!!,
                Utilities.getPreference(this, AppConstants.SHOOT_ID)!!,
                true
            )
            val call = request.updateSkuStauts(
                Utilities.getPreference(this, AppConstants.tokenId),
                updateSkuStatusRequest
            )

            call?.enqueue(object : Callback<UpdateSkuStatusResponse> {
                override fun onResponse(
                    call: Call<UpdateSkuStatusResponse>,
                    response: Response<UpdateSkuStatusResponse>
                ) {
                    Utilities.hideProgressDialog()
                    if (response.isSuccessful) {
                        Log.e("Sku completed", "MArked Complete")
                        setSkuImages()
                    }
                }

                override fun onFailure(call: Call<UpdateSkuStatusResponse>, t: Throwable) {
                    setSkuImages()
                    Utilities.hideProgressDialog()
                }
            })
        }
        else{
            Utilities.hideProgressDialog()
            llTimer.visibility = View.GONE
            llNoInternet.visibility = View.VISIBLE
            countDownTimer.cancel()
        }
    }

    private fun setCustomTimer() {
        if (Utilities.getPreference(this, AppConstants.FRAME_SHOOOTS) != null)
        {
            if (Utilities.getPreference(this, AppConstants.FRAME_SHOOOTS).equals("4"))
            {
                CountDownTimer(420000)
            }
            else if (Utilities.getPreference(this, AppConstants.FRAME_SHOOOTS).equals("8"))
            {
                CountDownTimer(420000 * 2)
            }
            else if (Utilities.getPreference(this, AppConstants.FRAME_SHOOOTS).equals("12"))
            {
                CountDownTimer(420000 * 3)
            }
            else if (Utilities.getPreference(this, AppConstants.FRAME_SHOOOTS).equals("24"))
            {
                CountDownTimer(420000 * 4)

            }   else if (Utilities.getPreference(this, AppConstants.FRAME_SHOOOTS).equals("36"))
            {
                CountDownTimer(390000 * 3)
            }
        }
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
            }
        }.start()
    }

    private fun setSkuImages() {
        photoList = ArrayList<Photos>()
        photsAdapter = PhotosAdapter(this, photoList,
            object : PhotosAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    Log.e("position preview", position.toString())
                }
            })

        val layoutManager: RecyclerView.LayoutManager =
            GridLayoutManager(this, 2)
        rvSkuDemo.setLayoutManager(layoutManager)
        rvSkuDemo.setAdapter(photsAdapter)
        fetchSkuData()
    }

    private fun fetchSkuData() {
        if (Utilities.isNetworkAvailable(this)) {
            llTimer.visibility = View.VISIBLE
            llNoInternet.visibility = View.GONE
            val request = RetrofitClient.buildService(APiService::class.java)

            val call = request.getSkuDetails(
                Utilities.getPreference(this, AppConstants.tokenId),
                Utilities.getPreference(this, AppConstants.SKU_ID).toString()
            )

            call?.enqueue(object : Callback<SkuResponse> {
                override fun onResponse(
                    call: Call<SkuResponse>,
                    response: Response<SkuResponse>
                ) {
                    Utilities.hideProgressDialog()
                    if (response.isSuccessful) {
                        if (response.body()?.payload != null) {
                            if (response.body()?.payload!!.data.photos.size > 0) {
                                (photoList as ArrayList).clear()
                                (photoList as ArrayList).addAll(response.body()?.payload!!.data.photos as ArrayList)
                            }
                        }
                        photsAdapter.notifyDataSetChanged()
                        // Utilities.showProgressDialog(this@TimerActivity)
                        if (countGif < photoList.size - 1) {
                            bulkUpload(countGif)
                        }
                    }
                    else{
                        Toast.makeText(
                            this@TimerActivity,
                            "Server not responding!!!", Toast.LENGTH_SHORT
                        ).show()
                        llTimer.visibility = View.VISIBLE
                        llNoInternet.visibility = View.GONE
                    }
                }

                override fun onFailure(call: Call<SkuResponse>, t: Throwable) {
                    Utilities.hideProgressDialog()
                    llTimer.visibility = View.VISIBLE
                    llNoInternet.visibility = View.GONE
                    Toast.makeText(
                        this@TimerActivity,
                        "Server not responding!!!", Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
        else{
            Utilities.hideProgressDialog()

            llTimer.visibility = View.GONE
            llNoInternet.visibility = View.VISIBLE
            countDownTimer.cancel()
        }
    }

    //Upload bulk data
    private fun bulkUpload(countsGif: Int) {
        if (Utilities.isNetworkAvailable(this)) {
            llTimer.visibility = View.VISIBLE
            llNoInternet.visibility = View.GONE

            val request = RetrofitClients.buildService(APiService::class.java)
            Log.e("Upload bulk", "started......")

            /*   val background : ArrayList<String> = ArrayList<String>()
           background.addAll(listOf(backgroundSelect))*/

//        image_url.add(photoList[countsGif].displayThumbnail)

            val background = RequestBody.create(
                MultipartBody.FORM,
                backgroundSelect
            )

            val userId = RequestBody.create(
                MultipartBody.FORM,
                Utilities.getPreference(this, AppConstants.tokenId)!!
            )
            val skuId = RequestBody.create(
                MultipartBody.FORM,
                Utilities.getPreference(this, AppConstants.SKU_ID)!!
            )
            val imageUrl = RequestBody.create(
                MultipartBody.FORM,
                photoList[countsGif].displayThumbnail
            )
            Log.e(
                "Sku NAme Upload",
                Utilities.getPreference(this, AppConstants.SKU_NAME)!!
            )
            val skuName = RequestBody.create(
                MultipartBody.FORM,
                Utilities.getPreference(this, AppConstants.SKU_NAME)!!
            )

            val windowStatus = RequestBody.create(
                MultipartBody.FORM,
                "inner"
            )

            val call =
                request.bulkUPload(background, userId, skuId, imageUrl, skuName, windowStatus)

            call?.enqueue(object : Callback<BulkUploadResponse> {
                override fun onResponse(
                    call: Call<BulkUploadResponse>,
                    response: Response<BulkUploadResponse>
                ) {
                    if (response.isSuccessful && response.body()!!.status == 200) {
                        ++countGif
                        if (countGif < photoList.size) {
                            Log.e("countGif", countGif.toString())
                            bulkUpload(countGif)
                        } else
                            fetchBulkUpload()
                        Log.e("Upload Replace", "bulk")
                        Log.e(
                            "Upload Replace SKU",
                            Utilities.getPreference(
                                this@TimerActivity,
                                AppConstants.SKU_NAME
                            )!!
                        )
                    } else {
                        llTimer.visibility = View.GONE
                        llNoInternet.visibility = View.VISIBLE
                        countDownTimer.cancel()
                        Utilities.hideProgressDialog()
                        Toast.makeText(
                            this@TimerActivity,
                            "Server not responding!!!", Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<BulkUploadResponse>, t: Throwable) {
                    Utilities.hideProgressDialog()
                    llTimer.visibility = View.GONE
                    llNoInternet.visibility = View.VISIBLE
                    countDownTimer.cancel()
                    Toast.makeText(
                        this@TimerActivity,
                        "Server not responding!!!", Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
        else{
            Utilities.hideProgressDialog()

            llTimer.visibility = View.GONE
            llNoInternet.visibility = View.VISIBLE
            countDownTimer.cancel()
        }
    }

    //Fetch bulk data
    private fun fetchBulkUpload() {
        if (Utilities.isNetworkAvailable(this)) {
            llTimer.visibility = View.VISIBLE
            llNoInternet.visibility = View.GONE
            Log.e("Fetchbulkupload", "started......")

            val request = RetrofitClients.buildService(APiService::class.java)
            val userId = RequestBody.create(
                MultipartBody.FORM,
                Utilities.getPreference(this, AppConstants.tokenId)!!)
            val skuId = RequestBody.create(
                MultipartBody.FORM,
                Utilities.getPreference(this, AppConstants.SKU_ID)!!)

            val call = request.fetchBulkImage(userId, skuId)

            call?.enqueue(object : Callback<List<FetchBulkResponse>> {
                override fun onResponse(call: Call<List<FetchBulkResponse>>,
                                        response: Response<List<FetchBulkResponse>>
                ) {
                    if (response.isSuccessful) {
                        Log.e("Upload Replace", "bulk Fetch")
                        imageList = ArrayList<String>()
                        imageListAfter = ArrayList<String>()

                        imageList.clear()
                        imageListAfter.clear()


                        for (i in 0..response.body()!!.size - 1) {
                            imageList.add(response.body()!![i].input_image_url)
                            imageListAfter.add(response.body()!![i].output_image_url)
                        }
                        fetchGif()
                    } else {
                        fetchBulkUpload()
                    }
                }

                override fun onFailure(call: Call<List<FetchBulkResponse>>, t: Throwable) {
                    Utilities.hideProgressDialog()

                    Toast.makeText(this@TimerActivity,
                        "Server not responding!!!", Toast.LENGTH_SHORT).show()

                }
            })
        }
        else{
            Utilities.hideProgressDialog()
            llTimer.visibility = View.GONE
            llNoInternet.visibility = View.VISIBLE
            countDownTimer.cancel()
        }
    }

    private fun fetchGif() {
        if (Utilities.isNetworkAvailable(this)) {
            llTimer.visibility = View.VISIBLE
            llNoInternet.visibility = View.GONE
            val request = RetrofitClientsBulk.buildService(APiService::class.java)
            val fetchGifRequest = FetchGifRequest(imageListAfter)

            val call = request.fetchGif(fetchGifRequest)

            call?.enqueue(object : Callback<FetchGifResponse> {
                override fun onResponse(call: Call<FetchGifResponse>,
                                        response: Response<FetchGifResponse>
                ) {
                    Utilities.hideProgressDialog()
                    if (response.isSuccessful) {
                        Log.e("Upload Replace", "bulk gif fetched")

                        countDownTimer.cancel()
                        gifLink = response.body()!!.url
                        uploadGif()

                    } else {
                        fetchBulkUpload()
                    }
                }

                override fun onFailure(call: Call<FetchGifResponse>, t: Throwable) {
                    Utilities.hideProgressDialog()
                    Toast.makeText(this@TimerActivity,
                        "Server not responding!!!", Toast.LENGTH_SHORT).show()
                }
            })
        }
        else{
            llTimer.visibility = View.GONE
            llNoInternet.visibility = View.VISIBLE
            countDownTimer.cancel()
        }
    }


    private fun uploadGif() {
        if (Utilities.isNetworkAvailable(this)) {
            llTimer.visibility = View.VISIBLE
            llNoInternet.visibility = View.GONE
            val request = RetrofitClients.buildService(APiService::class.java)

            val userId = RequestBody.create(
                MultipartBody.FORM,
                Utilities.getPreference(this, AppConstants.tokenId)!!)

            val skuId = RequestBody.create(
                MultipartBody.FORM,
                Utilities.getPreference(this, AppConstants.SKU_ID)!!)
            val gifUrl = RequestBody.create(
                MultipartBody.FORM,
                gifLink)

            val call = request.uploadUserGif(userId, skuId, gifUrl)

            call?.enqueue(object : Callback<UploadGifResponse> {
                override fun onResponse(call: Call<UploadGifResponse>,
                                        response: Response<UploadGifResponse>
                ) {
                    Utilities.hideProgressDialog()
                    if (response.isSuccessful) {
                        sendEmail()
                    }
                }

                override fun onFailure(call: Call<UploadGifResponse>, t: Throwable) {
                    Utilities.hideProgressDialog()
                    Toast.makeText(this@TimerActivity,
                        "Server not responding!!!", Toast.LENGTH_SHORT).show()
                }
            })
        }
        else{
            Utilities.hideProgressDialog()
            llTimer.visibility = View.GONE
            llNoInternet.visibility = View.VISIBLE
            countDownTimer.cancel()
        }
    }

    private fun sendEmail() {
        if (Utilities.isNetworkAvailable(this)) {
            llTimer.visibility = View.VISIBLE
            llNoInternet.visibility = View.GONE
            //   Utilities.showProgressDialog(this)

            val request = RetrofitClientSpyneAi.buildService(APiService::class.java)

            val sendEmailRequest = SendEmailRequest(imageList,imageListAfter,gifLink,
                Utilities.getPreference(this,AppConstants.EMAIL_ID).toString()
            )
            val call = request.sendEmailAll(sendEmailRequest)

            call?.enqueue(object : Callback<OtpResponse> {
                override fun onResponse(call: Call<OtpResponse>, response: Response<OtpResponse>) {
                    Utilities.hideProgressDialog()
                    if (response.isSuccessful) {
                        if (response.body()!!.id.equals("200")) {
                            val intent = Intent(this@TimerActivity,
                                ShowImagesActivity::class.java)
                            intent.putExtra(AppConstants.GIF, gifLink)
                            startActivity(intent)
                            finish()
                            Toast.makeText(this@TimerActivity, response.body()!!.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<OtpResponse>, t: Throwable) {
                    Log.e("ok", "no way")
                    Utilities.hideProgressDialog()
                    Toast.makeText(this@TimerActivity, "Server not responding!!!", Toast.LENGTH_SHORT).show()
                    //  Utilities.hideProgressDialog()
                    //Toast.makeText(this@MainActivity, "${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
        else{
            Utilities.hideProgressDialog()

            llTimer.visibility = View.GONE
            llNoInternet.visibility = View.VISIBLE
            countDownTimer.cancel()
        }
    }


    private fun moveNext() {
        if (Utilities.getPreference(this@TimerActivity,
                AppConstants.FRAME_SHOOOTS).equals("4")
            || Utilities.getPreference(this@TimerActivity,
                AppConstants.FRAME_SHOOOTS).equals("8")) {
            val intent = Intent(this@TimerActivity,
                ShowImagesActivity::class.java)
            intent.putExtra(AppConstants.GIF, gifLink)
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this@TimerActivity,
                ShowGifActivity::class.java)
            intent.putExtra(AppConstants.GIF, gifLink)
            startActivity(intent)
            finish()
        }

    }


    override fun onBackPressed() {
        //  super.onBackPressed()
        //onDestroy()

        showExitDialog()
    }

    //Exit dialog
    fun showExitDialog( ) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_exit)
        val dialogButtonYes: TextView = dialog.findViewById(R.id.btnYes)
        val dialogButtonNo: TextView = dialog.findViewById(R.id.btnNo)

        dialogButtonYes.setOnClickListener(View.OnClickListener {
            Utilities.savePrefrence(this@TimerActivity, AppConstants.SHOOT_ID, "")
            Utilities.savePrefrence(this@TimerActivity, AppConstants.CATEGORY_ID, "")
            Utilities.savePrefrence(this@TimerActivity, AppConstants.PRODUCT_ID, "")
            Utilities.savePrefrence(this@TimerActivity, AppConstants.SKU_NAME, "")
            Utilities.savePrefrence(this@TimerActivity, AppConstants.SKU_ID, "")
            val intent = Intent(this, DashboardActivity::class.java)

            val updateSkuResponseList = ArrayList<UpdateSkuResponse>()
            updateSkuResponseList.clear()

            Utilities.setList(
                this@TimerActivity,
                AppConstants.FRAME_LIST, updateSkuResponseList
            )
            countDownTimer.cancel()
            startActivity(intent)
            finish()
            dialog.dismiss()

        })
        dialogButtonNo.setOnClickListener(View.OnClickListener { dialog.dismiss() })
        dialog.show()
    }

}