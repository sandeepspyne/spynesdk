package com.spyneai.shoot.workmanager

import android.content.Context
import androidx.work.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.spyneai.BaseApplication

class OverlaysPreloadWorker(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val position = inputData.getInt("position",0)
        val overlaysList = inputData.getStringArray("overlays")

        return if (position == overlaysList?.size){
            Result.success()
        }else{
            Glide.with(appContext)
                .load(overlaysList?.get(position))
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .skipMemoryCache(false)
                .preload()

            preloadNextOverlay(overlaysList!!,position.plus(1))

            Result.success()
        }
    }

    private fun preloadNextOverlay(overlays: Array<String>, position: Int) {
        val data = Data.Builder()
            .putStringArray("overlays",overlays)
            .putInt("position",position)
            .build()

        val constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val overlayPreloadWorkRequest = OneTimeWorkRequest.Builder(OverlaysPreloadWorker::class.java)
            .addTag("Preload Overlays")
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        WorkManager.getInstance(BaseApplication.getContext())
            .enqueue(overlayPreloadWorkRequest)
    }
}