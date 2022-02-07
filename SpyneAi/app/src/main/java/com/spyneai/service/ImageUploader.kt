package com.spyneai.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.room.Room
import com.google.gson.Gson
import com.spyneai.*
import com.spyneai.base.network.Resource
import com.spyneai.base.room.AppDatabase
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ImagesRepoV2
import com.spyneai.shoot.data.ShootRepository
import com.spyneai.shoot.repository.model.image.Image
import id.zelory.compressor.Compressor
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.*
import java.util.*
import kotlin.collections.HashMap
import android.provider.MediaStore

import android.content.ContentValues
import android.os.Environment
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution


class ImageUploader(
    val context: Context,
    val localRepository: ImagesRepoV2,
    val shootRepository: ShootRepository,
    var listener: DataSyncListener,
    var lastIdentifier: String = "0",
    var imageType: String = AppConstants.REGULAR,
    var retryCount: Int = 0,
    var connectionLost: Boolean = false,
    var isActive: Boolean = false
) {

    companion object{
        @Volatile
        private var INSTANCE: ImageUploader? = null

        fun getInstance(context: Context,listener: DataSyncListener): ImageUploader {
            synchronized(this) {
                var instance = ImageUploader.INSTANCE

                if (instance == null) {
                    instance = ImageUploader(
                        context,
                        ImagesRepoV2(AppDatabase.getInstance(BaseApplication.getContext()).imageDao()),
                        ShootRepository(),
                        listener
                    )

                    INSTANCE = instance
                }
                return instance
            }
        }
    }

    val TAG = "ImageUploader"
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    fun uploadParent(type : String,startedBy : String?) {
        context.captureEvent("UPLOAD PARENT TRIGGERED",HashMap<String,Any?>().apply {
            put("type",type)
            put("service_started_by",startedBy)
            put("upload_running",isActive)
        })

        //update triggered value
        Utilities.saveBool(context, AppConstants.UPLOAD_TRIGGERED, true)

        val handler = Handler(Looper.getMainLooper())

        handler.postDelayed({
            if (Utilities.getBool(context, AppConstants.UPLOAD_TRIGGERED, true) && !isActive) {
                if (context.isInternetActive())
                    scope.launch {
                        this@ImageUploader.isActive = true
                        context.captureEvent("START UPLOADING CALLED",HashMap())
                        startUploading()
                    }
                else {
                    this@ImageUploader.isActive = false
                    listener.onConnectionLost("Image uploading paused",ServerSyncTypes.UPLOAD)
                }
            }
        }, getRandomNumberInRange().toLong())
    }

    suspend fun startUploading() {
        do {
            lastIdentifier = getUniqueIdentifier()

            var image = localRepository.getOldestImage()

            if (connectionLost){
                context.captureEvent(
                    AppConstants.CONNECTION_BREAK,
                    HashMap<String,Any?>()
                        .apply {
                            put("remaining_images",JSONObject().apply {
                                put("upload_remaining",localRepository.totalRemainingUpload())
                                put("mark_done_remaining",localRepository.totalRemainingMarkDone())
                            }.toString())
                        }
                )
                this@ImageUploader.isActive = false
                val text = if (image == null) "Image uploading paused" else "Image uploading paused at ${image.skuName?.uppercase()} (${image.image_category}-${image.sequence})"
                listener.onConnectionLost(text,ServerSyncTypes.UPLOAD)
                break
            }


            if (image == null){
                context.captureEvent(
                    AppConstants.ALL_UPLOADED_BREAK,
                    HashMap<String,Any?>()
                        .apply {
                            put("remaining_images",JSONObject().apply {
                                put("upload_remaining",localRepository.totalRemainingUpload())
                                put("mark_done_remaining",localRepository.totalRemainingMarkDone())
                            }.toString())
                        }
                )
                break
            }
            else {
                lastIdentifier = image.name + "_" + image.skuId

                val imageProperties = HashMap<String, Any?>()
                    .apply {
                        put("sku_id", image.skuId)
                        put("iteration_id", lastIdentifier)
                        put("retry_count", retryCount)
                        put("upload_type", imageType)
                        put("data", Gson().toJson(image))
                        put("remaining_images",JSONObject().apply {
                            put("upload_remaining",localRepository.totalRemainingUpload())
                            put("mark_done_remaining",localRepository.totalRemainingMarkDone())
                            put("remaining_above", localRepository.getRemainingAbove(image.uuid!!))
                            put("remaining_above_skipped", localRepository.getRemainingAboveSkipped(image.uuid!!))
                            put("remaining_below", localRepository.getRemainingBelow(image.uuid!!))
                            put("remaining_below_skipped", localRepository.getRemainingBelowSkipped(image.uuid!!))
                        }.toString())

                    }

                context.captureEvent(
                    AppConstants.IMAGE_SELECTED,
                    imageProperties
                )

                listener.inProgress("Uploading ${image.skuName?.uppercase()} (${image.image_category}-${image.sequence})",ServerSyncTypes.UPLOAD)
                this@ImageUploader.isActive = true

                if (retryCount > 4) {
                    val skip = localRepository.skipImage(
                        image.uuid,
                        image.toProcessAT.plus( image.retryCount * AppConstants.RETRY_DELAY_TIME)
                    )

                    captureEvent(
                        Events.MAX_RETRY,
                        image,
                        false,
                        "Image upload limit reached",
                        skip
                    )
                    retryCount = 0
                    continue
                }

                if (!image.isUploaded) {
                    if (image.preSignedUrl != AppConstants.DEFAULT_PRESIGNED_URL) {
                        when (image.image_category) {
                            "Exterior",
                            "Interior",
                            "Focus Shoot",
                            "360int",
                            "Info" -> {
                                val imageUploaded = uploadImage(image)

                                if (!imageUploaded)
                                    continue

                                val imageMarkedDone = markDoneImage(image)

                                continue
                            }
                            else -> {
                                val bitmap =
                                    modifyOrientation(
                                        BitmapFactory.decodeFile(image.path),
                                        image.path
                                    )

                                try {
                                    File(outputDirectory).mkdirs()
                                    val outputFile = File(
                                        outputDirectory + System.currentTimeMillis()
                                            .toString() + ".jpg"
                                    )
                                    outputFile.createNewFile()

                                    val os: OutputStream = BufferedOutputStream(
                                        FileOutputStream(outputFile)
                                    )
                                    bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, os)
                                    os.close()

                                    image.path = outputFile.path
                                    val imageUploaded = uploadImage(image)

                                    if (!imageUploaded)
                                        continue

                                    val imageMarkedDone = markDoneImage(image)
                                    Log.d(
                                        TAG, "startUploading: imageMarkedDone " + imageMarkedDone
                                    )
                                    continue
                                } catch (
                                    e: Exception
                                ) {
                                    captureEvent(
                                        Events.IMAGE_ROTATION_EXCEPTION,
                                        image,
                                        false,
                                        e.localizedMessage
                                    )
                                    retryCount++
                                    continue
                                }
                            }
                        }
                    } else {
                        val uploadType = if (retryCount == 1) "Direct" else "Retry"
                        image.tags =
                            if (image.tags.isNullOrEmpty()) JSONObject().toString() else JSONObject(
                                image.tags
                            ).toString()
                        image.debugData =
                            if (image.debugData.isNullOrEmpty()) JSONObject().toString() else JSONObject(
                                image.debugData
                            ).toString()

                        val gotPresigned = getPresigned(image, uploadType)

                        if (!gotPresigned)
                            continue

                        when (image.image_category) {
                            "Exterior",
                            "Interior",
                            "Focus Shoot",
                            "360int",
                            "Info" -> {
                                val imageUploaded = uploadImage(image)

                                if (!imageUploaded)
                                    continue

                                val imageMarkedDone = markDoneImage(image)
                                continue
                            }
                            else -> {
                                val bitmap =
                                    modifyOrientation(
                                        BitmapFactory.decodeFile(image.path),
                                        image.path
                                    )

                                try {
                                    File(outputDirectory).mkdirs()
                                    val outputFile = File(
                                        outputDirectory + System.currentTimeMillis()
                                            .toString() + ".jpg"
                                    )
                                    outputFile.createNewFile()

                                    val os: OutputStream = BufferedOutputStream(
                                        FileOutputStream(outputFile)
                                    )
                                    bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, os)
                                    os.close()

                                    image.path = outputFile.path

                                    val imageUploaded = uploadImage(image)

                                    if (!imageUploaded)
                                        continue

                                    val imageMarkedDone = markDoneImage(image)
                                    Log.d(
                                        TAG, "startUploading: imageMarkedDone " + imageMarkedDone
                                    )
                                    continue

                                } catch (
                                    e: Exception
                                ) {
                                    captureEvent(
                                        Events.IMAGE_ROTATION_EXCEPTION,
                                        image,
                                        false,
                                        e.localizedMessage
                                    )
                                    retryCount++
                                    continue
                                }
                            }
                        }
                    }
                } else {
                    if (image.imageId == null) {
                        captureEvent(
                            AppConstants.IMAGE_ID_NULL,
                            image,
                            true,
                            null
                        )
                        localRepository.markDone(image.uuid)
                        retryCount = 0
                        continue
                    } else {
                        val imageMarkedDone = markDoneImage(image)
                        if (imageMarkedDone)
                            retryCount = 0

                        continue

                    }
                }
            }
        } while (image != null)

        //upload images clicked while service uploading skipped images
        if (!connectionLost){
            deleteTempFiles(File(outputDirectory))
            listener.onCompleted("All images uploaded",ServerSyncTypes.UPLOAD)
            this@ImageUploader.isActive = false
        }
    }

    private suspend fun getPresigned(image: Image, uploadType: String): Boolean {
        image.name = if (image.image_category == "360int")
            image.skuName?.uppercase() + "_" + image.skuId + "_360int_1.JPG"
        else{
            image.skuName?.uppercase()+"_"+image.skuId+"_"+image.image_category+"_"+image.sequence+ "." + image.path.substringAfter(
                "."
            )
        }

        var response = shootRepository.getPreSignedUrl(
            uploadType,
            image
        )

        captureEvent(
            Events.GET_PRESIGNED_CALL_INITIATED, image, true, null,
            retryCount = retryCount
        )

        when (response) {
            is Resource.Failure -> {
                captureEvent(
                    Events.GET_PRESIGNED_FAILED,
                    image,
                    false,
                    getErrorMessage(response),
                    response = Gson().toJson(response).toString(),
                    retryCount = retryCount,
                    throwable = response.throwable
                )

                retryCount++
                return false
            }
        }

        val imagePreSignedRes = (response as Resource.Success).value

        image.preSignedUrl = imagePreSignedRes.data.presignedUrl
        image.imageId = imagePreSignedRes.data.imageId

        captureEvent(
            Events.GOT_PRESIGNED_IMAGE_URL, image,
            true,
            null,
            response = Gson().toJson(response.value).toString(),
            retryCount = retryCount
        )

        val count = localRepository.addPreSignedUrl(image)

        captureEvent(
            Events.IS_PRESIGNED_URL_UPDATED,
            localRepository.getImage(image.uuid!!),
            true,
            null,
            count,
            retryCount = retryCount
        )

        return true
    }

    private suspend fun uploadImage(image: Image): Boolean {
        val requestFile = File(image.path)
        val compressedImageFile = Compressor.compress(context, requestFile)

        val uploadResponse = shootRepository.uploadImageToGcp(
            image.preSignedUrl!!,
            compressedImageFile.asRequestBody("text/x-markdown; charset=utf-8".toMediaTypeOrNull()))

        val imageProperties = HashMap<String, Any?>()
            .apply {
                put("sku_id", image.skuId)
                put("iteration_id", lastIdentifier)
                put("upload_type", imageType)
                put("retry_count", retryCount)
                put("data", Gson().toJson(image))
            }

        context.captureEvent(
            Events.UPLOADING_TO_GCP_INITIATED,
            imageProperties
        )

        when (uploadResponse) {
            is Resource.Failure -> {
                captureEvent(
                    Events.IMAGE_UPLOAD_TO_GCP_FAILED,
                    image,
                    false,
                    getErrorMessage(uploadResponse),
                    response = Gson().toJson(uploadResponse).toString(),
                    retryCount = retryCount,
                    throwable = uploadResponse.throwable
                )
                retryCount++
                return false
            }
        }

        captureEvent(
            Events.IMAGE_UPLOADED_TO_GCP, image,
            true,
            null,
            response = Gson().toJson(uploadResponse).toString(),
            retryCount = retryCount
        )

        val markUploadCount = localRepository.markUploaded(image.uuid)

        captureEvent(
            Events.IS_MARK_GCP_UPLOADED_UPDATED,
            localRepository.getImage(image.uuid!!),
            true,
            null,
            markUploadCount,
            retryCount = retryCount
        )

        return true
    }

    private suspend fun markDoneImage(image: Image): Boolean {
        val markUploadResponse =
            shootRepository.markUploaded(image.imageId!!)

        captureEvent(
            Events.MARK_DONE_CALL_INITIATED, image, true, null,
            retryCount = retryCount
        )

        when (markUploadResponse) {
            is Resource.Failure -> {
                captureEvent(
                    Events.MARK_IMAGE_UPLOADED_FAILED,
                    image,
                    false,
                    getErrorMessage(markUploadResponse),
                    response = Gson().toJson(markUploadResponse).toString(),
                    retryCount = retryCount,
                    throwable = markUploadResponse.throwable
                )
                retryCount++
                return false
            }
        }

        captureEvent(
            Events.MARKED_IMAGE_UPLOADED, image, true,
            null,
            response = Gson().toJson(markUploadResponse).toString()
        )

        val count = localRepository.markStatusUploaded(image.uuid)

        captureEvent(
            Events.IS_MARK_DONE_STATUS_UPDATED,
            localRepository.getImage(image.uuid!!),
            true,
            null,
            count,
            retryCount = retryCount
        )
        retryCount = 0
        return true
    }

    private fun getErrorMessage(response: Resource.Failure): String {
        return if (response.errorMessage == null) response.errorCode.toString() + ": Http exception from server" else response.errorCode.toString() + ": " + response.errorMessage
    }


    @Throws(IOException::class)
    fun modifyOrientation(bitmap: Bitmap, image_absolute_path: String?): Bitmap? {
        val ei = ExifInterface(image_absolute_path!!)
        val orientation =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        return when (orientation) {
            ExifInterface.ORIENTATION_UNDEFINED,
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

    private fun captureEvent(
        eventName: String,
        image: Image,
        isSuccess: Boolean,
        error: String?,
        dbUpdateStatus: Int = 0,
        response: String? = null,
        retryCount: Int = 0,
        throwable: String? = null
    ) {
        val properties = HashMap<String, Any?>()
            .apply {
                put("sku_id", image.skuId)
                put("iteration_id", lastIdentifier)
                put("db_update_status", dbUpdateStatus)
                put("data", Gson().toJson(image))
                put("response", response)
                put("retry_count", retryCount)
                put("throwable", throwable)
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

    private fun deleteTempFiles(file: File): Boolean {
        if (file.isDirectory) {
            val files = file.listFiles()
            if (files != null) {
                for (f in files) {
                    if (f.isDirectory) {
                        deleteTempFiles(f)
                    } else {
                        f.delete()
                    }
                }
            }
        }
        return file.delete()
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


    val outputDirectory = "/storage/emulated/0/DCIM/Spynetemp/"


    private fun getRandomNumberInRange(): Int {
        val r = Random()
        return r.nextInt(100 - 10 + 1) + 10
    }

}