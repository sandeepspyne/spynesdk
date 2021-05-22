package com.spyneai.loginsignup.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.spyneai.R
import com.spyneai.activity.SignInUsingOtpActivity
import com.spyneai.dashboard.ui.dashboard.MainDashboardActivity
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClientSpyneAi
import com.spyneai.loginsignup.models.SignupBody
import com.spyneai.loginsignup.models.SignupResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_sign_in_using_otp.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        listeners()
    }

    private fun listeners() {
        tv_signUp.setOnClickListener {

            if (et_signupEmail.text.isNullOrEmpty() || !Utilities.isValidEmail(et_signupEmail.text.toString().trim())){
                et_signupEmail.error = getString(R.string.please_enter_email)
                tvLogin.isClickable = true
                tv_signUp.isClickable = true
                tv_terms.isClickable = true
                tv_sign_in_using_otp.isClickable = true
            }else if (et_signupPassword.text.isNullOrEmpty() || et_signupPassword.text.toString().trim().length < 3) {
                et_signupPassword.error = "Password must be at least 3 characters long."
                return@setOnClickListener
            }else if(et_business_name.text.isNullOrEmpty()){
                et_business_name.error = "PLease enter business/your name"
            }else{
                signUp()
                tvEmailError.visibility = View.GONE
                tvLogin.isClickable = false
                tv_signUp.isClickable = false
                tv_terms.isClickable = false
                tv_sign_in_using_otp.isClickable = false
            }
        }

        tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        tv_sign_in_using_otp.setOnClickListener {
            val intent = Intent(this, SignInUsingOtpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun signUp() {
        Utilities.showProgressDialog(this)

        var body = SignupBody(
            et_signupEmail.text.toString(), "spyne-default", et_business_name.text.toString(), "", "corporate",
            et_signupPassword.text.toString(), 91, "yes", "", "Others"
        )

        var call = RetrofitClientSpyneAi.buildService(APiService::class.java).signUp(body)

        call?.enqueue(object : Callback<SignupResponse> {
            override fun onResponse(
                call: Call<SignupResponse>,
                response: Response<SignupResponse>
            ) {
                Utilities.hideProgressDialog()

                if (response.isSuccessful && response.body() != null) {
                    Utilities.hideProgressDialog()
                    if (response.body()!!.exists.equals(true)) {
                        Toast.makeText(
                            this@SignUpActivity,
                            "Email id already exists, please login.",
                            Toast.LENGTH_SHORT
                        ).show()
                        tvLogin.isClickable = true
                        tv_signUp.isClickable = true
                        tv_terms.isClickable = true
                        tv_sign_in_using_otp.isClickable = true
                    } else if (response.body()!!.exists.equals(false)) {
                        Toast.makeText(this@SignUpActivity, "Signup successful", Toast.LENGTH_SHORT)
                            .show()
                        Utilities.savePrefrence(
                            this@SignUpActivity, AppConstants.tokenId,
                            response.body()!!.userId
                        )
                        val intent = Intent(this@SignUpActivity, MainDashboardActivity::class.java)
                        startActivity(intent)
                        finish()
                        tvLogin.isClickable = true
                        tv_signUp.isClickable = true
                        tv_terms.isClickable = true
                        tv_sign_in_using_otp.isClickable = true
                    }
                } else {
                    Utilities.hideProgressDialog()
                    Toast.makeText(
                        this@SignUpActivity,
                        response.errorBody().toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                    tvLogin.isClickable = true
                    tv_signUp.isClickable = true
                    tv_terms.isClickable = true
                    tv_sign_in_using_otp.isClickable = true
                }
            }

            override fun onFailure(call: Call<SignupResponse>, t: Throwable) {
                Utilities.hideProgressDialog()
                Toast.makeText(
                    applicationContext,
                    "Server not responding!, Please try again later",
                    Toast.LENGTH_SHORT
                ).show()
                tvLogin.isClickable = true
                tv_signUp.isClickable = true
                tv_terms.isClickable = true
                tv_sign_in_using_otp.isClickable = true
            }
        })
    }
}