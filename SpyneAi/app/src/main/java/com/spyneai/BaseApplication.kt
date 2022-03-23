package com.spyneai

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.*
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.posthog.android.PostHog
import com.spyneai.dashboard.ui.WhiteLabelConstants
import com.spyneai.needs.AppConstants
import com.spyneai.sdk.Spyne
import com.spyneai.shoot.workmanager.InternetWorker
import java.util.concurrent.TimeUnit


@SuppressLint("StaticFieldLeak")
class BaseApplication : Application() {

    private val POSTHOG_API_KEY = "FoIzpWdbY_I9T_4jr5k4zzNuVJPcpzs_mIpO6y7581M"
    private val POSTHOG_HOST = "https://app.posthog.com"

    private val SENTRY_DSN = "https://cb29df9ea3bf465ba3c7af863fe67a3a@o1145224.ingest.sentry.io/6212690"

    companion object {
        private lateinit var context: Context

        fun setContext(con: Context){
            context = con
        }

        fun getContext(): Context {
            return context;
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = this

        Spyne.init(context,WhiteLabelConstants.API_KEY,AppConstants.CARS_CATEGORY_ID)

        //Sentry.init(SENTRY_DSN)

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

        val repeatInternal = 30L
        val flexInterval = 25L
        val workerTag = "InternetWorker"

        PeriodicWorkRequest
            .Builder(
                InternetWorker::class.java, repeatInternal,
                TimeUnit.MINUTES, flexInterval, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build())
            .build()
            .also {
                WorkManager.getInstance(context).enqueueUniquePeriodicWork(workerTag,
                    ExistingPeriodicWorkPolicy.REPLACE, it)
            }
    }
}