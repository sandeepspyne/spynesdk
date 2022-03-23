package com.spyneai.sdk

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.captureIdentity
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.ActivitySignupSdkBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.ui.MyOrdersActivity
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.ui.base.ShootActivity
import com.spyneai.shoot.ui.base.ShootPortraitActivity

class SignupSDK : AppCompatActivity() {

    private lateinit var binding: ActivitySignupSdkBinding
    lateinit var viewModel: ShootViewModel
    var categoryName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupSdkBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        viewModel = ViewModelProvider(this, ViewModelFactory()).get(ShootViewModel::class.java)


//        Spyne.init(this, "c6d1adc2-df19-4f44-b191-ca2500c76db6", "0IJU3L28G")
//
//        Spyne.start(
//            applicationContext,
//            this,
//            "test@gourav22.in",
//            "",
//            "",
//            Intent(this, SignupSDK::class.java)
//        )

        if (::viewModel.isInitialized) {
            signupUser()
        }

    }

    override fun onBackPressed() {

    }

    private fun signupUser() {
        val properties = HashMap<String, Any?>()
        properties.apply {
            this.put("user_id", Spyne.userId)
            this.put("email", Spyne.email)
            this.put("contact", Spyne.contactNo)
        }

        viewModel.signupIntoSDK(
            Spyne.apiKey.toString(),
            Spyne.contactNo.toString(),
            Spyne.email.toString(),
            Spyne.userId.toString()
        )

        //SENTRY
        captureEvent(Events.LOGIN_INTIATED, properties)

        viewModel.signupIntoSDKRes.observe(
            this, Observer {
                when (it) {
                    is Resource.Success -> {
                        Utilities.hideProgressDialog()
                        if (it.value.status == 200) {
                            //sentry
                            captureEvent(Events.LOGIN_SUCCEED, properties)
                            captureIdentity(Spyne.userId.toString(), properties)

                            Utilities.savePrefrence(
                                this,
                                AppConstants.AUTH_KEY,
                                it.value.data.secretKey
                            )

                            Utilities.savePrefrence(this, AppConstants.CATEGORY_ID, "cat_d8R14zUNE")
                            Utilities.savePrefrence(this, AppConstants.CATEGORY_NAME, "Automobiles")
                            Utilities.savePrefrence(this, AppConstants.ENTERPRISE_ID, Spyne.enterpriseid)

                            // swiggy SDK signup done, send user to FOOD SDK home screen
                            val intent = Intent(this, MyOrdersActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    }
                    is Resource.Failure -> {
                        Utilities.hideProgressDialog()
                        handleApiError(it) { signupUser() }

                        //sentry
                        captureFailureEvent(
                            Events.LOGIN_FAILED,
                            properties,
                            it.errorMessage.toString()
                        )
                    }
                }
            }
        )
    }
}