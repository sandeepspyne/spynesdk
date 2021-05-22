package com.spyneai.loginsignup.activity

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.spyneai.R
import com.spyneai.activity.SignInUsingOtpActivity
import com.spyneai.dashboard.ui.dashboard.MainDashboardActivity
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClientSpyneAi
import com.spyneai.loginsignup.models.LoginEmailPasswordBody
import com.spyneai.loginsignup.models.LoginEmailPasswordResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
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

    private fun listeners(){
        bt_login.setOnClickListener {
            if (!et_loginEmail.text.toString().trim().isEmpty() && !et_loginPassword.text.toString().trim().isEmpty()
                && Utilities.isValidEmail(et_loginEmail.text.toString().trim())) {
                login()
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
            finish()
        }
    }

    private fun login(){

        Utilities.showProgressDialog(this)

        var body = LoginEmailPasswordBody(
            et_loginEmail.text.toString(), et_loginPassword.text.toString()
        )

        var call = RetrofitClientSpyneAi.buildService(APiService::class.java).loginEmailPassword(body)


        call?.enqueue(object : Callback<LoginEmailPasswordResponse> {
            override fun onResponse(call: Call<LoginEmailPasswordResponse>, response: Response<LoginEmailPasswordResponse>) {
                Utilities.hideProgressDialog()

                if (response.isSuccessful && response.body()!=null) {
                    Utilities.hideProgressDialog()
                    if (response.body()!!.status.equals("INVALID")){
                        Toast.makeText(this@LoginActivity, response.body()!!.message, Toast.LENGTH_SHORT).show()
                        bt_login.isClickable = true
                        tvSignup.isClickable = true
                        tvForgotPassword.isClickable = true
                        bt_sign_in_using_otp.isClickable = true
                        tvterms.isClickable = true
                    }else if (response.body()!!.status.equals("LOGGED_IN")){
                        Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                        Utilities.savePrefrence(this@LoginActivity,AppConstants.tokenId,
                            response.body()!!.userId)
                        val intent = Intent(this@LoginActivity, MainDashboardActivity::class.java)
                        startActivity(intent)
                        finish()
                        bt_login.isClickable = true
                        tvSignup.isClickable = true
                        tvForgotPassword.isClickable = true
                        bt_sign_in_using_otp.isClickable = true
                        tvterms.isClickable = true
                    }
                }
                else{
                    Utilities.hideProgressDialog()
                    Toast.makeText(this@LoginActivity, response.errorBody().toString(), Toast.LENGTH_SHORT).show()
                    bt_login.isClickable = true
                    tvSignup.isClickable = true
                    tvForgotPassword.isClickable = true
                    bt_sign_in_using_otp.isClickable = true
                    tvterms.isClickable = true
                }
            }

            override fun onFailure(call: Call<LoginEmailPasswordResponse>, t: Throwable) {
                Utilities.hideProgressDialog()
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