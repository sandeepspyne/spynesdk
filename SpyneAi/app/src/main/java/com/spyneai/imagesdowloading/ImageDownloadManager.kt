package com.spyneai.imagesdowloading

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.downloader.*
import com.spyneai.R
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.model.credit.UpdateCreditResponse
import com.spyneai.model.processImageService.Task
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_downloading.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ImageDownloadManager(var task : DownloadTask, var listener : Listener) : AppCompatActivity() {

    var path_save_photos: String = ""
    lateinit var file: File

    fun start() {
        if (task.listHdQuality.size > 0 && task.listHdQuality != null) {

            for (i in 0 until task.listHdQuality.size) {
              if (task.listHdQuality[i] == null){

              } else{
                  downloadWithHighQuality(task.listHdQuality[i])
              }

            }
        }
    }
    //Download
    private fun downloadWithHighQuality(imageFile: String?) {
        val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

        val imageName: String = "Spyne" + SimpleDateFormat(
            FILENAME_FORMAT, Locale.US
        ).format(System.currentTimeMillis()) + ".png"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            path_save_photos = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + R.string.app_name;
        }else{
            path_save_photos = Environment.getExternalStorageDirectory().getAbsolutePath() +
                    File.separator +
                    this.getResources().getString(R.string.app_name)
        }

        file = File(path_save_photos)

        PRDownloader.download(
            imageFile,
            path_save_photos,
            imageName
        )
            .build()
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    listener.onScan(file.absolutePath + "/" + imageName)
                    task.downloadCount++

                    if (task.downloadCount == task.listHdQuality.size) {
                        listener.onSuccess(task)
                    }
                }

                override fun onError(error: com.downloader.Error?) {
                    Log.d("ImageDownloadManager", "onError: ")
                    if (!task.failureNotified){
                        task.failureNotified = true
                        listener.onFailure(task)
                    }
                }

            })
    }

    interface Listener {
        fun onSuccess(task: DownloadTask)
        fun onScan(filePath : String)
        fun onRefresh(filePath : String)
        fun onFailure(task: DownloadTask)
    }
}