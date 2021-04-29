package com.spyneai.credits

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.spyneai.R
import com.spyneai.databinding.ActivityWalletBinding
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClientSpyneAi
import com.spyneai.model.credit.CreditDetailsResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URLEncoder

class WalletActivity : AppCompatActivity() {

    private lateinit var binding : ActivityWalletBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_wallet)


        binding.tvAddCredit.setOnClickListener {
            showWhatsappCreditDialog()
        }

        binding.ivBack.setOnClickListener { onBackPressed() }
        fetchUserCreditDetails()
    }

    private fun showWhatsappCreditDialog(){

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.whatsapp_credit_dialog)
        dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        val ivClose: ImageView = dialog.findViewById(R.id.ivClose)
        val llRequestWappCredit: LinearLayout = dialog.findViewById(R.id.llRequestWappCredit)


        ivClose.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
        })
        llRequestWappCredit.setOnClickListener {
            try {
                val i = Intent(Intent.ACTION_VIEW)
                val url = "https://api.whatsapp.com/send?phone=" + "+919625429526" + "&text=" +
                        URLEncoder.encode(
                            "Spyne App is awesome!, I already used all my free credits. \n" +
                                    "Can you please help me get more credits.",
                            "UTF-8"
                        )
                i.setPackage("com.whatsapp")
                i.setData(Uri.parse(url))
                startActivity(i)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        dialog.show()

    }

    private fun fetchUserCreditDetails(){

        val request = RetrofitClientSpyneAi.buildService(APiService::class.java)
        val call = request.userCreditsDetails(
            Utilities.getPreference(this, AppConstants.tokenId).toString()
        )

        call?.enqueue(object : Callback<CreditDetailsResponse> {
            override fun onResponse(
                call: Call<CreditDetailsResponse>,
                response: Response<CreditDetailsResponse>
            ) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful) {
                    binding.tvCredits.setText(response.body()?.data?.creditAvailable.toString()!!)
                    Utilities.savePrefrence(
                        this@WalletActivity,
                        AppConstants.CREDIT_ALLOTED,
                        response.body()?.data?.creditAlloted.toString()
                    )
                    Utilities.savePrefrence(
                        this@WalletActivity,
                        AppConstants.CREDIT_AVAILABLE,
                        response.body()?.data?.creditAvailable.toString()
                    )
                    Utilities.savePrefrence(
                        this@WalletActivity,
                        AppConstants.CREDIT_USED,
                        response.body()?.data?.creditUsed.toString()
                    )


                } else {
                    Toast.makeText(
                        this@WalletActivity,
                        "Server not responding!!!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }

            override fun onFailure(call: Call<CreditDetailsResponse>, t: Throwable) {
                Toast.makeText(
                    this@WalletActivity,
                    "Server not responding!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

    }

}