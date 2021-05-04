package com.spyneai.imagesdowloading

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import com.downloader.*
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

class ImageDownloadManager(var task : DownloadTask, var listener : Listener) {

    fun start() {
        if (task.listHdQuality.size > 0 && task.listHdQuality != null) {

            for (i in 0 until task.listHdQuality.size) {
              if (task.listHdQuality[i] != null)
                    downloadWithHighQuality(task.listHdQuality[i])
            }
        }
    }

    //Download
    private fun downloadWithHighQuality(imageFile: String?) {
        val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

        val imageName: String = "Spyne" + SimpleDateFormat(
            FILENAME_FORMAT, Locale.US
        ).format(System.currentTimeMillis()) + ".png"

        var file = File(Environment.getExternalStorageDirectory().toString() + "/Spyne")

       PRDownloader.download(
            imageFile,
            Environment.getExternalStorageDirectory().toString() + "/Spyne",
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