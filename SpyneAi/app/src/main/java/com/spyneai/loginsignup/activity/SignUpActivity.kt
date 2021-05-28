package com.spyneai.loginsignup.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.spyneai.R
import com.spyneai.activity.SignInUsingOtpActivity
import com.spyneai.dashboard.ui.dashboard.MainDashboardActivity
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClientSpyneAi
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.loginsignup.models.GetCountriesResponse
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

    var countriesList = ArrayList<String>()
    lateinit var spinnerAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
//set countries spinner
        countriesList.add("Select Your Country")
        spinnerAdapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            countriesList
        )

        counties_spinner.adapter = spinnerAdapter

        setSpinner()
        listeners()
    }

    private fun setSpinner() {

        val call = RetrofitClients.buildService(APiService::class.java).getCountries()

        call?.enqueue(object : Callback<GetCountriesResponse> {
            override fun onResponse(
                call: Call<GetCountriesResponse>,
                response: Response<GetCountriesResponse>
            ) {
                if (response.isSuccessful){

                    var countriesData = response.body()

                    if (countriesData?.status == 200 && (countriesData.data !=null && countriesData.data.size > 0)){
                        for (item in countriesData.data){
                            countriesList.add(item.name)
                        }

                        setData(countriesList)
                    }
                }else{
                    setSpinner()
                }
            }

            override fun onFailure(call: Call<GetCountriesResponse>, t: Throwable) {
                setSpinner()
            }
        })
    }

    private fun setData(countriesList: java.util.ArrayList<String>) {
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
            } else {
                signUp(
                    et_signupEmail.text.toString().trim(),
                    et_signupPassword.text.toString(),
                    et_business_name.text.toString().trim(),
                    counties_spinner.selectedItem.toString()
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
            startActivity(intent)
            finish()
        }

        tv_sign_in_using_otp.setOnClickListener {
            val intent = Intent(this, SignInUsingOtpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun signUp(email: String, password: String, name: String, country: String) {
        Utilities.showProgressDialog(this)

        val call = RetrofitClients.buildService(APiService::class.java)
            .signUp(AppConstants.API_KEY, email, password, "PASSWORD", name, country)

        call?.enqueue(object : Callback<SignupResponse> {
            override fun onResponse(
                call: Call<SignupResponse>,
                response: Response<SignupResponse>
            ) {
                Utilities.hideProgressDialog()


            if (response.isSuccessful && response.body() != null) {

                var signUpResponse = response.body()

                if (signUpResponse?.status == 200) {
                    if (signUpResponse.message == "User already exists. Please Login") {
                        Toast.makeText(
                            this@SignUpActivity,
                            "Email id already exists, please login.",
                            Toast.LENGTH_SHORT
                        ).show()
                        tvLogin.isClickable = true
                        tv_signUp.isClickable = true
                        tv_terms.isClickable = true
                        tv_sign_in_using_otp.isClickable = true
                    } else {
                        Toast.makeText(
                            this@SignUpActivity,
                            "Signup successful",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        Utilities.savePrefrence(
                            this@SignUpActivity, AppConstants.tokenId,
                            response.body()!!.userId
                        )
                        Utilities.savePrefrence(this@SignUpActivity, AppConstants.USER_NAME, response.body()!!.user_name)
                        Utilities.savePrefrence(this@SignUpActivity, AppConstants.USER_EMAIL, response.body()!!.email_id)
                        val intent = Intent(this@SignUpActivity, MainDashboardActivity::class.java)
                        intent.putExtra("from_signup",true)

                        startActivity(intent)
                        finish()
                        tvLogin.isClickable = true
                        tv_signUp.isClickable = true
                        tv_terms.isClickable = true
                        tv_sign_in_using_otp.isClickable = true
                    }
                } else {
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