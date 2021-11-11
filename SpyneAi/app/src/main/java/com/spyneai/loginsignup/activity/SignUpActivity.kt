package com.spyneai.loginsignup.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
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
import com.spyneai.interfaces.MyAPIService
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.loginsignup.models.GetCountriesResponse
import com.spyneai.loginsignup.models.SignupResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.utils.log
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_sign_in_using_otp.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SignUpActivity : AppCompatActivity() {

    var countriesList = ArrayList<String>()
    lateinit var spinnerAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
//set countries spinner
        countriesList.add(getString(R.string.select_country))
        spinnerAdapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            countriesList
        )

        counties_spinner.adapter = spinnerAdapter

        when(getString(R.string.app_name)) {
            AppConstants.SPYNE_AI -> { llCoupon.visibility = View.VISIBLE
            }
            else ->llCoupon.visibility = View.GONE
        }

        setSpinner()
        listeners()
        //tvAlreadyLogin.text = "Already a "+ getString(R.string.app_name) + " user?"
        val appName = getString(R.string.app_name)
        tvAlreadyLogin.text = getString(R.string.already_a_user,appName)
    }

    private fun setSpinner() {
        val call = RetrofitClients.buildService(MyAPIService::class.java).getCountries()

        call?.enqueue(object : Callback<GetCountriesResponse> {
            override fun onResponse(
                call: Call<GetCountriesResponse>,
                response: Response<GetCountriesResponse>
            ) {
                if (response.isSuccessful) {

                    var countriesData = response.body()

                    if (countriesData?.status == 200 && (countriesData.data != null && countriesData.data.size > 0)) {
                        for (item in countriesData.data) {
                            countriesList.add(item.name)
                        }

                        setData(countriesList)
                    }
                } else {
                    setSpinner()
                }
            }

            override fun onFailure(call: Call<GetCountriesResponse>, t: Throwable) {
                setSpinner()
            }
        })
    }

    private fun setData(countriesList: java.util.ArrayList<String>) {
        countryProgressBar.visibility = View.GONE
        spinnerAdapter.addAll(countriesList)
    }

    private fun listeners() {
        tv_signUp.setOnClickListener {

            if (et_signupEmail.text.isNullOrEmpty() || !Utilities.isValidEmail(
                    et_signupEmail.text.toString().trim()
                )
            ) {
                et_signupEmail.error = getString(R.string.please_enter_email)
                tvLogin.isClickable = true
                tv_signUp.isClickable = true
                tv_terms.isClickable = true
                tv_sign_in_using_otp.isClickable = true
            } else if (et_signupPassword.text.isNullOrEmpty() || et_signupPassword.text.toString()
                    .trim().length < 3
            ) {
                et_signupPassword.error = "Password must be at least 3 characters long."
                return@setOnClickListener
            } else if (et_business_name.text.isNullOrEmpty()) {
                et_business_name.error = "PLease enter business/your name"
            }else if (counties_spinner.selectedItemPosition == 0){
                Toast.makeText(this, "Please select your country", Toast.LENGTH_LONG).show()
            }
            else {
                signUp(
                    et_signupEmail.text.toString().trim(),
                    et_signupPassword.text.toString(),
                    et_business_name.text.toString().trim(),
                    counties_spinner.selectedItem.toString(),
                    etCoupon.text.toString().trim()
                )
                tvEmailError.visibility = View.GONE
                tvLogin.isClickable = false
                tv_signUp.isClickable = false
                tv_terms.isClickable = false
                tv_sign_in_using_otp.isClickable = false
            }
        }

        tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        tv_sign_in_using_otp.setOnClickListener {
            val intent = Intent(this, SignInUsingOtpActivity::class.java)
            startActivity(intent)
        }

        tv_terms.setOnClickListener {
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

    private fun signUp(email: String, password: String, name: String, country: String,coupon: String) {
        val properties = Properties()
        properties.apply {
            this["email"] = email
            this["name"] = name
            this["country"] = country
            this["coupon"] = coupon
        }

        captureEvent(Events.SIGNUP_INTIATED, properties)
        Utilities.showProgressDialog(this)

        val call = RetrofitClients.buildService(MyAPIService::class.java)
            .signUp(WhiteLabelConstants.API_KEY,
                email,
                password,
                "PASSWORD",
                name,
                country,
                "Android",
                coupon)

        call?.enqueue(object : Callback<SignupResponse> {
            override fun onResponse(
                call: Call<SignupResponse>,
                response: Response<SignupResponse>
            ) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful && response.body() != null) {
                    var signUpResponse = response.body()
                    when (signUpResponse?.status) {
                        200 -> {
                            Toast.makeText(
                                this@SignUpActivity,
                                "Signup successful",
                                Toast.LENGTH_SHORT
                            ).show()

                            Utilities.savePrefrence(
                                this@SignUpActivity, AppConstants.AUTH_KEY,
                                response.body()!!.auth_token
                            )

                            Utilities.savePrefrence(
                                this@SignUpActivity, AppConstants.ENTERPRISE_ID,
                                response.body()!!.enterpriseId
                            )

                            properties.apply {
                                this["user_id"] = signUpResponse.userId
                            }

                            captureEvent(Events.SIGNUP_SUCCEED, properties)
                            captureIdentity(signUpResponse.userId, properties)
                            log("User name(savePrefrence): " + response.body()!!.userName)
                            log("User Email(savePrefrence): " + response.body()!!.emailId)
                            log("Auth token(savePrefrence): " + response.body()!!.auth_token)
                            log("User Id(savePrefrence): " + response.body()!!.userId)

                            val intent = Intent(
                                this@SignUpActivity,
                                MainDashboardActivity::class.java
                            )
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            intent.putExtra(AppConstants.IS_NEW_USER, true)
                            intent.putExtra(
                                AppConstants.CREDITS_MESSAGE,
                                signUpResponse.displayMessage
                            )
                            startActivity(intent)

                            tvLogin.isClickable = true
                            tv_signUp.isClickable = true
                            tv_terms.isClickable = true
                            tv_sign_in_using_otp.isClickable = true
                        }
                        400 -> {
                            Toast.makeText(
                                this@SignUpActivity,
                                signUpResponse.message,
                                Toast.LENGTH_SHORT
                            ).show()
                            tvLogin.isClickable = true
                            tv_signUp.isClickable = true
                            tv_terms.isClickable = true
                            tv_sign_in_using_otp.isClickable = true

                            captureFailureEvent(
                                Events.SIGNUP_FAILED,
                                properties,
                                signUpResponse.message
                            )
                        }

                        else -> {
                            Toast.makeText(
                                this@SignUpActivity,
                                response.errorBody().toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                            tvLogin.isClickable = true
                            tv_signUp.isClickable = true
                            tv_terms.isClickable = true
                            tv_sign_in_using_otp.isClickable = true

                            captureFailureEvent(
                                Events.SIGNUP_FAILED,
                                properties,
                                "Server not responding"
                            )
                        }
                    }
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Server not responding!, Please try again later",
                        Toast.LENGTH_SHORT
                    ).show()

                    tvLogin.isClickable = true
                    tv_signUp.isClickable = true
                    tv_terms.isClickable = true
                    tv_sign_in_using_otp.isClickable = true
                    captureFailureEvent(Events.SIGNUP_FAILED, properties, "Server not responding")
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

                captureFailureEvent(Events.SIGNUP_FAILED, properties, t?.localizedMessage)
            }
        })
    }
}