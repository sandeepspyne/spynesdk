package com.spyneai.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.util.Log
import androidx.work.*
import com.spyneai.*
import com.spyneai.R
import com.spyneai.base.network.ClipperApi
import com.spyneai.base.network.Resource
import com.spyneai.interfaces.GcpClient
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ImageLocalRepository
import com.spyneai.shoot.data.ShootLocalRepository
import com.spyneai.shoot.data.ShootRepository
import com.spyneai.shoot.data.model.Image
import com.spyneai.shoot.utils.logUpload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.util.*
import kotlin.collections.HashMap

class ImageUploader(
    val context: Context,
    val localRepository: ImageLocalRepository,
    val shootRepository: ShootRepository,
    var listener: Listener,
    var lastIdentifier: String = "0"
) {

    val TAG = "ImageUploader"

    fun start() {
        selectLastImageAndUpload(AppConstants.REGULAR, 0)
    }

    private fun selectLastImageAndUpload(imageType: String, retryCount: Int) {

        if (context.isInternetActive()) {
            GlobalScope.launch(Dispatchers.Default) {

                lastIdentifier = if (retryCount == 0) getUniqueIdentifier() else lastIdentifier

                var skipFlag = -1
                val image = if (imageType == AppConstants.REGULAR) {
                    localRepository.getOldestImage("0")
                } else {
                    skipFlag = -2
                    localRepository.getOldestImage("-1")
                }

                if (image.itemId != null) {
                    //uploading enqueued
                    lastIdentifier = image.name + "_" + image.skuId

                    val imageProperties = HashMap<String, Any?>()
                        .apply {
                            put("iteration_id", lastIdentifier)
                            put("retry_count", retryCount)
                            put("image_id", image.imageId)
                            put("image_local_id", image.itemId)
                            put("project_id", image.projectId)
                            put("sku_id", image.skuId)
                            put("sku_name", image.skuName)
                            put("upload_status", image.isUploaded)
                            put("make_done_status", image.isStatusUpdated)
                            put("image_name", image.name)
                            put("overlay_id", image.overlayId)
                            put("sequence", image.sequence)
                            put("pre_url", image.preSignedUrl)
                            put("is_reclick", image.isReclick)
                            put("is_reshoot", image.isReshoot)
                            put("image_path", image.imagePath)
                            put("upload_type", imageType)
                        }

                    context.captureEvent(
                        AppConstants.IMAGE_SELECTED,
                        imageProperties
                    )


                    listener.inProgress(image)

                    if (retryCount > 4) {
                        if (image.isUploaded != 1)
                            localRepository.skipImage(image.itemId!!, skipFlag)

                        startNextUpload(image.itemId!!, false, imageType)

                        captureEvent(Events.MAX_RETRY, image, false, "Image upload limit reached")
                        return@launch
                    }

                    if (image.isUploaded == 0 || image.isUploaded == -1) {
                        logUpload("presignerd url " + image.preSignedUrl)
                        context.captureEvent(
                            Events.IMAGE_NOT_UPLOADED,
                            imageProperties
                        )

                        if (image.preSignedUrl != AppConstants.DEFAULT_PRESIGNED_URL) {
                            context.captureEvent(
                                Events.UPLOADING_TO_GCP_INITIATED,
                                imageProperties
                            )

                            when (image.categoryName) {
                                "Exterior",
                                "Interior",
                                "Focus Shoot",
                                "360int",
                                "Info" -> {
                                    uploadImageToGcp(image, imageType, retryCount)
                                }
                                else -> {
                                    val bitmap =
                                        modifyOrientation(
                                            BitmapFactory.decodeFile(image.imagePath),
                                            image.imagePath
                                        )

                                    try {
                                        val file = File(image.imagePath)
                                        val os: OutputStream = BufferedOutputStream(
                                            FileOutputStream(file)
                                        )
                                        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, os)
                                        os.close()

                                        uploadImageToGcp(image, imageType, retryCount)
                                    } catch (
                                        e: java.lang.Exception
                                    ) {
                                        val s = ""
                                    }
                                }
                            }
                        } else {
                            val uploadType = if (retryCount == 0) "Direct" else "Retry"
                            //val uploadType = "Retry"

                            var response = shootRepository.getPreSignedUrl(
                                uploadType,
                                image
                            )

                            when (response) {
                                is Resource.Success -> {
                                    image.preSignedUrl = response.value.data.presignedUrl
                                    image.imageId = response.value.data.imageId

                                    captureEvent(Events.GOT_PRESIGNED_IMAGE_URL, image, true, null)

                                    val count = localRepository.addPreSignedUrl(image)
                                    val updatedImage = localRepository.getImage(image.itemId!!)

                                    captureEvent(
                                        Events.IS_PRESIGNED_URL_UPDATED,
                                        updatedImage,
                                        true,
                                        null,
                                        count
                                    )

                                    when (image.categoryName) {
                                        "Exterior",
                                        "Interior",
                                        "Focus Shoot",
                                        "360int",
                                        "Info" -> {
                                            uploadImageToGcp(image, imageType, retryCount)
                                        }
                                        else -> {
                                            val bitmap =
                                                modifyOrientation(
                                                    BitmapFactory.decodeFile(image.imagePath),
                                                    image.imagePath
                                                )

                                            try {
                                                val file = File(image.imagePath)
                                                val os: OutputStream = BufferedOutputStream(
                                                    FileOutputStream(file)
                                                )
                                                bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, os)
                                                os.close()

                                                uploadImageToGcp(image, imageType, retryCount)
                                            } catch (
                                                e: java.lang.Exception
                                            ) {
                                                val s = ""
                                            }
                                        }
                                    }
                                }

                                is Resource.Failure -> {
                                    log("Image upload failed")
                                    logUpload("Upload error " + response.errorCode.toString() + " " + response.errorMessage)
                                    if (response.errorMessage == null) {
                                        captureEvent(
                                            Events.GET_PRESIGNED_FAILED,
                                            image,
                                            false,
                                            response.errorCode.toString() + ": Http exception from server"
                                        )
                                        selectLastImageAndUpload(imageType, retryCount + 1)
                                    } else {
                                        // if duplicated entry error change status
                                        if (response.errorCode == 500
                                            && (response.errorMessage.toString()
                                                .contains("Duplicate entry")
                                                    && response.errorMessage.toString()
                                                .contains("image_name_sku_id"))
                                        ) {

                                            checkImageStatusOnServer(image, imageType, retryCount)
                                        } else {
                                            captureEvent(
                                                Events.GET_PRESIGNED_FAILED,
                                                image,
                                                false,
                                                response.errorCode.toString() + ": " + response.errorMessage
                                            )
                                            selectLastImageAndUpload(imageType, retryCount + 1)
                                        }

                                    }
                                }
                            }
                        }
                    } else {
                        setStatusUploaed(image, imageType, retryCount)
                    }
                } else {
                    logUpload("All Images uploaded")
                    if (imageType == AppConstants.REGULAR) {
                        //start skipped images worker
                        logUpload("Start Skipped Images uploaded")
                        selectLastImageAndUpload(AppConstants.SKIPPED, 0)
                    } else {
                        //make second time skipped images elligible for upload
                        val count = localRepository.updateSkipedImages()

                        //check if we don"t have any new image clicked while uploading skipped images
                        if (localRepository.getOldestImage("0").itemId == null) {
                            if (count > 0) {
                                //upload double skipped images if we don't have any new image
                                selectLastImageAndUpload(AppConstants.SKIPPED, 0)
                            } else
                                listener.onUploaded(image)
                        } else {
                            //upload images clicked while service uploading skipped images
                            selectLastImageAndUpload(AppConstants.REGULAR, 0)
                        }
                    }
                }
            }
        } else {
            listener.onConnectionLost()
        }
    }


    @Throws(IOException::class)
    fun modifyOrientation(bitmap: Bitmap, image_absolute_path: String?): Bitmap? {
        val ei = ExifInterface(image_absolute_path!!)
        val orientation =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        return when (orientation) {
            ExifInterface.ORIENTATION_NORMAL,
            ExifInterface.ORIENTATION_ROTATE_90 -> rotate(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotate(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotate(bitmap, 270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flip(bitmap, true, false)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> flip(bitmap, false, true)
            else -> bitmap
        }
    }

    fun rotate(bitmap: Bitmap, degrees: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun flip(bitmap: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap? {
        val matrix = Matrix()
        matrix.preScale(
            (if (horizontal) -1 else 1.toFloat()) as Float,
            (if (vertical) -1 else 1.toFloat()) as Float
        )
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }


    private suspend fun checkImageStatusOnServer(image: Image, imageType: String, retryCount: Int) {
        if (image.imageId != null) {
            val response = shootRepository.getImageData(image.imageId!!)

            when (response) {
                is Resource.Success -> {
                    //send success event
                    captureEvent(Events.GOT_IMAGE_DATA, image, true, null)

                    if (response.value.data.status != "Yet to Upload") {
                        //mark image status uploaded in DB
                        val isUpdated = localRepository.markDone(image)
                        //send db update event
                        captureEvent(
                            Events.IMAGE_DATA_UPDATED_LOCALLY,
                            image,
                            true,
                            null,
                            isUpdated
                        )

                        //upload next image
                        selectLastImageAndUpload(imageType, retryCount + 1)
                    }
                }

                is Resource.Failure -> {
                    if (response.errorMessage == null) {
                        captureEvent(
                            Events.GET_IMAGE_DATA_FAILED,
                            image,
                            false,
                            response.errorCode.toString() + ": Http exception from server"
                        )
                    } else {
                        captureEvent(
                            Events.GET_IMAGE_DATA_FAILED,
                            image,
                            false,
                            response.errorCode.toString() + ": " + response.errorMessage
                        )
                    }

                    //send failure event and upload next
                    selectLastImageAndUpload(imageType, retryCount + 1)
                }
            }
        } else {
            //upload next image
            selectLastImageAndUpload(imageType, retryCount + 1)
        }
    }

    private fun getUniqueIdentifier(): String {
        val SALTCHARS = "abcdefghijklmnopqrstuvwxyz1234567890"
        val salt = StringBuilder()
        val rnd = Random()
        while (salt.length < 7) { // length of the random string.
            //val index = (rnd.nextFloat() * SALTCHARS.length) as Int
            val index = rnd.nextInt(SALTCHARS.length)
            salt.append(SALTCHARS[index])
        }
        return salt.toString()
    }

    private fun uploadImageToGcp(image: Image, imageType: String, retryCount: Int) {
        // create RequestBody instance from file
        val requestFile =
            File(image.imagePath).asRequestBody("text/x-markdown; charset=utf-8".toMediaTypeOrNull())

        //upload image with presigned url
        val request = GcpClient.buildService(ClipperApi::class.java)

        val call = request.uploadVideo(
            "application/octet-stream",
            image.preSignedUrl!!,
            requestFile
        )

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d(TAG, "onResponse: " + response.code())
                if (response.isSuccessful) {
                    val count = localRepository.markUploaded(image)
                    val updatedImage = localRepository.getImage(image.itemId!!)


                    captureEvent(
                        Events.IS_MARK_GCP_UPLOADED_UPDATED,
                        updatedImage,
                        true,
                        null,
                        count
                    )
//                    captureEvent(
//                        AppConstants.IS_MARK_GCP_UPLOADED_UPDATED,
//                        Properties()
//                            .apply {
//                                put("iteration_id",lastIdentifier)
//                                put("retry_count",retryCount)
//                                put("image_id",image.itemId)
//                                put("pre_url",updatedImage.preSignedUrl)
//                                put("is_updated",count != 0)
//                                put("upload_status",updatedImage.isStatusUpdated)
//                                put("make_done_status",updatedImage.isStatusUpdated)
//                            }
//                    )

                    onImageUploaded(
                        image,
                        imageType,
                        retryCount
                    )
                } else {
                    onImageUploadFailed(
                        imageType,
                        retryCount,
                        image,
                        response.errorBody().toString()
                    )
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d(TAG, "onFailure: " + t.message)
                onImageUploadFailed(
                    imageType,
                    retryCount,
                    image,
                    t.message
                )
            }

        })
    }

    private fun onImageUploaded(image: Image, imageType: String, retryCount: Int) {
        captureEvent(Events.IMAGE_UPLOADED_TO_GCP, image, true, null)

        GlobalScope.launch(Dispatchers.Default) {
            setStatusUploaed(image, imageType, retryCount)
        }
    }

    private fun onImageUploadFailed(
        imageType: String,
        retryCount: Int,
        image: Image,
        error: String?
    ) {
        captureEvent(Events.IMAGE_UPLOAD_TO_GCP_FAILED, image, false, error)

        GlobalScope.launch(Dispatchers.Default) {
            selectLastImageAndUpload(imageType, retryCount + 1)
        }
    }

    private suspend fun setStatusUploaed(image: Image, imageType: String, retryCount: Int) {
        val response = shootRepository.markUploaded(image.imageId!!)

        when (response) {
            is Resource.Success -> {
                captureEvent(Events.MARKED_IMAGE_UPLOADED, image, true, null)

                val count = localRepository.markStatusUploaded(image)
                val updatedImage = localRepository.getImage(image.itemId!!)

                captureEvent(
                    Events.IS_MARK_DONE_STATUS_UPDATED,
                    updatedImage,
                    true,
                    null,
                    count
                )
//                   captureEvent(
//                       AppConstants.IS_MARK_DONE_STATUS_UPDATED,
//                       Properties()
//                           .apply {
//                               put("iteration_id",lastIdentifier)
//                               put("retry_count",retryCount)
//                               put("image_id",image.itemId)
//                               put("pre_url",updatedImage.preSignedUrl)
//                               put("is_updated",count != 0)
//                               put("upload_status",updatedImage.isStatusUpdated)
//                               put("make_done_status",updatedImage.isStatusUpdated)
//                           }
//                   )

                selectLastImageAndUpload(imageType, 0)
            }

            is Resource.Failure -> {
                if (response.errorMessage == null) {
                    captureEvent(
                        Events.MARK_IMAGE_UPLOADED_FAILED,
                        image,
                        false,
                        response.errorCode.toString() + ": Http exception from server"
                    )
                } else {
                    captureEvent(
                        Events.MARK_IMAGE_UPLOADED_FAILED,
                        image,
                        false,
                        response.errorCode.toString() + ": " + response.errorMessage
                    )
                }

                selectLastImageAndUpload(imageType, retryCount + 1)
            }
        }

    }

    private fun startNextUpload(itemId: Long, uploaded: Boolean, imageType: String) {
        logUpload("Start next upload " + uploaded)
        //remove uploaded item from database
        if (uploaded)
            localRepository.deleteImage(itemId)

        selectLastImageAndUpload(imageType, 0)
    }

    private fun captureEvent(
        eventName: String,
        image: Image,
        isSuccess: Boolean,
        error: String?,
        dbUpdateStatus: Int = 0
    ) {
        val properties = HashMap<String, Any?>()
            .apply {
                put("iteration_id", lastIdentifier)
                put("image_id", image.imageId)
                put("image_local_id", image.itemId)
                put("project_id", image.projectId)
                put("sku_id", image.skuId)
                put("sku_name", image.skuName)
                put("upload_status", image.isUploaded)
                put("make_done_status", image.isStatusUpdated)
                put("image_name", image.name)
                put("overlay_id", image.overlayId)
                put("sequence", image.sequence)
                put("pre_url", image.preSignedUrl)
                put("is_reclick", image.isReclick)
                put("is_reshoot", image.isReshoot)
                put("image_path", image.imagePath)
                put("image_type", image.categoryName)
                put("db_update_status", dbUpdateStatus)
                // put("upload_type",imageType)
            }

        if (isSuccess) {
            context.captureEvent(
                eventName,
                properties
            )
        } else {
            context.captureFailureEvent(
                eventName,
                properties, error!!
            )
        }
    }


    interface Listener {
        fun inProgress(task: Image)
        fun onUploaded(task: Image)
        fun onUploadFail(task: Image)
        fun onConnectionLost()
    }

}