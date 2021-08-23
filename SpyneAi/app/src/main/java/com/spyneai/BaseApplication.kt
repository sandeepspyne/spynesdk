package com.spyneai

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.*
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.posthog.android.PostHog
import com.spyneai.shoot.workmanager.ProcessSkuWorker
import com.spyneai.shoot.workmanager.RecursiveProcessSkuWorker
import com.spyneai.shoot.workmanager.RecursiveSkippedImagesWorker
import java.util.concurrent.TimeUnit

@SuppressLint("StaticFieldLeak")
class BaseApplication : Application() {

    private val POSTHOG_API_KEY = "FoIzpWdbY_I9T_4jr5k4zzNuVJPcpzs_mIpO6y7581M"
    private val POSTHOG_HOST = "https://app.posthog.com"

    companion object {
        private lateinit var context: Context

        fun getContext(): Context {
            return context;
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = this

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)

        //disable night mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Create a PostHog client with the given context, API key and host.
        val posthog : PostHog = PostHog.Builder(this, POSTHOG_API_KEY, POSTHOG_HOST)
            .captureApplicationLifecycleEvents() // Record certain application events automatically!
            // .recordScreenViews() // Record screen views automatically!
            .build()


        // Set the initialized instance as a globally accessible instance.
        PostHog.setSingletonInstance(posthog)

        //prcess periodic worker
        val constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val longWorkRequest = PeriodicWorkRequestBuilder<ProcessSkuWorker>(
            1, TimeUnit.HOURS)
            .addTag("Periodic Processing Worker")

        WorkManager.getInstance(context)
            .enqueue(
                longWorkRequest
                    .setConstraints(constraints)
                    .build())

        val recursiveWorkRequest = PeriodicWorkRequestBuilder<RecursiveSkippedImagesWorker>(
            6, TimeUnit.HOURS)
            .addTag("Periodic Processing Worker")

        WorkManager.getInstance(context)
            .enqueue(
                recursiveWorkRequest
                    .setConstraints(constraints)
                    .build())

    }



}