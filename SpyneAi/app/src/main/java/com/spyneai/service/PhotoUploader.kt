package com.spyneai.service

import UploadPhotoResponse
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.util.Log
import com.spyneai.BaseApplication
import com.spyneai.aipack.BulkUploadResponse
import com.spyneai.aipack.FetchBulkResponse
import com.spyneai.aipack.FetchGifRequest
import com.spyneai.aipack.FetchGifResponse
import com.spyneai.interfaces.*
import com.spyneai.model.ai.SendEmailRequest
import com.spyneai.model.ai.UploadGifResponse
import com.spyneai.model.ai.WaterMarkResponse
import com.spyneai.model.marketplace.FootwearBulkResponse
import com.spyneai.model.otp.OtpResponse
import com.spyneai.model.processImageService.Task
import com.spyneai.model.sku.SkuResponse
import com.spyneai.model.skustatus.UpdateSkuStatusRequest
import com.spyneai.model.skustatus.UpdateSkuStatusResponse
import com.spyneai.model.upload.UploadResponse
import com.spyneai.model.uploadRough.UploadPhotoRequest
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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

class PhotoUploader(var task: Task, var listener: Listener) {

    fun start() {
        uploadImageToBucket()
    }

    fun uploadImageToBucket() {
        Log.e("First execution", "UploadImage Bucket")
        log("Start uploading images to bucket")
        //Get All Data to be uploaded


        GlobalScope.launch(Dispatchers.Default) {

            val imageFile = setImageRaw(task.imageFileList[task.totalImagesToUploadIndex])

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
                    //
                    if (response.isSuccessful) {

                        task.mainImage = response.body()?.image.toString()
                        log("onResponse: image upload success:\n" + task.mainImage)

                        uploadImageURLs()
                    } else {

                        log("Error in uploading image to bucket")
                        log("Error Body: " + response.errorBody())
                        log("Response: " + response.body())
                    }
                }

                override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                    listener.onFailure(task)
                    Log.e("Respo Image ", "Image error")

                    log("Server not responding(uploadImageToBucket)")
                    log("onFailure: " + t.localizedMessage)
                }
            })

        }

    }

    fun uploadImageURLs() {

        log("start upload image url")
        val request = RetrofitClient.buildService(APiService::class.java)
        val uploadPhotoName: String = task.mainImage.split("/")[task.mainImage.split("/").size - 1]

        val uploadPhotoRequest = UploadPhotoRequest(
            task.skuName,
            task.skuId,
            "raw",
            task.imageFileListFrames[task.totalImagesToUploadIndex],
            task.shootId,
            task.mainImage,
            uploadPhotoName,
            "EXTERIOR"
        )

        val call = request.uploadPhotoRough(
            task.tokenId, uploadPhotoRequest
        )

        call?.enqueue(object : Callback<UploadPhotoResponse> {
            override fun onResponse(
                call: Call<UploadPhotoResponse>,
                response: Response<UploadPhotoResponse>
            ) {
                if (response.isSuccessful) {
                    try {
                        if (task.totalImagesToUploadIndex < task.totalImagesToUpload - 1) {
                            Log.e("uploadImageURLs", task.totalImagesToUploadIndex.toString())

                            Log.e("IMage uploaded ", response.body()?.msgInfo.toString())
                            task.totalImagesToUploadIndex++

                            uploadImageToBucket()

                        } else {
                            task.totalImagesToUploadIndex = 0
                            task.totalImagesToUpload = task.imageInteriorFileList.size

                            if (!task.imageInteriorFileList.isEmpty()) {
                                uploadImageToBucketInterior()
                            } else
                                markSkuComplete()
                        }
                    } catch (e: Exception) {
                        Log.e("Except", e.printStackTrace().toString())
                    }

                } else {

                    log("Error in uploading image url. Please try again")
                    log("Error: " + response.errorBody())
                }
            }

            override fun onFailure(call: Call<UploadPhotoResponse>, t: Throwable) {

                listener.onFailure(task)

                log("Server not responding(uploadImageURLs)")
                Log.e("Respo Image ", "Image error")
                log("onFailure: " + t.localizedMessage)
            }
        })

    }

    fun uploadImageToBucketInterior() {

        Log.e("First execution", "UploadImage Bucket")
        log("start uploading interior to bucket")
        //Get All Data to be uploaded
        GlobalScope.launch(Dispatchers.Default) {
            val imageFile = setImageRaw(task.imageInteriorFileList[task.totalImagesToUploadIndex])

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
                    //
                    if (response.isSuccessful) {

//                        PROGRESS_CURRENT++

//                        val notification = updateNotification("Interior image uploading started...")
//                        startForeground(notificationID, notification)

                        Log.e("First execution", "UploadImage Bucket")



                        Log.e("uploadImageToBucket", response.body()?.image.toString())
                        Log.e("uploadImageToBucket", task.totalImagesToUploadIndex.toString())
                        log("uploadImageToBucketInterior" + response.body()?.image.toString())
                        log("uploadImageToBucketInterior" + response.body()?.image.toString())

                        task.mainImageInterior = response.body()?.image.toString()
                        uploadImageURLsInterior()
                    } else {

                        log("Error in uploading interior images.")
                        log("Error: " + response.errorBody())
                    }
                }

                override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                    listener.onFailure(task)
                    Log.e("Respo Image ", "Image error")

                    log("Server not responding(uploadImageToBucketInterior): ")
                    log("onFailure: " + t.localizedMessage)
                }
            })
        }
    }

    fun uploadImageURLsInterior() {


        log("start Uploading images url interior")
        val request = RetrofitClient.buildService(APiService::class.java)

        val uploadPhotoName: String =
            task.mainImageInterior.split("/")[task.mainImageInterior.toString().split("/").size - 1]

        val uploadPhotoRequest = UploadPhotoRequest(
            task.skuName,
            task.skuId,
            "raw",
            task.imageInteriorFileListFrames[task.totalImagesToUploadIndex],
            task.shootId,
            task.mainImageInterior,
            uploadPhotoName,
            "INTERIOR"
        )
        val call = request.uploadPhotoRough(
            task.mainImageInterior, uploadPhotoRequest
        )

        call?.enqueue(object : Callback<UploadPhotoResponse> {
            override fun onResponse(
                call: Call<UploadPhotoResponse>,
                response: Response<UploadPhotoResponse>
            ) {
                if (response.isSuccessful) {
                    try {
                        if (task.totalImagesToUploadIndex < task.totalImagesToUpload - 1) {

                            Log.e("IMageInterioruploaded ", response.body()?.msgInfo.toString())
                            log("IMageInterioruploaded: " + response.body()?.msgInfo.toString())
                            Log.e(
                                "Frame 1i",
                                response.body()?.payload!!.data.currentFrame.toString()
                            )
                            log("Frame 1i: " + response.body()?.payload!!.data.currentFrame.toString())
                            Log.e(
                                "Frame 2i",
                                response.body()?.payload!!.data.totalFrames.toString()
                            )
                            log("Frame 2i: " + response.body()?.payload!!.data.totalFrames.toString())


                            task.totalImagesToUploadIndex++
                            uploadImageToBucketInterior(

                            )
                        } else {
                            markSkuComplete(
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("Except", e.printStackTrace().toString())
                    }

                } else {

                    log("Error in uploading image url(INTERIOR)")
                    log("Error: " + response.errorBody())
                }
            }

            override fun onFailure(call: Call<UploadPhotoResponse>, t: Throwable) {
                listener.onFailure(task)

                log("Server not responding(uploadImageURLsInterior)")
                Log.e("Respo Image ", "Image error")
                log("onFailure: " + t.localizedMessage)
            }
        })

    }

    suspend fun setImageRaw(photoFile: File): File? {
        val myBitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
        var ei: ExifInterface? = null
        try {
            ei = ExifInterface(photoFile.absolutePath)
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

    suspend fun rotateImage(source: Bitmap, angle: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }

    suspend fun persistImage(bitmap: Bitmap): File? {
        var imageFile: File? = null
        if (BaseApplication.getContext() != null) {
            val filesDir: File = BaseApplication.getContext().filesDir
            imageFile = File(filesDir, "photo" + System.currentTimeMillis() + ".png")
            val os: OutputStream
            try {
                os = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.PNG, 70, os)
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
        log("start markSkuComplete")
        val request = RetrofitClient.buildService(APiService::class.java)

        val updateSkuStatusRequest = UpdateSkuStatusRequest(
            task.skuId,
            task.shootId,
            true
        )
        val call = request.updateSkuStauts(
            task.tokenId,
            updateSkuStatusRequest
        )

        call?.enqueue(object : Callback<UpdateSkuStatusResponse> {
            override fun onResponse(
                call: Call<UpdateSkuStatusResponse>,
                response: Response<UpdateSkuStatusResponse>
            ) {

//                    val notification = updateNotification("Mark SKU started...")
//                    startForeground(notificationID, notification)

                if (response.isSuccessful) {
                    Log.e("Sku completed", "MArked Complete")
                    fetchSkuData()
                }
            }

            override fun onFailure(call: Call<UpdateSkuStatusResponse>, t: Throwable) {
                fetchSkuData()
                log("Server not responding(markSkuComplete)")
                log("onFailure: " + t.localizedMessage)

            }
        })

    }

    private fun fetchSkuData(

    ) {


        log("start fetchSkuData")
        val request = RetrofitClient.buildService(APiService::class.java)

        val call = request.getSkuDetails(
            task.tokenId,
            task.skuId
        )

        call?.enqueue(object : Callback<SkuResponse> {
            override fun onResponse(
                call: Call<SkuResponse>,
                response: Response<SkuResponse>
            ) {

                if (response.isSuccessful) {
                    if (response.body()?.payload != null) {
                        if (response.body()?.payload!!.data.photos.size > 0) {
                            task.photoList.clear()
                            task.photoListInteriors.clear()

                            for (i in 0..response.body()?.payload!!.data.photos.size - 1) {
                                if (response.body()?.payload!!.data.photos[i].photoType.equals("EXTERIOR"))
                                    task.photoList.add(response.body()?.payload!!.data.photos[i])
                                else
                                    task.photoListInteriors.add(response.body()?.payload!!.data.photos[i])
                            }
                        }
                    }
                    if (task.countGif < task.photoList.size) {

                        when (task.catName) {
                            "Automobiles" -> bulkUpload()
                            "Footwear" -> bulkUploadFootwear()
                        }

                    }
                } else {

                    log("Error in fetch sku data")
                    log("Error: " + response.errorBody())
                }
            }

            override fun onFailure(call: Call<SkuResponse>, t: Throwable) {
                listener.onFailure(task)
                log("Server not responding(fetchSkuData)")
                log("onFailure: " + t.localizedMessage)
            }
        })

    }

    //Upload bulk data
    private fun bulkUpload(

    ) {
//        PROGRESS_MAX = photoList.size


        log("start bulk upload")
        val request = RetrofitClients.buildService(APiService::class.java)
        Log.e("Upload bulk", "started......")

        val Background = RequestBody.create(
            MultipartBody.FORM,
            task.backgroundSelect
        )

        val UserId = RequestBody.create(
            MultipartBody.FORM,
            task.tokenId
        )
        val SkuId = RequestBody.create(
            MultipartBody.FORM,
            task.skuId
        )
        val ImageUrl = RequestBody.create(
            MultipartBody.FORM,
            task.photoList[task.countGif].displayThumbnail
        )
        Log.e(
            "Sku NAme Upload done",
            task.skuName
        )
        val SkuName = RequestBody.create(
            MultipartBody.FORM,
            task.skuName
        )

        val WindowStatus = RequestBody.create(
            MultipartBody.FORM,
            task.windows
        )

        val contrast = RequestBody.create(
            MultipartBody.FORM,
            task.exposures
        )

        val call =
            request.bulkUPload(
                Background, UserId, SkuId,
                ImageUrl, SkuName, WindowStatus, contrast
            )

        call?.enqueue(object : Callback<BulkUploadResponse> {
            override fun onResponse(
                call: Call<BulkUploadResponse>,
                response: Response<BulkUploadResponse>
            ) {
                if (response.isSuccessful && response.body()!!.status == 200) {

                    task.countGif++
//                        PROGRESS_CURRENT++

//                        val notification = updateNotification("AI for Image Processing started...")
//                        startForeground(notificationID, notification)

                    if (task.countGif < task.photoList.size) {
                        Log.e("countGif", task.countGif.toString())
                        bulkUpload()
                    } else if (task.photoListInteriors.size > 0) {
                        task.countGif = 0
                        if (task.countGif < task.photoListInteriors.size) {
                            addWatermark()
                        }
                    } else
                        fetchBulkUpload(

                        )

                    Log.e("Upload Replace", "bulk")
                    Log.e(
                        "Upload Replace SKU",
                        task.skuName
                    )
                } else {


                    log("Error in bulk upload")
                    log("Error: " + response.errorBody())
                    log("Response: " + response.body())
                }
            }

            override fun onFailure(call: Call<BulkUploadResponse>, t: Throwable) {

                listener.onFailure(task)
                log("Server not responding(bulkUpload)")
                log("onFailure: " + t.localizedMessage)
            }
        })

    }

    //Fetch bulk data
    private fun fetchBulkUpload(

    ) {


        Log.e("Fetchbulkupload", "started......")
        log("start featch bulk upload")

        val request = RetrofitClients.buildService(APiService::class.java)
        val UserId = RequestBody.create(
            MultipartBody.FORM,
            task.tokenId
        )
        val SkuId = RequestBody.create(
            MultipartBody.FORM,
            task.skuId
        )

        val call = request.fetchBulkImage(UserId, SkuId)

        call?.enqueue(object : Callback<List<FetchBulkResponse>> {
            override fun onResponse(
                call: Call<List<FetchBulkResponse>>,
                response: Response<List<FetchBulkResponse>>
            ) {
                if (response.isSuccessful) {

                    Log.e("Upload Replace", "bulk Fetch")

                    task.imageList.clear()
                    task.imageListAfter.clear()
                    task.interiorList.clear()

                    for (i in 0..response.body()!!.size - 1) {
                        if (response.body()!![i].category.equals("Exterior")) {
                            task.imageList.add(response.body()!![i].input_image_url)
                            task.imageListAfter.add(response.body()!![i].output_image_url)
                        } else {
                            task.interiorList.add(response.body()!![i].output_image_url)
                        }
                    }
                    if (task.catName.equals("Automobiles")) {
                        fetchGif()
                    } else {


                        sendEmail()
                    }

                } else {
                    fetchBulkUpload()
                }
            }

            override fun onFailure(call: Call<List<FetchBulkResponse>>, t: Throwable) {
                listener.onFailure(task)
                log("Server not responding(fetchBulkUpload)")
                log("onFailure: " + t.localizedMessage)

            }
        })

    }

    private fun bulkUploadFootwear(

    ) {


        val request = RetrofitClients.buildService(APiService::class.java)
        Log.e("Upload bulk footwear", "started......")
        log("start bulk Upload Footwear")
        val marketplace_id = RequestBody.create(
            MultipartBody.FORM,
            task.marketplaceId
        )

        val bg_color = RequestBody.create(
            MultipartBody.FORM,
            task.backgroundColour
        )

        val UserId = RequestBody.create(
            MultipartBody.FORM,
            task.tokenId
        )
        val SkuId = RequestBody.create(
            MultipartBody.FORM,
            task.skuId
        )
        val imageUrl = RequestBody.create(
            MultipartBody.FORM,
            task.photoList[task.countGif].displayThumbnail
        )
        Log.e(
            "Sku NAme Upload",
            task.skuName
        )
        val SkuName = RequestBody.create(
            MultipartBody.FORM,
            task.skuName
        )

        val windowStatus = RequestBody.create(
            MultipartBody.FORM,
            "inner"
        )

        val call =
            request.bulkUPloadFootwear(
                UserId,
                SkuId,
                imageUrl,
                SkuName,
                marketplace_id,
                bg_color
            )

        call?.enqueue(object : Callback<FootwearBulkResponse> {
            override fun onResponse(
                call: Call<FootwearBulkResponse>,
                response: Response<FootwearBulkResponse>
            ) {
                if (response.isSuccessful && response.body()!!.status == 200) {

//                        val notification = updateNotification("AI for Image Processing started...")
//                        startForeground(notificationID, notification)

                    ++task.countGif
                    if (task.countGif < task.photoList.size) {
                        Log.e("countGif", task.countGif.toString())
                        bulkUploadFootwear(

                        )
                    } else
                        fetchBulkUpload(

                        )
                    log("Upload Replace: "+ "bulk")
                    log(
                        "Upload Replace SKU: "+
                        task.skuName
                    )
                } else {
                    listener.onFailure(task)
                    log("Error in bulk upload footwear")
                    log("Error: " + response.errorBody())
                }
            }

            override fun onFailure(call: Call<FootwearBulkResponse>, t: Throwable) {
                listener.onFailure(task)
                log("Server not responding(bulkUploadFootwear)")
                log("onFailure: " + t.localizedMessage)
            }
        })

    }

    private fun addWatermark(

    ) {


        val request = RetrofitClients.buildService(APiService::class.java)
        Log.e("Watermark bulk", "started......")
        log("start add watermark")

        val background = RequestBody.create(
            MultipartBody.FORM,
            task.backgroundSelect
        )

        val UserId = RequestBody.create(
            MultipartBody.FORM,
            task.tokenId
        )
        val SkuId = RequestBody.create(
            MultipartBody.FORM,
            task.skuId
        )
        val imageUrl = RequestBody.create(
            MultipartBody.FORM,
            task.photoListInteriors[task.countGif].displayThumbnail
        )

        val SkuName = RequestBody.create(
            MultipartBody.FORM,
            task.skuName
        )

        val call =
            request.addWaterMark(
                background, UserId, SkuId,
                imageUrl, SkuName
            )

        call?.enqueue(object : Callback<WaterMarkResponse> {
            override fun onResponse(
                call: Call<WaterMarkResponse>,
                response: Response<WaterMarkResponse>
            ) {
                if (response.isSuccessful && response.body()!!.status == 200) {

                    task.countGif++
                    if (task.countGif < task.photoListInteriors.size) {
                        Log.e("countGif", task.countGif.toString())
                        addWatermark(

                        )
                    } else
                        fetchBulkUpload(

                        )
                    Log.e("Upload Replace", "bulk")
                    Log.e(
                        "Upload Replace SKU",
                        task.skuName
                    )
                } else {
                    listener.onFailure(task)
                    log("Error in add watermark")
                    log("Error: " + response.errorBody())
                }
            }

            override fun onFailure(call: Call<WaterMarkResponse>, t: Throwable) {
                listener.onFailure(task)
                log("Server not responding(addWatermark)")
                log("onFailure: " + t.localizedMessage)
            }
        })

    }

    private fun fetchGif(

    ) {

        log("start fetch gif")
        val request = RetrofitClientsBulk.buildService(APiService::class.java)
        val fetchGifRequest = FetchGifRequest(task.imageListAfter)

        val call = request.fetchGif(fetchGifRequest)

        call?.enqueue(object : Callback<FetchGifResponse> {
            override fun onResponse(
                call: Call<FetchGifResponse>,
                response: Response<FetchGifResponse>
            ) {
                if (response.isSuccessful) {
                    Log.e("Upload Replace", "bulk gif fetched")

                    task.gifLink = response.body()!!.url
                    uploadGif(

                    )
                } else {
                    fetchBulkUpload(

                    )
                }
            }

            override fun onFailure(call: Call<FetchGifResponse>, t: Throwable) {
                listener.onFailure(task)
                log("Server not responding(fetchGif)")
                log("onFailure: " + t.localizedMessage)
            }
        })

    }


    private fun uploadGif(

    ) {

        log("start upload gif")
        val request = RetrofitClients.buildService(APiService::class.java)

        val UserId = RequestBody.create(
            MultipartBody.FORM,
            task.tokenId
        )

        val SkuId = RequestBody.create(
            MultipartBody.FORM,
            task.skuId
        )
        val gifUrl = RequestBody.create(
            MultipartBody.FORM,
            task.gifLink
        )

        val call = request.uploadUserGif(UserId, SkuId, gifUrl)

        call?.enqueue(object : Callback<UploadGifResponse> {
            override fun onResponse(
                call: Call<UploadGifResponse>,
                response: Response<UploadGifResponse>
            ) {
                if (response.isSuccessful) {
                    sendEmail(
                    )
                }
            }

            override fun onFailure(call: Call<UploadGifResponse>, t: Throwable) {
                listener.onFailure(task)
                log("Server not responding(uploadGif)")
                log("onFailure: " + t.localizedMessage)
            }
        })

    }

    private fun sendEmail() {

        log("start send email")
        val request = RetrofitClientSpyneAi.buildService(APiService::class.java)

        val sendEmailRequest = SendEmailRequest(
            task.imageList, task.imageListAfter, task.interiorList, task.gifLink,
            Utilities.getPreference(BaseApplication.getContext(), AppConstants.EMAIL_ID).toString()
        )
        val call = request.sendEmailAll(sendEmailRequest)

        call?.enqueue(object : Callback<OtpResponse> {
            override fun onResponse(call: Call<OtpResponse>, response: Response<OtpResponse>) {
                if (response.isSuccessful) {
                    if (response.body()!!.id.equals("200")) {

                        log("" + response.body()!!.message)
//                            val notification = updateNotification("Output email sent...")
//                            startForeground(notificationID, notification)

                        listener.onSuccess(task)
                    }
                } else {
                    listener.onFailure(task)
                }
            }

            override fun onFailure(call: Call<OtpResponse>, t: Throwable) {
                listener.onFailure(task)
                Log.e("ok", "no way")

                log("Server not responding(sendEmail)")
                log("onFailure" + t.localizedMessage)
            }
        })

    }


}

interface Listener {
    fun onSuccess(task: Task)
    fun onFailure(task: Task)
}