package com.spyneai.loginsignup.activity

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.spyneai.R
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.interfaces.MyAPIService
import com.spyneai.interfaces.RetrofitClientSpyneAi
import com.spyneai.loginsignup.models.ForgotPasswordResponse
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import kotlinx.android.synthetic.main.activity_forgot_password.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ForgotPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        listners()
    }

    private fun listners(){
        bt_sendLink.setOnClickListener {
            if (!et_forgotPasswordEmail.text.toString().trim().isEmpty()
                && Utilities.isValidEmail(et_forgotPasswordEmail.text.toString().trim())) {
                forgotPassword()
                tvErrorEmail.visibility = View.GONE
            }else
                tvErrorEmail.visibility = View.VISIBLE
        }

        bt_forgotLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        tvterms.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.terms_and_conditions_url))))
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    this, "No application can handle this request."
                            + " Please install a web browser", Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            }
        }
    }

    private fun forgotPassword(){
        var properties = HashMap<String,Any?>().
            apply {
               this.put("email", et_forgotPasswordEmail.text.toString().trim())
            }

        captureEvent(Events.FORGOT_PASSWORD_INTIATED,properties)

        Utilities.showProgressDialog(this)

        val request = RetrofitClientSpyneAi.buildService(MyAPIService::class.java)
        val call = request.forgotPassword(
            et_forgotPasswordEmail.text.toString().trim()
        )

        call?.enqueue(object : Callback<ForgotPasswordResponse> {
            override fun onResponse(call: Call<ForgotPasswordResponse>, response: Response<ForgotPasswordResponse>) {
                Utilities.hideProgressDialog()

                if (response.isSuccessful && response.body()!=null) {
                    Utilities.hideProgressDialog()
                    showFreeCreditDialog()
                    captureEvent(Events.FORGOT_PASSWORD_MAIL_SENT,properties)
                }
                else{
                    captureFailureEvent(Events.FORGOT_PASSWORD_FAILED,properties,"Sever not responding")
                    Utilities.hideProgressDialog()
                    Toast.makeText(this@ForgotPasswordActivity, response.errorBody().toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ForgotPasswordResponse>, t: Throwable) {
                captureFailureEvent(Events.FORGOT_PASSWORD_FAILED,properties,t?.localizedMessage)
                Utilities.hideProgressDialog()
                Toast.makeText(
                    applicationContext,
                    "Server not responding!, Please try again later",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun showFreeCreditDialog() {

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)

        var dialogView = LayoutInflater.from(this).inflate(R.layout.forgot_password_dialog, null)

        dialog.setContentView(dialogView)

        dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        val llLogin: LinearLayout = dialog.findViewById(R.id.llLogin)
        var ivClose: ImageView = dialogView.findViewById(R.id.ivClose)
        var lottieEmailSent: LottieAnimationView = dialogView.findViewById(R.id.lottieEmailSent)

//        lottieEmailSent.setPadding(-50, -50, -50, -50);

        ivClose.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
        })

        dialog.show()


        llLogin.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()

        })

    }
}