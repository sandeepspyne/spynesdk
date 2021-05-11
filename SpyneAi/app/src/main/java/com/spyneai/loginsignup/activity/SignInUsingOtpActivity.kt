package com.spyneai.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.spyneai.R
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClient
import com.spyneai.model.login.LoginRequest
import com.spyneai.model.login.LoginResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_sign_in_using_otp.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignInUsingOtpActivity : AppCompatActivity() {

    private lateinit var etNumber : EditText
    private lateinit var etEmail : EditText

    private lateinit var llNumber : LinearLayout
    private lateinit var llEmail : LinearLayout

    private lateinit var tvEmailInstead : TextView

    private lateinit var tvSignIn: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in_using_otp)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        findIds()
        listeners()
    }

    private fun findIds() {
        etEmail = findViewById(R.id.et_email)

        llEmail = findViewById(R.id.ll_email)

        tvEmailInstead = findViewById(R.id.tv_email_instead)
        tvSignIn = findViewById(R.id.tv_sign_in)

    }

    private fun listeners() {
        tvEmailInstead.setOnClickListener(View.OnClickListener {
            llEmail.visibility = View.VISIBLE
            tvEmailInstead.visibility = View.GONE
            llNumber.visibility = View.GONE
        })

        tvSignIn.setOnClickListener(View.OnClickListener {
            if (!et_email.text.toString().trim().isEmpty()
                && Utilities.isValidEmail(et_email.text.toString().trim())) {
                makeSignIn()
                tvErrorEmail.visibility = View.INVISIBLE
            } else
                tvErrorEmail.visibility = View.VISIBLE

        })

        tvTerms.setOnClickListener(View.OnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.spyne.ai/terms-service"))
            startActivity(browserIntent)
        })

        etEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                           after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int,
                                       count: Int) {
                tvErrorEmail.visibility = View.INVISIBLE
            }
        })

        ivgoogle.setOnClickListener(View.OnClickListener {
            Toast.makeText(
                applicationContext,
                "Coming Soon!!!",
                Toast.LENGTH_SHORT
            ).show()
        })


    }

    //Sign in api
    private fun makeSignIn() {
        //val todoPostCall: Call<com.spyneai.model.login.LoginResponse> = apiInterface.postContactNum(userRegistrationRequest)

        Utilities.showProgressDialog(this)
        val loginRequest = LoginRequest(etEmail.text.toString());

        val request = RetrofitClient.buildService(APiService::class.java)
        val call = request.loginEmailApp(loginRequest)
//        val call = request.loginEmailApp(etEmail.text.toString(),"value")


        call?.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                Utilities.hideProgressDialog()

                if (response.isSuccessful) {
                    if (response.body()!!.header.tokenId != null) {
                        Toast.makeText(
                            this@SignInUsingOtpActivity,
                            response.body()!!.msgInfo.msgDescription,
                            Toast.LENGTH_SHORT
                        ).show()

                        //  Log.e("ok", response.body()!!.header.tokenId)
                        Utilities.savePrefrence(this@SignInUsingOtpActivity,
                            AppConstants.EMAIL_ID, etEmail.text.toString())
                        val intent = Intent(this@SignInUsingOtpActivity, OtpActivity::class.java)
                        if (response.body()!!.header.tokenId != null)
                            intent.putExtra(AppConstants.tokenId, response.body()!!.header.tokenId)
                        startActivity(intent)
                    }
                    else{
                        val intent = Intent(this@SignInUsingOtpActivity, OtpActivity::class.java)
                        if (response.body()!!.header.tokenId != null)
                            intent.putExtra(AppConstants.tokenId, response.body()!!.header.tokenId)
                        startActivity(intent)
                        Toast.makeText(
                            this@SignInUsingOtpActivity,
                            "Otp Sent",
                            Toast.LENGTH_SHORT
                        ).show()
                       /* Toast.makeText(
                            applicationContext,
                            "Unable to send OTP, Please try again later.",
                            Toast.LENGTH_SHORT
                        ).show()*/
                    }
                }
                else{
                    Toast.makeText(
                        applicationContext,
                        "Server not responding!, Please try again later",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("ok", "no way")
                Utilities.hideProgressDialog()
                Toast.makeText(
                    applicationContext,
                    "Server not responding!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Utilities.savePrefrence(
            this@SignInUsingOtpActivity,
            AppConstants.tokenId, "")
    }

    override fun onPause() {
        super.onPause()
        /* Utilities.savePrefrence(
                 this,
                 AppConstants.tokenId,
                 "")*/
    }

    override fun onDestroy() {
        super.onDestroy()
        /* Utilities.savePrefrence(
                 this,
                 AppConstants.tokenId,
                 "")*/
    }
}