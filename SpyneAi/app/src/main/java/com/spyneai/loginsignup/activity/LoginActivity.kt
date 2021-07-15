package com.spyneai.loginsignup.activity

import android.content.Intent
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
import com.spyneai.interfaces.MyAPIService

import com.spyneai.interfaces.RetrofitClientSpyneAi
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.loginsignup.models.LoginEmailPasswordBody
import com.spyneai.loginsignup.models.LoginEmailPasswordResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shoot.utils.log
import com.spyneai.posthog.Events
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_sign_in_using_otp.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        et_loginPassword.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        listeners()
    }

    private fun listeners() {
        bt_login.setOnClickListener {
            if (!et_loginEmail.text.toString().trim().isEmpty() && !et_loginPassword.text.toString()
                    .trim().isEmpty()
                && Utilities.isValidEmail(et_loginEmail.text.toString().trim())
            ) {
                login(et_loginEmail.text.toString().trim(), et_loginPassword.text.toString().trim())
                bt_login.isClickable = false
                tvSignup.isClickable = false
                tvForgotPassword.isClickable = false
                bt_sign_in_using_otp.isClickable = false
                tvterms.isClickable = false
                tvError.visibility = View.GONE
            } else
                tvError.visibility = View.VISIBLE
            bt_login.isClickable = true
            tvSignup.isClickable = true
            tvForgotPassword.isClickable = true
            bt_sign_in_using_otp.isClickable = true
            tvterms.isClickable = true
        }
        tvSignup.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        bt_sign_in_using_otp.setOnClickListener {
            val intent = Intent(this, SignInUsingOtpActivity::class.java)
            startActivity(intent)
        }

        tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    private fun login(email: String, password: String) {
        val properties = Properties().putValue("email",email)
        captureEvent(Events.LOGIN_INTIATED,properties)

        Utilities.showProgressDialog(this)

        val call = RetrofitClients.buildService(MyAPIService::class.java)
            .loginEmailPassword(email, AppConstants.API_KEY, password, "PASSWORD")


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
                        bt_login.isClickable = true
                        tvSignup.isClickable = true
                        tvForgotPassword.isClickable = true
                        bt_sign_in_using_otp.isClickable = true
                        tvterms.isClickable = true
                    } else if (loginResponse?.status == 200) {

                        Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT)
                            .show()

                        Utilities.savePrefrence(this@LoginActivity,AppConstants.AUTH_KEY, response.body()!!.auth_token)
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


                        bt_login.isClickable = true
                        tvSignup.isClickable = true
                        tvForgotPassword.isClickable = true
                        bt_sign_in_using_otp.isClickable = true
                        tvterms.isClickable = true

                        val intent = Intent(this@LoginActivity, MainDashboardActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        intent.putExtra(AppConstants.IS_NEW_USER,false)
                        startActivity(intent)
                    }
                } else {
                    val error = response.errorBody().toString()
                    Toast.makeText(this@LoginActivity, error, Toast.LENGTH_SHORT).show()
                    bt_login.isClickable = true
                    tvSignup.isClickable = true
                    tvForgotPassword.isClickable = true
                    bt_sign_in_using_otp.isClickable = true
                    tvterms.isClickable = true
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
                bt_login.isClickable = true
                tvSignup.isClickable = true
                tvForgotPassword.isClickable = true
                bt_sign_in_using_otp.isClickable = true
                tvterms.isClickable = true
            }
        })

    }


}
