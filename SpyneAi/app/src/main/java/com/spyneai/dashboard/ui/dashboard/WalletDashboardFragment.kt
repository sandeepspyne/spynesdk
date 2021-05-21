package com.spyneai.dashboard.ui.dashboard

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.spyneai.R
import com.spyneai.credits.CreditPlansActivity
import com.spyneai.credits.CreditUtils
import com.spyneai.databinding.ActivityWalletBinding
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClientSpyneAi
import com.spyneai.model.credit.CreditDetailsResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WalletDashboardFragment : Fragment() {

    private lateinit var binding : ActivityWalletBinding
    private var availableCredits = 0
    private var retry = 0
    protected lateinit var rootView: View


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.wallet_dashboard_fragment, container, false)

        binding = DataBindingUtil.setContentView(requireContext() as Activity,R.layout.activity_wallet)

        binding.flAddCredits.setOnClickListener {
            val intent = Intent(requireContext(), CreditPlansActivity::class.java)
            intent.putExtra("from_wallet",true)
            intent.putExtra("credit_available",availableCredits)
            startActivity(intent)
        }

        fetchUserCreditDetails()

        return rootView

    }

    private fun fetchUserCreditDetails(){

        binding.shimmer.startShimmer()

        val request = RetrofitClientSpyneAi.buildService(APiService::class.java)
        val call = request.userCreditsDetails(
            Utilities.getPreference(requireContext(), AppConstants.tokenId).toString()
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

                    availableCredits = response.body()?.data?.creditAvailable!!

                    if (response.body()?.data?.creditAvailable.toString() == "0"){
                        binding.tvCredits.setTextColor(ContextCompat.getColor(requireContext(),R.color.zero_credits))
                        binding.tvCredits.text = "00"
                    }else{
                        binding.tvCredits.setTextColor(ContextCompat.getColor(requireContext(),R.color.available_credits))
                        binding.tvCredits.text = CreditUtils.getFormattedNumber(response.body()!!.data.creditAvailable)
                    }


                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.CREDIT_ALLOTED,
                        response.body()?.data?.creditAlloted.toString()
                    )
                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.CREDIT_AVAILABLE,
                        response.body()?.data?.creditAvailable.toString()
                    )
                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.CREDIT_USED,
                        response.body()?.data?.creditUsed.toString()
                    )


                } else {

                    retry++
                    if (retry < 4){
                        fetchUserCreditDetails()
                    }else{
                        Toast.makeText(
                            requireContext(),
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
                        requireContext(),
                        "Server not responding!!!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })

    }

}