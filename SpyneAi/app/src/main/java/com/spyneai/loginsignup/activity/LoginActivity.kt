package com.spyneai.loginsignup.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.posthog.android.Properties
import com.spyneai.R
import com.spyneai.activity.SignInUsingOtpActivity
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.captureIdentity
import com.spyneai.dashboard.ui.MainDashboardActivity
import com.spyneai.dashboard.ui.WhiteLabelConstants
import com.spyneai.databinding.ActivityLoginBinding
import com.spyneai.interfaces.MyAPIService
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.loginsignup.models.LoginEmailPasswordResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.utils.log
import kotlinx.android.synthetic.main.activity_sign_up.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : AppCompatActivity() {
    
    lateinit var binding : ActivityLoginBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etLoginPassword.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)

        when(getString(R.string.app_name)){
            AppConstants.KARVI -> {
                binding.apply {
                    rlSingup.visibility = View.GONE
                    llOr.visibility = View.GONE
                    btSignInUsingOtp.visibility = View.GONE
                }
            }
            else ->{}
        }

        val appName = getString(R.string.app_name)
        binding.tvNewToApp.text = getString(R.string.new_to,appName)

        listeners()
    }

    private fun listeners() {
        binding.btLogin.setOnClickListener {

            when {
                binding.etLoginEmail.text.toString().isEmpty() -> {
                    binding.etLoginEmail.error = "Please enter email id"
                }
                binding.etLoginPassword.text.toString().isEmpty() -> {
                    binding.etLoginPassword.error = "Please enter password"
                }
                else -> {
                    login(binding.etLoginEmail.text.toString().trim(), binding.etLoginPassword.text.toString().trim())
                    binding.btLogin.isClickable = false
                    binding.tvSignup.isClickable = false
                    binding.tvForgotPassword.isClickable = false
                    binding.btSignInUsingOtp.isClickable = false
                    binding.tvterms.isClickable = false
                }
            }
        }


        binding.tvSignup.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.btSignInUsingOtp.setOnClickListener {
            val intent = Intent(this, SignInUsingOtpActivity::class.java)
            startActivity(intent)
        }

        binding.tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        binding.tvterms.setOnClickListener {
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

    private fun login(email: String, password: String) {
        val properties = Properties().putValue("email",email)
        captureEvent(Events.LOGIN_INTIATED,properties)

        Utilities.showProgressDialog(this)

        val call = RetrofitClients.buildService(MyAPIService::class.java)
            .loginEmailPassword(email, WhiteLabelConstants.API_KEY, password, "PASSWORD")


        call?.enqueue(object : Callback<LoginEmailPasswordResponse> {
            override fun onResponse(
                call: Call<LoginEmailPasswordResponse>,
                response: Response<LoginEmailPasswordResponse>
            ) {
                Utilities.hideProgressDialog()

                var loginResponse = response.body()

                if (response.isSuccessful && loginResponse != null) {
                    if (loginResponse?.status == 401) {

                        Toast.makeText(
                            this@LoginActivity,
                            response.body()!!.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.btLogin.isClickable = true
                         binding.tvSignup.isClickable = true
                         binding.tvForgotPassword.isClickable = true
                        binding.btSignInUsingOtp.isClickable = true
                        binding.tvterms.isClickable = true
                    } else if (loginResponse?.status == 200) {

                        Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT)
                            .show()

                        Utilities.savePrefrence(this@LoginActivity,AppConstants.AUTH_KEY, response.body()!!.auth_token)
                        Utilities.savePrefrence(
                            this@LoginActivity, AppConstants.ENTERPRISE_ID,
                            response.body()!!.enterpriseId
                        )

                        log("User name(savePrefrence): "+response.body()!!.user_name)
                        log("User Email(savePrefrence): "+response.body()!!.email_id)
                        log("User Id(savePrefrence): "+response.body()!!.user_id)
                        log("Auth token(savePrefrence): "+response.body()!!.auth_token)

                        properties.apply {
                            this["user_id"] = loginResponse.user_id
                            this["name"] = loginResponse.user_name
                        }

                        captureEvent(Events.LOGIN_SUCCEED,properties)
                        captureIdentity(loginResponse.user_id,properties)


                        binding.btLogin.isClickable = true
                         binding.tvSignup.isClickable = true
                         binding.tvForgotPassword.isClickable = true
                        binding.btSignInUsingOtp.isClickable = true
                        binding.tvterms.isClickable = true

                        val intent = Intent(this@LoginActivity, MainDashboardActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        intent.putExtra(AppConstants.IS_NEW_USER,false)
                        startActivity(intent)
                    }
                } else {
                    val error = response.errorBody().toString()
                    Toast.makeText(this@LoginActivity, error, Toast.LENGTH_SHORT).show()
                    binding.btLogin.isClickable = true
                     binding.tvSignup.isClickable = true
                     binding.tvForgotPassword.isClickable = true
                    binding.btSignInUsingOtp.isClickable = true
                    binding.tvterms.isClickable = true
                    captureFailureEvent(Events.LOGIN_FAILED,properties,error)
                }
            }

            override fun onFailure(call: Call<LoginEmailPasswordResponse>, t: Throwable) {
                Utilities.hideProgressDialog()
                captureFailureEvent(Events.LOGIN_FAILED,properties,t?.localizedMessage)
                Toast.makeText(
                    applicationContext,
                    "Server not responding!, Please try again later",
                    Toast.LENGTH_SHORT
                ).show()
                binding.btLogin.isClickable = true
                 binding.tvSignup.isClickable = true
                 binding.tvForgotPassword.isClickable = true
                binding.btSignInUsingOtp.isClickable = true
                binding.tvterms.isClickable = true
            }
        })

    }


}
