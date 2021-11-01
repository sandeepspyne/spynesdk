package com.spyneai.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.work.*
import com.spyneai.*
import com.spyneai.base.network.ClipperApi
import com.spyneai.base.network.Resource
import com.spyneai.interfaces.GcpClient
import com.spyneai.needs.AppConstants
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ImageLocalRepository
import com.spyneai.shoot.data.ShootRepository
import com.spyneai.shoot.data.model.Image
import com.spyneai.shoot.utils.logUpload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*

class ImageUploader(val context: Context,
                    val localRepository : ImageLocalRepository,
                    val shootRepository: ShootRepository,
                    var listener: Listener) {

    val TAG = "ImageUploader"

    fun start() {
        selectLastImageAndUpload(AppConstants.REGULAR,0)
    }

    private fun selectLastImageAndUpload(imageType : String,retryCount : Int) {

       if (context.isInternetActive()){
           GlobalScope.launch(Dispatchers.Default) {

               var skipFlag = -1
               val image = if (imageType == AppConstants.REGULAR){
                   localRepository.getOldestImage("0")
               } else{
                   skipFlag = -2
                   localRepository.getOldestImage("-1")
               }

               if (image.itemId != null){
                   //uploading enqueued

                   Log.d(TAG, "selectLastImageAndUpload: "+retryCount)
                   Log.d(TAG, "selectLastImageAndUpload: id "+image.itemId)
                   Log.d(TAG, "selectLastImageAndUpload: reclick "+image.isReclick)
                   Log.d(TAG, "selectLastImageAndUpload: path "+image.imagePath)
                   Log.d(TAG, "selectLastImageAndUpload: uploaded "+image.isUploaded)
                   Log.d(TAG, "selectLastImageAndUpload: status updated "+image.isStatusUpdated)
                   listener.inProgress(image)

                   if (retryCount > 4) {
                       if (image.itemId != null){

                           localRepository.skipImage(image.itemId!!,skipFlag)
                           startNextUpload(image.itemId!!,false,imageType)
                       }
                       captureEvent(Events.UPLOAD_FAILED_SERVICE,image,false,"Image upload limit reached")
                       logUpload("Upload Skipped Retry Limit Reached")
                       return@launch
                   }

                   if (image.isUploaded == 0 || image.isUploaded == -1){

                       if (image.preSignedUrl != AppConstants.DEFAULT_PRESIGNED_URL){
                           uploadImageToGcp(image,imageType,retryCount)
                       }else {
                           val uploadType = if (retryCount == 0) "Direct" else "Retry"

                           var response = shootRepository.getPreSignedUrl(
                               uploadType,
                               image
                           )

                           when(response){
                               is Resource.Success -> {
                                   image.preSignedUrl = response.value.data.presignedUrl
                                   image.imageId = response.value.data.imageId

                                   //captureEvent(Events.GOT_PRESIGNED_VIDEO_URL,video,true,null)

                                   localRepository.addPreSignedUrl(image)

                                   uploadImageToGcp(image,imageType,retryCount)
                               }

                               is Resource.Failure -> {
                                   log("Image upload failed")
                                   logUpload("Upload error "+response.errorCode.toString()+" "+response.errorMessage)
                                   if(response.errorMessage == null){
                                       captureEvent(Events.UPLOAD_FAILED_SERVICE,image,false,response.errorCode.toString()+": Http exception from server")
                                   }else {
                                       captureEvent(Events.UPLOAD_FAILED_SERVICE,image,false,response.errorCode.toString()+": "+response.errorMessage)
                                   }

                                   selectLastImageAndUpload(imageType,retryCount+1)
                               }
                           }
                       }
                   }else{
                       setStatusUploaed(image,imageType,retryCount)
                   }

               }else{
                   logUpload("All Images uploaded")
                   if (imageType == AppConstants.REGULAR){
                       //start skipped images worker
                       logUpload("Start Skipped Images uploaded")
                      selectLastImageAndUpload(AppConstants.SKIPPED,0)
                   }else{
                       //make second time skipped images elligible for upload
                       val count = localRepository.updateSkipedImages()

                       //check if we don"t have any new image clicked while uploading skipped images
                      if (localRepository.getOldestImage("0").itemId == null){
                          if (count > 0){
                              //upload double skipped images if we don't have any new image
                              selectLastImageAndUpload(AppConstants.SKIPPED,0)
                          }
                          else
                              listener.onUploaded(image)
                      } else{
                          //upload images clicked while service uploading skipped images
                          selectLastImageAndUpload(AppConstants.REGULAR,0)
                      }
                   }
               }
           }
       }else {
           listener.onConnectionLost()
       }
    }

    private fun uploadImageToGcp(image: Image,imageType : String,retryCount : Int) {

        var path = image.imagePath
        if (image.categoryName != "Automobiles" && image.categoryName != "Bikes"){
            //rotate image
            val bitmap =  modifyOrientation( BitmapFactory.decodeFile(path) ,path)

            val outputDirectory: String by lazy {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    "${Environment.DIRECTORY_DCIM}/SpyneTemp/"
                } else {
                    "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)}/SpyneTemp/"
                }
            }

            File(outputDirectory).mkdirs()

            try {
                val file = File(outputDirectory+System.currentTimeMillis()+".jpg")
                val isC = file.createNewFile()
                val os: OutputStream = BufferedOutputStream(FileOutputStream(file))
                bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, os)
                os.close()

                path = file.path
            }catch (
                e : Exception
            ){
                val s = ""
            }
        }

        // create RequestBody instance from file
        val requestFile =
            File(path).asRequestBody("text/x-markdown; charset=utf-8".toMediaTypeOrNull())

        //upload video with presigned url
        val request = GcpClient.buildService(ClipperApi::class.java)

        val call = request.uploadVideo(
            "application/octet-stream",
            image.preSignedUrl!!,
            requestFile
        )

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d("VideoUploader", "onResponse: "+response.code())
                if (response.isSuccessful){
                    localRepository.markUploaded(image)
                    onVideoUploaded(
                        image,
                        imageType,
                        retryCount
                    )
                }else {
                    onVideoUploadFailed(
                        imageType,
                        retryCount,
                        image,
                        response.errorBody().toString()
                    )
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("VideoUploader", "onFailure: "+t.message)
                onVideoUploadFailed(
                    imageType,
                    retryCount,
                    image,
                    t.message
                )
            }

        })
    }

    @Throws(IOException::class)
    fun modifyOrientation(bitmap: Bitmap, image_absolute_path: String?): Bitmap? {
        val ei = ExifInterface(image_absolute_path!!)
        val orientation =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        return when (orientation) {
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
        matrix.preScale((if (horizontal) -1 else 1.toFloat()) as Float,
            (if (vertical) -1 else 1.toFloat()) as Float
        )
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }


    private fun onVideoUploaded(video: Image, imageType: String, retryCount : Int) {
        captureEvent(Events.VIDEO_UPLOADED_TO_GCP,video,true,null)

        GlobalScope.launch(Dispatchers.Default) {
            setStatusUploaed(video,imageType,retryCount)
        }
    }

    private fun onVideoUploadFailed(imageType: String,retryCount: Int,video: Image,error: String?) {
        captureEvent(Events.VIDEO_UPLOAD_TO_GCP_FAILED,video,false,error)

        GlobalScope.launch(Dispatchers.Default) {
            selectLastImageAndUpload(imageType,retryCount+1)
        }
    }

    private suspend fun setStatusUploaed(video: Image,imageType: String,retryCount : Int) {
           val response = shootRepository.markUploaded(video.imageId!!)

           when(response){
               is Resource.Success -> {
                   captureEvent(Events.MARKED_VIDEO_UPLOADED,video,true,null)
                   localRepository.markStatusUploaded(video)
                   selectLastImageAndUpload(imageType,0)
               }

               is Resource.Failure -> {
                   if(response.errorMessage == null){
                       captureEvent(Events.MARK_VIDEO_UPLOADED_FAILED,video,false,response.errorCode.toString()+": Http exception from server")
                   }else {
                       captureEvent(Events.MARK_VIDEO_UPLOADED_FAILED,video,false,response.errorCode.toString()+": "+response.errorMessage)
                   }

                   selectLastImageAndUpload(imageType,retryCount+1)
               }
           }

    }

    private fun startNextUpload(itemId: Long,uploaded : Boolean,imageType : String) {
       logUpload("Start next upload "+uploaded)
        //remove uploaded item from database
        if (uploaded)
            localRepository.deleteImage(itemId)

        selectLastImageAndUpload(imageType,0)
    }

    private fun captureEvent(eventName : String, image : Image, isSuccess : Boolean, error: String?) {
        val properties = HashMap<String,Any?>()
        properties.apply {
            this["sku_id"] = image.skuId
            this["project_id"] = image.projectId
            this["image_type"] = image.categoryName
            this["sequence"] = image.sequence
           // this["retry_count"] = runAttemptCount
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


    interface Listener {
        fun inProgress(task: Image)
        fun onUploaded(task: Image)
        fun onUploadFail(task: Image)
        fun onConnectionLost()
    }

}