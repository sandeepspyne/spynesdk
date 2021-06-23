package com.spyneai.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.spyneai.extras.OnboardTwoActivity
import com.spyneai.R
import com.spyneai.dashboard.ui.MainDashboardActivity
import com.spyneai.interfaces.MyAPIService
import com.spyneai.interfaces.RetrofitClient
import com.spyneai.interfaces.RetrofitClientSpyneAi
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.model.login.LoginRequest
import com.spyneai.model.login.LoginResponse
import com.spyneai.model.otp.OtpRequest
import com.spyneai.model.otp.OtpResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shoot.utils.log
import kotlinx.android.synthetic.main.activity_otp.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

public class OtpActivity : AppCompatActivity() {
    public var resendOtp: TextView? = null

    private  var tvSubmitOtp: TextView? = null
    private  var edtxt1: EditText? = null
    private  var edtxt2:EditText? = null
    private  var edtxt3:EditText? = null
    private  var edtxt4:EditText? = null
    private  var edtxt5:EditText? = null
    private  var edtxt6:EditText? = null

    //  var countDownTimer: OtpVerification.MyCountDownTimer = OtpVerification.MyCountDownTimer(31000, 1000)

    var txtView_count_down_timer: TextView? = null

    private val otp_wait_spinner: ProgressBar? = null

    var tick_image: ImageView? = null

    var otp_entered = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

//        val strNumber = intent.getStringExtra(AppConstants.phone)!!
        /*  val newString =  StringBuilder(strNumber)
          newString.setCharAt(2,'X')
          newString.setCharAt(3,'X')
          newString.setCharAt(4,'X')
          newString.setCharAt(5,'X')
          newString.setCharAt(6,'X')
          newString.setCharAt(7,'X')
          tvNumber.setText("+91" + newString)*/

        tvNumber.setText(Utilities.getPreference(this,AppConstants.EMAIL_ID))

        findIds()
        textListeners()
        startTimer()
    }

    private fun startTimer() {
        tvResend.visibility = View.GONE
        txtView_count_down_timer?.visibility = View.VISIBLE
        resendOtp!!.isClickable = false

        val timer = object: CountDownTimer(31000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                txtView_count_down_timer?.setText(" Sec")

                var countDownTime: String

                countDownTime = txtView_count_down_timer!!.text.toString()

                countDownTime =
                    if (millisUntilFinished / 1000 < 10)
                        "00 : 0" + millisUntilFinished / 1000 + ""
                    else
                        "00 : " + (millisUntilFinished / 1000).toString() + ""

                //txtView_count_down_timer.setText((millisUntilFinished / 1000) + "");

                //txtView_count_down_timer.setText((millisUntilFinished / 1000) + "");
                txtView_count_down_timer?.setText(countDownTime + " Sec")
            }

            override fun onFinish() {
                txtView_count_down_timer?.setText("00 : 00 Sec")
                tvResend.visibility = View.VISIBLE
                txtView_count_down_timer?.visibility = View.GONE
                //resendOtp.setBackgroundColor(Color.parseColor("#198AC7"));
                resendOtp!!.isClickable = true
            }
        }
        timer.start()
    }


    private fun findIds() {
        tvSubmitOtp = findViewById<View>(R.id.tvSubmitOtp) as TextView
        txtView_count_down_timer = findViewById<View>(R.id.tvTimer) as TextView
        resendOtp = findViewById<View>(R.id.tvResend) as TextView
        edtxt1 = findViewById<View>(R.id.etOne) as EditText
        edtxt2 = findViewById<View>(R.id.ettwo) as EditText
        edtxt3 = findViewById<View>(R.id.etThree) as EditText
        edtxt4 = findViewById<View>(R.id.etFour) as EditText
        edtxt5 = findViewById<View>(R.id.etFive) as EditText
        edtxt6 = findViewById<View>(R.id.etSix) as EditText

    }

    private fun textListeners() {

        imgBack.setOnClickListener(View.OnClickListener {
            finish()
        })

        //changing focus to next edit text in otp
        edtxt1!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.length == 1) {
                    edtxt2!!.requestFocus()
                    //otp_entered += edtxt1.getText().toString().charAt(0);
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                           after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int,
                                       count: Int) {

                /*if(edtxt1.getText().toString() != null)
                    otp_entered += edtxt1.getText().toString().charAt(0);*/
            }
        })

        edtxt2!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.length == 1) {
                    edtxt3!!.requestFocus()
                    //otp_entered += edtxt2.getText().toString().charAt(0);
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                           after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int,
                                       count: Int) {
                if (edtxt2!!.text.toString().length == 0)
                {
                    edtxt1!!.requestFocus()
                }

                /*if(edtxt2.getText().toString() != null)
                    otp_entered += edtxt2.getText().toString().charAt(0);*/
            }
        })

        edtxt3!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.length == 1) {
                    edtxt4!!.requestFocus()
                    //otp_entered += edtxt3.getText().toString().charAt(0);
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                           after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int,
                                       count: Int) {
                if (edtxt3!!.text.toString().length == 0)
                {
                    edtxt2!!.requestFocus()
                }

                /*if(edtxt3.getText().toString() != null)
                    otp_entered += edtxt3.getText().toString().charAt(0);*/
            }
        })

        edtxt4!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.length == 1) {
                    edtxt5!!.requestFocus()
                    //otp_entered += edtxt4.getText().toString().charAt(0);
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                           after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int,
                                       count: Int) {
                if (edtxt4!!.text.toString().length == 0)
                {
                    edtxt3!!.requestFocus()
                }
            }
        })

        edtxt5!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.length == 1) {
                    edtxt6!!.requestFocus()
                    //otp_entered += edtxt5.getText().toString().charAt(0);
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                           after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int,
                                       count: Int) {
                if (edtxt5!!.text.toString().length == 0)
                {
                    edtxt4!!.requestFocus()
                }
                /*if(edtxt5.getText().toString() != null)
                    otp_entered += edtxt5.getText().toString().charAt(0);*/
            }
        })

        edtxt6!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.length == 1) {
                    //otp_entered += edtxt6.getText().toString().charAt(0);
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                           after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int,
                                       count: Int) {
                tvError.visibility = View.INVISIBLE
                /*if(edtxt6.getText().toString() != null)
                    otp_entered += edtxt6.getText().toString().charAt(0);*/
                if (edtxt1!!.text.toString().length == 1 && edtxt2!!.text.toString().length == 1 && edtxt3!!.text.toString().length == 1 && edtxt4!!.text.toString().length == 1 && edtxt5!!.text.toString().length == 1 && edtxt6!!.text.toString().length == 1) {
                    otp_entered = (edtxt1!!.text.toString() + edtxt2!!.text.toString() + edtxt3!!.text.toString()
                            + edtxt4!!.text.toString() + edtxt5!!.text.toString() + edtxt6!!.text.toString())
                    Log.d("TAG_OTP", "otp_entered$otp_entered")
                    postOtp(otp_entered)
                }
                if (edtxt6!!.text.toString().length == 0)
                {
                    edtxt5!!.requestFocus()
                }
            }
        })


        //on pressing the back key in otp edittext
        edtxt2!!.setOnKeyListener { v, keyCode, event -> //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_DEL
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                //this is for backspace
                if (edtxt2!!.length() == 1) {
                    edtxt2!!.setText("")
                } else {
                    edtxt1!!.setText("")
                    edtxt1!!.requestFocus()
                }
            }
            false
        }

        edtxt3!!.setOnKeyListener { v, keyCode, event -> //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_DEL
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                //this is for backspace
                if (edtxt3!!.length() == 1) {
                    edtxt3!!.setText("")
                } else {
                    edtxt2!!.setText("")
                    edtxt2!!.requestFocus()
                }
            }
            false
        }

        edtxt4!!.setOnKeyListener { v, keyCode, event -> //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_DEL
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                //this is for backspace
                if (edtxt4!!.length() == 1) {
                    edtxt4!!.setText("")
                } else {
                    edtxt3!!.setText("")
                    edtxt3!!.requestFocus()
                }
            }
            false
        }

        edtxt5!!.setOnKeyListener { v, keyCode, event -> //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_DEL
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                //this is for backspace
                if (edtxt5!!.length() == 1) {
                    edtxt5!!.setText("")
                } else {
                    edtxt4!!.setText("")
                    edtxt4!!.requestFocus()
                }
            }
            false
        }

        edtxt6!!.setOnKeyListener { v, keyCode, event -> //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_DEL
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                //this is for backspace
                if (edtxt6!!.length() == 1) {
                    edtxt6!!.setText("")
                } else {
                    edtxt5!!.setText("")
                    edtxt5!!.requestFocus()
                }
            }
            false
        }

        tvSubmitOtp!!.setOnClickListener(View.OnClickListener {
            if (otp_entered.length == 6) {
                //if (Utils.checkInternetConnection(Objects.<Context>requireNonNull(OtpVerification.this)))
                //otp_wait_spinner.setVisibility(View.VISIBLE);
                Log.d("TAG_OTP_Internet", "In internet check otp_entered$otp_entered")
                postOtp(otp_entered)

                //if(otp_entered.equals("123456"))
                //OtpResponse o = new OtpResponse();
            }
            else{
                tvError.visibility = View.VISIBLE
            }

        })

        resendOtp!!.setOnClickListener {
            startTimer()
            //   resendOtp!!.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.resendotpbckgnd))

            //resending the otp
            val i = intent
            val phoneNumber = i.getStringExtra(AppConstants.phone)
            // val emailId = i.getStringExtra("USER_EMAIL_ID")
            if (phoneNumber != null) {
                resendOtp(phoneNumber)
            }
        }

    }

    private fun postOtp(otpEntered: String) {
        Utilities.showProgressDialog(this)

        val request = RetrofitClients.buildService(MyAPIService::class.java)
        val call = request.postOtp(Utilities.getPreference(this, AppConstants.EMAIL_ID).toString(),
            AppConstants.API_KEY,
            otpEntered)

        call?.enqueue(object : Callback<OtpResponse> {
            override fun onResponse(call: Call<OtpResponse>, response: Response<OtpResponse>) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful ) {
                    if (response.body()!!.status == 200) {

                        Toast.makeText(this@OtpActivity, response.body()!!.message, Toast.LENGTH_SHORT).show()

                        Utilities.savePrefrence(this@OtpActivity,AppConstants.AUTH_KEY, response.body()!!.authToken)
                        Utilities.savePrefrence(this@OtpActivity, AppConstants.USER_NAME, response.body()!!.userName)
                        Utilities.savePrefrence(this@OtpActivity, AppConstants.USER_EMAIL, response.body()!!.emailId)
                        Utilities.savePrefrence(this@OtpActivity, AppConstants.tokenId, response.body()!!.userId)
                        log("User name(savePrefrence): "+response.body()!!.userName)
                        log("User Email(savePrefrence): "+response.body()!!.emailId)
                        log("Auth token(savePrefrence): "+response.body()!!.authToken)
                        log("User Id(savePrefrence): "+response.body()!!.userId)

                        val intent = Intent(applicationContext, MainDashboardActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                        if (response.body()!!.message == "OTP validated"){
                            intent.putExtra(AppConstants.IS_NEW_USER,true)
                            intent.putExtra(AppConstants.CREDITS_MESSAGE, response.body()!!.displayMessage)
                        }

                        startActivity(intent)

                        tvError.visibility = View.INVISIBLE

                    }else{
                        tvError.visibility = View.VISIBLE
                    }
                }
                else{
                    Toast.makeText(this@OtpActivity, "Server not responding!!!", Toast.LENGTH_SHORT).show()
                }

            }

            override fun onFailure(call: Call<OtpResponse>, t: Throwable) {
                Log.e("ok", "no way")
                Toast.makeText(this@OtpActivity, "Server not responding!!!", Toast.LENGTH_SHORT).show()
                Utilities.hideProgressDialog()
                //Toast.makeText(this@MainActivity, "${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun resendOtp(phoneNumber: String) {
        Utilities.showProgressDialog(this)
        val loginRequest = LoginRequest(Utilities.getPreference(this, AppConstants.EMAIL_ID).toString());

        val request = RetrofitClient.buildService(MyAPIService::class.java)
        val call = request.loginEmailApp(Utilities.getPreference(this, AppConstants.EMAIL_ID).toString(),AppConstants.API_KEY)

        call?.enqueue(object : Callback<LoginResponse>{
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                Utilities.hideProgressDialog()

                if (response.isSuccessful){
                    var loginResponse = response.body()

                    loginResponse?.status.let {
                        if (it == 200){
                            Toast.makeText(
                                this@OtpActivity,
                                loginResponse?.message,
                                Toast.LENGTH_SHORT
                            ).show()

                            if (loginResponse?.userId != null)
                                Utilities.savePrefrence(this@OtpActivity,
                                    AppConstants.tokenId, loginResponse?.userId)

                        }else{
                            Toast.makeText(
                                applicationContext,
                                "Server not responding!, Please try again later",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }else{
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
                    this@OtpActivity,
                    "Server not responding!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    /*
        class MyCountDownTimer
        */
    override fun onBackPressed() {
        super.onBackPressed()
    }
}