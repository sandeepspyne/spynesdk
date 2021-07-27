package com.spyneai.dashboard.ui

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.spyneai.base.network.Resource
import com.spyneai.captureFailureEvent
import com.spyneai.logout.InvalidAuthDialog

fun <A : Activity> Activity.startNewActivity(activity: Class<A>) {
    Intent(this, activity).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
    }
}

fun View.visible(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}

fun View.enable(enabled: Boolean) {
    isEnabled = enabled
    alpha = if (enabled) 1f else 0.5f
}

fun View.snackbar(message: String, action: (() -> Unit)? = null) {
    val snackbar = if (action == null)
        Snackbar.make(this, message, Snackbar.LENGTH_LONG)
    else
        Snackbar.make(this, message, Snackbar.LENGTH_INDEFINITE)

    action?.let {
        snackbar.setAction("Retry") {
            it()
        }
    }
    snackbar.show()
}

fun Fragment.handleApiError(
    failure: Resource.Failure,
    retry: (() -> Unit)? = null
) {
    when {
        failure.isNetworkError -> requireView().snackbar(
            failure.errorMessage!!,
            retry
        )
        failure.errorCode == 401 -> {
            InvalidAuthDialog().show(requireFragmentManager(), "InvalidAuthDialog")
        }
        else -> {
            val error = failure.errorMessage
            requireView().snackbar(error!!,retry)
        }
    }
}





