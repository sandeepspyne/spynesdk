package com.spyneai.threesixty.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.FileProvider
import com.abedelazizshe.lightcompressorlibrary.CompressionListener
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.abedelazizshe.lightcompressorlibrary.VideoQuality
import com.abedelazizshe.lightcompressorlibrary.config.Configuration
import com.google.gson.Gson
import com.spyneai.*
import com.spyneai.base.network.ClipperApi
import com.spyneai.base.network.Resource
import com.spyneai.base.room.AppDatabase
import com.spyneai.interfaces.GcpClient
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.service.DataSyncListener
import com.spyneai.service.ImageUploader
import com.spyneai.shoot.data.ImagesRepoV2
import com.spyneai.shoot.data.ShootRepository
import com.spyneai.threesixty.data.model.PreSignedVideoBody
import com.spyneai.threesixty.data.model.VideoDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*
import kotlin.collections.HashMap
import kotlin.coroutines.suspendCoroutine


class VideoUploader(val context: Context,
                    val localRepository : VideoLocalRepoV2,
                    val threeSixtyRepository: ThreeSixtyRepository,
                    var listener: Listener,
                    var lastIdentifier : String = "0",
                    var videoType: String = AppConstants.REGULAR,
                    var retryCount: Int = 0,
                    var connectionLost: Boolean = false,
                    var isActive: Boolean = false
) {

    companion object{
        @Volatile
        private var INSTANCE: VideoUploader? = null

        fun getInstance(context: Context,listener: Listener): VideoUploader {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = VideoUploader(
                        context,
                        VideoLocalRepoV2(AppDatabase.getInstance(BaseApplication.getContext()).videoDao()),
                        ThreeSixtyRepository(),
                        listener
                    )

                    INSTANCE = instance
                }
                return instance
            }
        }
    }

    fun uploadParent(type : String,startedBy : String?) {
        context.captureEvent(Events.VIDEO_UPLOAD_PARENT_TRIGGERED,HashMap<String,Any?>().apply {
            put("type",type)
            put("service_started_by",startedBy)
            put("upload_running",isActive)
        })

        //update triggered value
        Utilities.saveBool(context, AppConstants.VIDEO_UPLOAD_TRIGGERED, true)

        val handler = Handler(Looper.getMainLooper())

        handler.postDelayed({
            if (Utilities.getBool(context, AppConstants.VIDEO_UPLOAD_TRIGGERED, true)
                &&
                !isActive
            ) {
                if (context.isInternetActive())
                    GlobalScope.launch(Dispatchers.Default) {
                        Log.d(TAG, "uploadParent: start")
                        isActive = true
                        context.captureEvent("START VIDEO UPLOADING CALLED",HashMap())
                        startUploading()
                    }
                else {
                    isActive = false
                    listener.onConnectionLost()
                    Log.d(TAG, "uploadParent: connection lost")
                }
            }
        }, getRandomNumberInRange().toLong())
    }

    private suspend fun startUploading() {
        do {
            lastIdentifier = getUniqueIdentifier()

            val remaingData = HashMap<String,Any?>()
                .apply {
                    put("remaining_videos", JSONObject().apply {
                        put("upload_remaining",localRepository.totalRemainingUpload())
                        put("mark_done_remaining",localRepository.totalRemainingMarkDone())
                    }.toString())
                }


            if (connectionLost){
                context.captureEvent(
                    Events.VIDEO_CONNECTION_BREAK,
                    remaingData
                )
                isActive = false
                listener.onConnectionLost()
                break
            }

            var video = localRepository.getOldestVideo()


            val s = ""

            if (video == null){
                context.captureEvent(
                    Events.ALL_VIDEO_UPLOADED_BREAK,
                    HashMap<String,Any?>()
                        .apply {
                            put("remaining_videos", JSONObject().apply {
                                put("upload_remaining",localRepository.totalRemainingUpload())
                                put("mark_done_remaining",localRepository.totalRemainingMarkDone())
                            }.toString())
                        }
                )
                break
            }
            else {
                lastIdentifier = video.skuName+ "_" + video.skuId

                val videoProperties = HashMap<String, Any?>()
                    .apply {
                        put("sku_id", video.skuId)
                        put("iteration_id", lastIdentifier)
                        put("retry_count", retryCount)
                        put("upload_type", videoType)
                        put("data", Gson().toJson(video))
                        put("remaining_videos",JSONObject().apply {
                            put("upload_remaining",localRepository.totalRemainingUpload())
                            put("mark_done_remaining",localRepository.totalRemainingMarkDone())
//                            put("remaining_above", localRepository.getRemainingAbove(video.itemId!!))
//                            put("remaining_above_skipped", localRepository.getRemainingAboveSkipped(video.itemId!!))
//                            put("remaining_below", localRepository.getRemainingBelow(video.itemId!!))
//                            put("remaining_below_skipped", localRepository.getRemainingBelowSkipped(video.itemId!!))
                        }.toString())

                    }

                context.captureEvent(
                    Events.VIDEO_SELECTED,
                    videoProperties
                )

                listener.inProgress(video)
                isActive = true

                if (retryCount > 4) {
                    val dbStatus =  localRepository.skipVideo(
                        video.uuid,
                        video.toProcessAT.plus( video.retryCount * AppConstants.RETRY_DELAY_TIME)
                    )

                    captureEvent(
                        Events.VIDEO_MAX_RETRY,
                        video,
                        false,
                        "video upload limit reached",
                        dbStatus
                    )
                    retryCount = 0
                    continue
                }

                if (!video.isUploaded) {
                    if (video.preSignedUrl != null) {
                        val videoUploaded = uploadvideo(video)

                        if (!videoUploaded)
                            continue

                        markDoneVideo(video)

                        continue
                    } else {
                        val gotPresigned = getPresigned(video)

                        if (!gotPresigned)
                            continue

                        val videoUploaded = uploadvideo(video)

                        if (!videoUploaded)
                            continue

                        val videoMarkedDone = markDoneVideo(video)
                        continue
                    }
                } else {
                    if (video.videoId == null) {
                        captureEvent(
                            Events.VIDEO_ID_NULL,
                            video,
                            true,
                            null
                        )
                        video.isUploaded = true
                        video.isMarkedDone = true
                        localRepository.updateVideo(video)
                        retryCount = 0
                        continue
                    } else {
                        val videoMarkedDone = markDoneVideo(video)
                        if (videoMarkedDone)
                            retryCount = 0

                        continue

                    }
                }
            }
        }while (video != null)

        if (!connectionLost){
            listener.onUploaded()
            isActive = false
        }
    }

    private suspend fun getPresigned(video: VideoDetails): Boolean {
        val properties = HashMap<String, Any>()
        //upload video
        val response = threeSixtyRepository.getVideoPreSignedUrl(
                  properties.apply {
                      if (video.backgroundId != null) {
                          put("auth_key", Utilities.getPreference(context, AppConstants.AUTH_KEY).toString())
                          put("project_id", video.projectId!!)
                          put("sku_id", video.skuId!!)
                          put("category", video.categoryName)
                          put("sub_category", AppConstants.CARS_CATEGORY_ID)
                          put("total_frames_no", video.frames)
                          put("video_name", File(video.videoPath).name)
                          put("background_id", video.backgroundId.toString())
                      } else {
                          put("auth_key", Utilities.getPreference(context, AppConstants.AUTH_KEY).toString())
                          put("project_id", video.projectId!!)
                          put("sku_id", video.skuId!!)
                          put("category", video.categoryName)
                          put("sub_category", AppConstants.CARS_CATEGORY_ID)
                          put("total_frames_no", video.frames)
                          put("video_name", File(video.videoPath).name)
                      }
                  }
        )

                    captureEvent(
                        Events.GET_VIDEO_PRESIGNED_CALL_INITIATED, video, true, null,
                        retryCount = retryCount
                    )

                    when (response) {
                        is Resource.Failure -> {
                            captureEvent(
                                Events.GET_VIDEO_PRESIGNED_FAILED,
                                video,
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

                    video.preSignedUrl = imagePreSignedRes.data.presignedUrl
                    video.videoId = imagePreSignedRes.data.videoId

                    captureEvent(
                        Events.GOT_VIDEO_PRESIGNED_VIDEO_URL, video,
                        true,
                        null,
                        response = Gson().toJson(response.value).toString(),
                        retryCount = retryCount
                    )

                    val count = localRepository.updateVideo(video)

                    captureEvent(
                        Events.IS_VIDEO_PRESIGNED_URL_UPDATED,
                        localRepository.getVideo(video.uuid),
                        true,
                        null,
                        count,
                        retryCount = retryCount
                    )

                    return true
                }


    private suspend fun compressVideo(videoUri: Uri, video_detail:VideoDetails): RequestBody =
        suspendCoroutine { continuation ->
            try {
                VideoCompressor.start(
                    context = context, // => This is required
                    uris = listOf(videoUri), // => Source can be provided as content uris
                    isStreamable = true,
                    saveAt = Environment.DIRECTORY_MOVIES, // => the directory to save the compressed video(s)
                    listener = object : CompressionListener {
                        override fun onProgress(index: Int, percent: Float) {
                            Log.d(TAG, "onProgress:$percent")
                            context.captureEvent( Events.VIDEO_COMPRESSION_PROGRESS,
                                HashMap<String, Any?>()
                                    .apply {
                                        put("sku_id", video_detail.skuId)
                                        put("COMPRESSION_PROGRESS",percent)
                                        put("VIDEO_URI",videoUri)
                                    })
                        }

                        override fun onStart(index: Int) {
                            Log.d(TAG, "Compression Start")
                            context.captureEvent(Events.VIDEO_COMPRESSION_START,
                                HashMap<String, Any?>()
                                    .apply {
                                        put("sku_id", video_detail.skuId)
                                        put("COMPRESSION_START",videoUri)
                                    })

                        }

                        override fun onSuccess(index: Int, size: Long, path: String?) {
                            Log.d(TAG, "Compression Complete$path")
                            val requestFile = File(path).asRequestBody("text/x-markdown; charset=utf-8".toMediaTypeOrNull())
                            context.captureEvent(Events.VIDEO_COMPRESSION_SUCCESS,
                                HashMap<String, Any?>()
                                    .apply {
                                        put("sku_id", video_detail.skuId)
                                        put("COMPRESSION_SUCCESS",videoUri)
                                        put("RESULT_REQUEST_FILE",requestFile)
                                    })

                            continuation.resumeWith(Result.success(requestFile))


                        }

                        override fun onFailure(index: Int, failureMessage: String) {
                            Log.d(TAG, "Compression Fail$failureMessage")
                            context.captureEvent(Events.VIDEO_COMPRESSION_FAIL,
                                HashMap<String, Any?>()
                                    .apply {
                                        put("sku_id", video_detail.skuId)
                                        put("VIDEO_URI",videoUri)
                                        put("FAIL_MESSAGE",failureMessage)
                                    })
                        }

                        override fun onCancelled(index: Int) {
                            Log.d(TAG, "Compression Canceled")
                            context.captureEvent( Events.VIDEO_COMPRESSION_CANCEL,
                                HashMap<String, Any?>()
                                    .apply {
                                        put("sku_id", video_detail.skuId)
                                        put("VIDEO_URI",videoUri)
                                    })
                        }

                    },
                    configureWith = Configuration(
                        quality = VideoQuality.MEDIUM,
//                    frameRate = 24, /*Int, ignore, or null*/
                        isMinBitrateCheckEnabled = false,
//                    videoBitrate = 3677198, /*Int, ignore, or null*/
                        disableAudio = true, /*Boolean, or ignore*/
                        keepOriginalResolution = true, /*Boolean, or ignore*/
                        videoWidth = 1920.0, /*Double, ignore, or null*/
                        videoHeight = 1080.0 /*Double, ignore, or null*/
                    )
                )


            } catch (e: Exception) {
//                continuation.resumeWith(Result.failure(e))
                context.captureEvent(Events.VIDEO_COMPRESSED_EXCEPTION,
                    HashMap<String, Any?>()
                        .apply {
                            put("sku_id", video_detail.skuId)
                            put("VIDEO_URI",videoUri)
                            put("EXCEPTION",e)
                        })
                Log.d(TAG, "Compression Catch exception$e")
            }
        }

    private suspend fun uploadvideo(video: VideoDetails): Boolean {
        var requestFile:RequestBody?=null


        if(File(video.videoPath).exists()){
            try{
                val videoContentUri = FileProvider.getUriForFile(context, context.applicationContext.packageName + ".fileprovider", File(video.videoPath))
                context.captureEvent(Events.VIDEO_CONTENT_URI_CREATED,
                    HashMap<String, Any?>()
                        .apply {
                            put("sku_id", video.skuId)
                            put("video_content_uri_created", videoContentUri)
                            put("video", video)
                        }
                )
                requestFile= compressVideo(videoContentUri,video)

            }catch (e: Exception){
                if (e.localizedMessage.contains("No such file or directory")) {
                    video.isUploaded = true
                    video.isMarkedDone = true
                    localRepository.updateVideo(video)
                }
                context.captureEvent( Events.VIDEO_CONTENT_URI_CREATION_FAIL,
                    HashMap<String, Any?>()
                        .apply {
                            put("sku_id", video.skuId)
                            put("Exception", e)
                            put("video", video)
                        })
                e.printStackTrace()
                return false
            }
        }
        else{
            context.captureEvent(Events.VIDEO_FILE_NOT_EXIST,
                HashMap<String, Any?>()
                    .apply {
                        put("sku_id", video.skuId)
                        put("video", video)
                    })
            video.isUploaded = true
            video.isMarkedDone = true
            localRepository.updateVideo(video)
            return false
        }

        val uploadResponse = threeSixtyRepository.uploadVideoToGcp(
            video.preSignedUrl!!,
            requestFile
        )

        val imageProperties = HashMap<String, Any?>()
            .apply {
                put("sku_id", video.skuId)
                put("iteration_id", lastIdentifier)
                put("upload_type", videoType)
                put("retry_count", retryCount)
                put("data", Gson().toJson(video))
            }

        context.captureEvent(
            Events.VIDEO_UPLOADING_TO_GCP_INITIATED,
            imageProperties
        )

        when (uploadResponse) {
            is Resource.Failure -> {
                captureEvent(
                    Events.VIDEO_UPLOAD_TO_GCP_FAILED,
                    video,
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
            Events.VIDEO_UPLOADED_TO_GCP,
            video,
            true,
            null,
            response = Gson().toJson(uploadResponse).toString(),
            retryCount = retryCount
        )

        video.isUploaded = true
        val markUploadCount = localRepository.updateVideo(video)

        captureEvent(
            Events.IS_VIDEO_GCP_UPLOADED_UPDATED,
            localRepository.getVideo(video.uuid),
            true,
            null,
            markUploadCount,
            retryCount = retryCount
        )

        return true
    }

    private suspend fun markDoneVideo(video: VideoDetails): Boolean {
        val markUploadResponse = threeSixtyRepository.setStatusUploaded(video.videoId!!)

        captureEvent(
            Events.VIDEO_MARK_DONE_CALL_INITIATED,
            video, true,
            null,
            retryCount = retryCount
        )

        when (markUploadResponse) {
            is Resource.Failure -> {
                captureEvent(
                    Events.MARK_VIDEO_UPLOADED_FAILED,
                    video,
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
            Events.VIDEO_MARKED_UPLOADED, video, true,
            null,
            response = Gson().toJson(markUploadResponse).toString()
        )

        video.isMarkedDone = true
        val count = localRepository.updateVideo(video)

        captureEvent(
            Events.IS_VIDEO_MARK_DONE_STATUS_UPDATED,
            localRepository.getVideo(video.uuid),
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

    private fun captureEvent(eventName : String,
                             video : VideoDetails,
                             isSuccess : Boolean,
                             error: String?,
                             dbUpdateStatus: Int = 0,
                             response: String? = null,
                             retryCount: Int = 0,
                             throwable: String? = null) {
        val properties = HashMap<String,Any?>()
        properties.apply {
            this["sku_id"] = video.skuId
            this["sku_name"] = video.skuName
            this["project_id"] = video.projectId
            this["video_id"] = video.videoId
            this["pre_signed_url"] = video.preSignedUrl
            put("video_local_id",video.uuid)
            put("iteration_id",lastIdentifier)
            put("frames",video.frames)
            put("project_id",video.projectId)
            put("sku_id",video.skuId)
            put("sku_name",video.skuName)
            put("upload_status",video.isUploaded)
            put("make_done_status",video.isMarkedDone)
            put("video_path",video.videoPath)
            put("video_type",video.categoryName)
            put("db_update_status",dbUpdateStatus)
            put("response",response)
            put("retry_count",retryCount)
            put("throwable",throwable)
        }

        if (isSuccess) {
            context.captureEvent(
                eventName,
                properties)
        }else{
            context.captureFailureEvent(
                eventName,
                properties, error!!
            )
        }
    }


    private fun getRandomNumberInRange(): Int {
        val r = Random()
        return r.nextInt(100 - 10 + 1) + 10
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

    interface Listener {
        fun inProgress(task: VideoDetails)
        fun onUploaded()
        fun onUploadFail(task: VideoDetails)
        fun onConnectionLost()
    }
}