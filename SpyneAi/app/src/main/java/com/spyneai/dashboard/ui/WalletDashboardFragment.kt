package com.spyneai.dashboard.ui

import android.app.ActionBar
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.posthog.android.Properties
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.credits.CreditUtils
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.databinding.WalletDashboardFragmentBinding
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClientSpyneAi
import com.spyneai.model.credit.CreditDetailsResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class WalletDashboardFragment :
    BaseFragment<DashboardViewModel, WalletDashboardFragmentBinding>()  {

    private var availableCredits = 0
    private var retry = 0
    protected lateinit var rootView: View



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Utilities.getPreference(requireContext(), AppConstants.USER_EMAIL).toString() != ""){
            binding.tvUserName.visibility = View.VISIBLE
            binding.tvUserEmail.visibility = View.VISIBLE
            binding.viewUser.visibility = View.VISIBLE
            binding.tvUserName.setText(Utilities.getPreference(requireContext(), AppConstants.USER_NAME))
            binding.tvUserEmail.setText(Utilities.getPreference(requireContext(), AppConstants.USER_EMAIL))

            if (Utilities.getPreference(requireContext(), AppConstants.USER_NAME).toString().trim().equals("default")){
                binding.tvUserName.visibility = View.GONE
                binding.tvUserName.paddingTop

                val params: LinearLayout.LayoutParams =
                    LinearLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT)
                params.setMargins(10, 20, 10, 10)
                binding.tvUserEmail.setLayoutParams(params)

            }

        }




        binding.flAddCredits.setOnClickListener {
//            val intent = Intent(requireContext(), CreditPlansActivity::class.java)
//            intent.putExtra("from_wallet",true)
//            intent.putExtra("credit_available",availableCredits)
//            startActivity(intent)
        }

        fetchUserCreditDetails()
    }


    private fun fetchUserCreditDetails(){

        binding.shimmer.startShimmer()

        val request = RetrofitClientSpyneAi.buildService(APiService::class.java)
        val call = request.userCreditsDetails(
            Utilities.getPreference(requireContext(), AppConstants.TOKEN_ID).toString()
        )

        call?.enqueue(object : Callback<CreditDetailsResponse> {
            override fun onResponse(
                call: Call<CreditDetailsResponse>,
                response: Response<CreditDetailsResponse>
            ) {
                Utilities.hideProgressDialog()
                binding.shimmer.stopShimmer()

                if (response.isSuccessful) {
                    requireContext().captureEvent(Events.FETCH_CREDITS, Properties())

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
                        requireContext().captureFailureEvent(Events.FETCH_CREDITS_FAILED, Properties(),
                            "Server not responding"
                        )
                        Toast.makeText(
                            requireContext(),
                            "Server not responding!!!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            override fun onFailure(call: Call<CreditDetailsResponse>, t: Throwable) {
                requireContext().captureFailureEvent(Events.FETCH_CREDITS_FAILED, Properties(),
                    t?.localizedMessage
                )
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

    override fun getViewModel() = DashboardViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = WalletDashboardFragmentBinding.inflate(inflater, container, false)

    override fun onDestroyView() {
        super.onDestroyView()
       // _binding = null
    }

}