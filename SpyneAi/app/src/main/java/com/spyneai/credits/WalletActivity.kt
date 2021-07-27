package com.spyneai.credits

import android.app.ActionBar
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.spyneai.R
import com.spyneai.databinding.ActivityWalletBinding
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClientSpyneAi
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.model.credit.CreditDetailsResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.home_dashboard_fragment.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WalletActivity : AppCompatActivity() {

    private lateinit var binding : ActivityWalletBinding
    private var availableCredits = 0
    private var retry = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Utilities.getPreference(this, AppConstants.USER_EMAIL).toString() != ""){
            binding.tvUserEmail.visibility = View.VISIBLE
            binding.viewUser.visibility = View.VISIBLE
            binding.tvUserName.visibility = View.VISIBLE
            binding.tvUserEmail.setText(Utilities.getPreference(this, AppConstants.EMAIL_ID))
            binding.tvUserName.setText(Utilities.getPreference(this, AppConstants.USER_NAME))

            if (Utilities.getPreference(this, AppConstants.USER_NAME).toString().trim().equals("default")){
                binding.tvUserName.visibility = View.GONE

                val params: LinearLayout.LayoutParams =
                    LinearLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT)
                params.setMargins(10, 20, 10, 10)
                binding.tvUserEmail.setLayoutParams(params)
            }
        }



        binding.flAddCredits.setOnClickListener {
            var intent = Intent(this,CreditPlansActivity::class.java)
            intent.putExtra("from_wallet",true)
            intent.putExtra("credit_available",availableCredits)
            startActivity(intent)
        }

        binding.ivBack.setOnClickListener { onBackPressed() }

        fetchUserCreditDetails()
    }


    private fun fetchUserCreditDetails(){

        binding.shimmer.startShimmer()

        val request = RetrofitClients.buildService(APiService::class.java)
        val call = request.userCreditsDetails(
            Utilities.getPreference(this, AppConstants.AUTH_KEY).toString()
        )

        call?.enqueue(object : Callback<CreditDetailsResponse> {
            override fun onResponse(
                call: Call<CreditDetailsResponse>,
                response: Response<CreditDetailsResponse>
            ) {
                Utilities.hideProgressDialog()
                binding.shimmer.startShimmer()

                if (response.isSuccessful) {
                    binding.shimmer.visibility = View.GONE
                    binding.tvCredits.visibility = View.VISIBLE

                    availableCredits = response.body()?.data?.credit_available!!

                    if (response.body()?.data?.credit_available.toString() == "0"){
                        binding.tvCredits.setTextColor(ContextCompat.getColor(this@WalletActivity,R.color.zero_credits))
                        binding.tvCredits.text = "00"
                    }else{
                        binding.tvCredits.setTextColor(ContextCompat.getColor(this@WalletActivity,R.color.available_credits))
                        binding.tvCredits.text = CreditUtils.getFormattedNumber(response.body()!!.data.credit_available)
                    }


                    Utilities.savePrefrence(
                        this@WalletActivity,
                        AppConstants.CREDIT_ALLOTED,
                        response.body()?.data?.credit_allotted.toString()
                    )
                    Utilities.savePrefrence(
                        this@WalletActivity,
                        AppConstants.CREDIT_AVAILABLE,
                        response.body()?.data?.credit_available.toString()
                    )
                    Utilities.savePrefrence(
                        this@WalletActivity,
                        AppConstants.CREDIT_USED,
                        response.body()?.data?.credit_used.toString()
                    )


                } else {

                    retry++
                    if (retry < 4){
                        fetchUserCreditDetails()
                    }else{
                        Toast.makeText(
                            this@WalletActivity,
                            "Server not responding!!!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<CreditDetailsResponse>, t: Throwable) {
                binding.shimmer.startShimmer()
                retry++
                if (retry < 4){
                    fetchUserCreditDetails()
                }else{
                    Toast.makeText(
                        this@WalletActivity,
                        "Server not responding!!!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })

    }

}