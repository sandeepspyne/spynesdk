package com.spyneai.loginsignup.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.airbnb.lottie.LottieAnimationView
import com.spyneai.R
import com.spyneai.activity.DashboardActivity
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClientSpyneAi
import com.spyneai.loginsignup.models.ForgotPasswordResponse
import com.spyneai.loginsignup.models.LoginEmailPasswordBody
import com.spyneai.loginsignup.models.LoginEmailPasswordResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_forgot_password.*
import kotlinx.android.synthetic.main.activity_login.*
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
            startActivity(intent)
            finish()
        }
    }

    private fun forgotPassword(){
        Utilities.showProgressDialog(this)

        val request = RetrofitClientSpyneAi.buildService(APiService::class.java)
        val call = request.forgotPassword(
            et_forgotPasswordEmail.text.toString()
        )

        call?.enqueue(object : Callback<ForgotPasswordResponse> {
            override fun onResponse(call: Call<ForgotPasswordResponse>, response: Response<ForgotPasswordResponse>) {
                Utilities.hideProgressDialog()

                if (response.isSuccessful && response.body()!=null) {
                    Utilities.hideProgressDialog()
                    showFreeCreditDialog()

                }
                else{
                    Utilities.hideProgressDialog()
                    Toast.makeText(this@ForgotPasswordActivity, response.errorBody().toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ForgotPasswordResponse>, t: Throwable) {
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

        })

    }
}