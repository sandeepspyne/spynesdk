package com.spyneai.shoot.workmanager

import android.content.Context
import android.text.TextUtils
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.TransferListener
import com.spyneai.shoot.ui.ShootViewModel
import java.io.File

class UploadImageWorker(appContext: Context, workerParams: WorkerParameters, shootViewModel: ShootViewModel):
    Worker(appContext, workerParams) {
    override fun doWork(): Result {
        // Do the work here--in this case, upload the images.
        uploadImages()
        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }

    private fun uploadImages() {
        try {
            var uploadImageURI = "/storage/emulated/0/Pictures/15062020155500.jpg";
            if (!TextUtils.isEmpty(uploadImageURI)) {
                val file = File(uploadImageURI)

            }
        } catch (exeption: Exception) {
            exeption.printStackTrace()
        }

    }

//    override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
//        Log.d("ImageUpload", String.format("onProgressChanged: %d, total: %d, current: %d", id, bytesTotal, bytesCurrent))
//    }
//    override fun onStateChanged(id: Int, newState: TransferState) {
//        if (newState == TransferState.COMPLETED) {
//        }
//    }

}