package com.spyneai.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.posthog.android.Properties
import com.spyneai.R
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.ui.WhiteLabelConstants
import com.spyneai.interfaces.MyAPIService
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.loginsignup.activity.LoginActivity
import com.spyneai.model.login.LoginResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_sign_in_using_otp.*
import kotlinx.android.synthetic.main.activity_sign_in_using_otp.tvTerms
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignInUsingOtpActivity : AppCompatActivity() {

    private lateinit var etNumber : EditText
    private lateinit var etEmail : EditText

    private lateinit var llNumber : LinearLayout
    private lateinit var llEmail : LinearLayout


    private lateinit var tvSignIn: Button


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

        tvSignIn = findViewById(R.id.tv_sign_in)

    }

    private fun listeners() {

        bt_login_with_password.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }


        tvSignIn.setOnClickListener(View.OnClickListener {
            if (!et_email.text.toString().trim().isEmpty()
                && Utilities.isValidEmail(et_email.text.toString().trim())) {
                makeSignIn()
                tvErrorEmail.visibility = View.GONE
            } else
                tvErrorEmail.visibility = View.VISIBLE

        })

        tvTerms.setOnClickListener {
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


        etEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                           after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int,
                                       count: Int) {
                tvErrorEmail.visibility = View.GONE
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
        val properties = Properties().putValue("email",etEmail.text.toString().trim())
        captureEvent(Events.OTP_LOGIN_INTIATED,properties)

        Utilities.showProgressDialog(this)

        val request = RetrofitClients.buildService(MyAPIService::class.java)
        val call = request.loginEmailApp(etEmail.text.toString().trim(), WhiteLabelConstants.API_KEY)
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
                                AppConstants.EMAIL_ID, etEmail.text.toString())
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