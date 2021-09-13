package com.spyneai

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.posthog.android.Properties
import android.widget.ImageButton
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.spyneai.dashboard.ui.MainDashboardActivity
import com.spyneai.loginsignup.activity.LoginActivity
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.ui.base.ShootActivity
import com.spyneai.shoot.ui.dialogs.ResolutionNotSupportedFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

fun Context.gotoHome(){
    val intent = Intent(this, MainDashboardActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
}
fun Context.gotoLogin(){
    this.captureEvent(Events.LOG_OUT, Properties())

    Utilities.savePrefrence(this, AppConstants.TOKEN_ID, "")
    Utilities.savePrefrence(this, AppConstants.AUTH_KEY, "")
    Utilities.savePrefrence(this, AppConstants.PROJECT_ID, "")
    Utilities.savePrefrence(this, AppConstants.SHOOT_ID, "")
    Utilities.savePrefrence(this, AppConstants.SKU_ID, "")
    Intent.FLAG_ACTIVITY_CLEAR_TASK
    val intent = Intent(this, LoginActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
}


fun Context.isValidGlideContext() = this !is Activity || (!this.isDestroyed && !this.isFinishing)

fun ImageButton.toggleButton(
    flag: Boolean, rotationAngle: Float, @DrawableRes firstIcon: Int, @DrawableRes secondIcon: Int,
    action: (Boolean) -> Unit
) {
    if (flag) {
        if (rotationY == 0f) rotationY = rotationAngle
        animate().rotationY(0f).apply {
            setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    action(!flag)
                }
            })
        }.duration = 200
        GlobalScope.launch(Dispatchers.Main) {
            delay(100)
            setImageResource(firstIcon)
        }
    } else {
        if (rotationY == rotationAngle) rotationY = 0f
        animate().rotationY(rotationAngle).apply {
            setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    action(!flag)
                }
            })
        }.duration = 200
        GlobalScope.launch(Dispatchers.Main) {
            delay(100)
            setImageResource(secondIcon)
        }
    }
}

fun Long.toDate() : String {
    val sdf = SimpleDateFormat("dd MMM, yyyy")
    val netDate = Date(this)
    return sdf.format(netDate)
}

fun Context.isMyServiceRunning(serviceClass: Class<*>): Boolean {
    val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (service in manager.getRunningServices(Int.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }
    return false
}

fun Context.isInternetActive() : Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
    return activeNetwork?.isConnectedOrConnecting == true
}

fun Context.getNetworkName() : String {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val wifi: NetworkInfo? = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
    val mobile: NetworkInfo? = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)

    var type = "None"

    if (wifi != null && wifi.isConnected)
        type = "Wi-Fi"

    if (mobile != null && mobile.isConnected)
        type = "Mobile"

    return type
}

fun getRequestHeaderData() : JSONObject {
    val headerData = JSONObject()

    headerData.put("device_manufacturer",Utilities.getPreference(
        BaseApplication.getContext(),AppConstants.DEVICE_MANUFACTURER))

    headerData.put("model",Utilities.getPreference(
        BaseApplication.getContext(),AppConstants.MODEL))

    headerData.put("os_version",Utilities.getPreference(
        BaseApplication.getContext(),AppConstants.OS_VERSION))

    headerData.put("app_version",Utilities.getPreference(
        BaseApplication.getContext(),AppConstants.APP_VERSION))

    headerData.put("app_version_code",Utilities.getPreference(
        BaseApplication.getContext(),AppConstants.APP_VERSION_CODE))


    headerData.put("network_type",Utilities.getPreference(
        BaseApplication.getContext(),AppConstants.NETWORK_TYPE))

    headerData.put("device_id",Utilities.getPreference(
        BaseApplication.getContext(),AppConstants.DEVICE_ID))

    return headerData
}

fun Context.isResolutionSupported() : Boolean {
    val cm = getSystemService(Context.CAMERA_SERVICE) as CameraManager

    var resolutionSupported = false

    if (cm.cameraIdList != null && cm.cameraIdList.size > 1) {
        val characteristics: CameraCharacteristics =
            cm.getCameraCharacteristics("1")

        val configs = characteristics.get(
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
        )

        val s = configs?.getOutputSizes(ImageFormat.JPEG)


        s?.forEach { it ->
            if (!resolutionSupported && it != null) {
                if (it.width == 1024 && it.height == 768)
                    resolutionSupported = true
            }
        }
    }

    return resolutionSupported
}
