package com.spyneai.threesixty.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.spyneai.*
import com.spyneai.base.network.ClipperApi
import com.spyneai.base.network.Resource
import com.spyneai.interfaces.GcpClient
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.threesixty.data.model.PreSignedVideoBody
import com.spyneai.threesixty.data.model.VideoDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
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


class VideoUploader(val context: Context,
                    val localRepository : VideoLocalRepository,
                    val threeSixtyRepository: ThreeSixtyRepository,
                    var listener: Listener,
                    var lastIdentifier : String = "0",
                    var videoType: String = AppConstants.REGULAR,
                    var retryCount: Int = 0,
                    var connectionLost: Boolean = false
) {

    fun uploadParent(type : String,startedBy : String?) {
        context.captureEvent(Events.VIDEO_UPLOAD_PARENT_TRIGGERED,HashMap<String,Any?>().apply {
            put("type",type)
            put("service_started_by",startedBy)
            put("upload_running",Utilities.getBool(context, AppConstants.VIDEO_VIDEO_UPLOADING_RUNNING, false))
        })

        //update triggered value
        Utilities.saveBool(context, AppConstants.VIDEO_UPLOAD_TRIGGERED, true)

        val handler = Handler(Looper.getMainLooper())

        handler.postDelayed({
            if (Utilities.getBool(context, AppConstants.VIDEO_UPLOAD_TRIGGERED, true)
                &&
                !Utilities.getBool(context, AppConstants.VIDEO_UPLOADING_RUNNING, false)
            ) {
                if (context.isInternetActive())
                    GlobalScope.launch(Dispatchers.Default) {
                        Log.d(TAG, "uploadParent: start")
                        Utilities.saveBool(context, AppConstants.VIDEO_UPLOADING_RUNNING, true)
                        context.captureEvent("START UPLOADING CALLED",HashMap())
                        startUploading()
                    }
                else {
                    Utilities.saveBool(context, AppConstants.VIDEO_UPLOADING_RUNNING, false)
                    listener.onConnectionLost()
                    Log.d(TAG, "uploadParent: connection lost")
                }
            }
        }, getRandomNumberInRange().toLong())
    }

    private suspend fun startUploading() {
        do {
            lastIdentifier = getUniqueIdentifier()

            if (connectionLost){
                context.captureEvent(
                    Events.VIDEO_CONNECTION_BREAK,
                    HashMap<String,Any?>()
                        .apply {
                            put("remaining_videos", JSONObject().apply {
                                put("upload_remaining",localRepository.totalRemainingUpload())
                                put("mark_done_remaining",localRepository.totalRemainingMarkDone())
                            }.toString())
                        }
                )
                Utilities.saveBool(context,AppConstants.VIDEO_UPLOADING_RUNNING,false)
                listener.onConnectionLost()
                break
            }

            var skipFlag = -1

            var video = localRepository.getOldestVideo("0")

            if (video.itemId == null) {
                videoType = AppConstants.SKIPPED
                video = localRepository.getOldestVideo("-1")
                skipFlag = -2
            }


            if (video.itemId == null && videoType == AppConstants.SKIPPED) {
                //make second time skipped videos elligible for upload
                val count = localRepository.updateSkippedVideos()
                val markDoneSkippedCount = localRepository.updateMarkDoneSkipedVideos()

                Log.d(TAG, "name: count"+count+" "+markDoneSkippedCount)

                //check if we don"t have any new video clicked while uploading skipped videos
                if (count > 0 || markDoneSkippedCount > 0)
                    video = localRepository.getOldestVideo("-1")
            }

            if (video.itemId == null){
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
            }else {
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
                            put("remaining_above", localRepository.getRemainingAbove(video.itemId!!))
                            put("remaining_above_skipped", localRepository.getRemainingAboveSkipped(video.itemId!!))
                            put("remaining_below", localRepository.getRemainingBelow(video.itemId!!))
                            put("remaining_below_skipped", localRepository.getRemainingBelowSkipped(video.itemId!!))
                        }.toString())

                    }

                context.captureEvent(
                    Events.VIDEO_SELECTED,
                    videoProperties
                )

                listener.inProgress(video)

                if (retryCount > 4) {
                    val dbStatus = if (video.isUploaded != 1)
                        localRepository.skipVideo(video.itemId!!, skipFlag)
                    else {
                        localRepository.skipMarkDoneFailedVideo(video.itemId!!)
                    }

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

                if (video.isUploaded == 0 || video.isUploaded == -1) {
                    if (video.preSignedUrl != AppConstants.DEFAULT_PRESIGNED_URL) {
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
                        localRepository.markUploaded(video)
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
            Utilities.saveBool(context, AppConstants.VIDEO_UPLOADING_RUNNING, false)
        }
    }

    private suspend fun getPresigned(video: VideoDetails): Boolean {
        //upload video
        val response = threeSixtyRepository.getVideoPreSignedUrl(
            PreSignedVideoBody(
                Utilities.getPreference(context,AppConstants.AUTH_KEY).toString(),
                video.projectId!!,
                video.skuId!!,
                video.categoryName,
                AppConstants.CARS_CATEGORY_ID,
                AppConstants.CARS_CATEGORY_ID,
                video.frames,
                File(video.videoPath).name,
                video.backgroundId.toString()
            )
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

        val count = localRepository.addPreSignedUrl(video)

        captureEvent(
            Events.IS_VIDEO_PRESIGNED_URL_UPDATED,
            localRepository.getVideo(video.itemId!!),
            true,
            null,
            count,
            retryCount = retryCount
        )

        return true
    }

    private suspend fun uploadvideo(video: VideoDetails): Boolean {
        val requestFile =
            File(video.videoPath).asRequestBody("text/x-markdown; charset=utf-8".toMediaTypeOrNull())

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

        val markUploadCount = localRepository.markUploaded(video)

        captureEvent(
            Events.IS_VIDEO_GCP_UPLOADED_UPDATED,
            localRepository.getVideo(video.itemId!!),
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

        val count = localRepository.markStatusUploaded(video)

        captureEvent(
            Events.IS_VIDEO_MARK_DONE_STATUS_UPDATED,
            localRepository.getVideo(video.itemId!!),
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
            put("video_local_id",video.itemId)
            put("iteration_id",lastIdentifier)
            put("frames",video.frames)
            put("project_id",video.projectId)
            put("sku_id",video.skuId)
            put("sku_name",video.skuName)
            put("upload_status",video.isUploaded)
            put("make_done_status",video.isStatusUpdate)
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