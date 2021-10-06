package com.spyneai.imagesdowloading

import android.os.Build
import android.os.Environment
import android.util.Log
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.spyneai.BaseApplication
import com.spyneai.R
import java.io.File

class ImageDownloadManager(var task : DownloadTask, var listener : Listener) {

    var path_save_photos: String = ""

    fun start() {
        if (task.listHdQuality.size > 0 && task.listHdQuality != null) {

            for (i in 0 until task.listHdQuality.size) {
              if (task.listHdQuality[i] == null){

              } else{
                  downloadWithHighQuality(task.listHdQuality[i],task.imageNameList[i])
              }

            }
        }
    }
    //Download
    private fun downloadWithHighQuality(imageFile: String,imageName : String) {

        var imageName = imageName
        if (imageName.length > 4 && imageName.takeLast(4) != ".jpg")
        {
            imageName += if (task.isHd)
                ".jpg"
            else
                "_watermark.jpg"
        }else {
            if (!task.isHd  && imageName.length > 4)
                imageName = imageName.dropLast(4) + "_watermark.jpg"
        }





        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            path_save_photos = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + R.string.app_name;
        }else{
            path_save_photos = Environment.getExternalStorageDirectory().getAbsolutePath() +
                    File.separator +
                    BaseApplication.getContext().getResources().getString(R.string.app_name)
        }

        var file = File(path_save_photos)

        //delete existing file
        if (File(path_save_photos).exists())
            File(path_save_photos).delete()

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
                    Log.d("ImageDownloadManager", "onError: "+error.toString())
                    if (error?.connectionException != null && error.connectionException.message == "Rename Failed"){
                        task.downloadCount++

                        if (task.downloadCount == task.listHdQuality.size) {
                            listener.onSuccess(task)
                        }
                    }else{
                        if (!task.failureNotified){
                            task.failureNotified = true
                            listener.onFailure(task)
                        }
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