package com.spyneai.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.spyneai.R
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.ui.WhiteLabelConstants
import com.spyneai.databinding.ActivitySignInUsingOtpBinding
import com.spyneai.interfaces.MyAPIService
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.loginsignup.activity.LoginActivity
import com.spyneai.model.login.LoginResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignInUsingOtpActivity : AppCompatActivity() {

    lateinit var binding : ActivitySignInUsingOtpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignInUsingOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        listeners()
    }



    private fun listeners() {

        
        binding.btLoginWithPassword.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }


        binding.tvSignIn.setOnClickListener{
            when {
                binding.etEmail.text.toString().isEmpty() -> {
                    binding.etEmail.error = "Please eneter email id"
                }

                !Utilities.isValidEmail(binding.etEmail.text.toString().trim()) -> {
                    binding.etEmail.error = "Please enter valid email id"
                }

                else -> makeSignIn()
            }
            if (!binding.etEmail.text.toString().trim().isEmpty()
                && Utilities.isValidEmail(binding.etEmail.text.toString().trim())) {

            }
        }

        binding.tvTerms.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.terms_and_conditions_url))))
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    this, "No application can handle this request."
                            + " Please install a webbrowser", Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            }
        }

    }

    //Sign in api
    private fun makeSignIn() {
        val properties = HashMap<String,Any?>()
            .apply {
               this.put("email",binding.etEmail.text.toString().trim())
            }

        captureEvent(Events.OTP_LOGIN_INTIATED,properties)

        Utilities.showProgressDialog(this)

        val request = RetrofitClients.buildService(MyAPIService::class.java)
        val call = request.loginEmailApp(binding.etEmail.text.toString().trim(), WhiteLabelConstants.API_KEY)
//        val call = request.loginEmailApp(etEmail.text.toString(),"value")

        call?.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                Utilities.hideProgressDialog()


                if (response.isSuccessful) {

                    var loginResponse = response.body()

                    loginResponse?.status.let {
                        if (it == 200){
                            Toast.makeText(
                                this@SignInUsingOtpActivity,
                                loginResponse?.message,
                                Toast.LENGTH_SHORT
                            ).show()

                            captureEvent(Events.OTP_LOGIN_SUCCEED,properties)

                            //  Log.e("ok", response.body()!!.header.tokenId)
                            Utilities.savePrefrence(this@SignInUsingOtpActivity,
                                AppConstants.EMAIL_ID, binding.etEmail.text.toString())
                            val intent = Intent(this@SignInUsingOtpActivity, OtpActivity::class.java)

                            if (loginResponse?.userId != null)
                                Utilities.savePrefrence(this@SignInUsingOtpActivity,
                                    AppConstants.TOKEN_ID, loginResponse?.userId)

                            startActivity(intent)

                        }else{
                            captureFailureEvent(Events.OTP_LOGIN_FAILED,properties,"Server not responding")
                            Toast.makeText(
                                applicationContext,
                                "Server not responding!, Please try again later",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                else{
                    captureFailureEvent(Events.OTP_LOGIN_FAILED,properties,"Server not responding")
                    Toast.makeText(
                        applicationContext,
                        "Server not responding!, Please try again later",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                captureFailureEvent(Events.OTP_LOGIN_FAILED,properties,t?.localizedMessage)
                Utilities.hideProgressDialog()
                Toast.makeText(
                    applicationContext,
                    "Server not responding!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

}