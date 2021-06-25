package com.spyneai.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.posthog.android.Properties
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.credits.CreditUtils
import com.spyneai.databinding.DialogTopUpBinding
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClientSpyneAi
import com.spyneai.model.credit.CreditDetailsResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TopUpFragment: DialogFragment() {

    private var _binding : DialogTopUpBinding? = null
    private val binding get() = _binding!!
    private var availableCredits = 0
    private var retry = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        _binding = DialogTopUpBinding.inflate(inflater, container, false)

        isCancelable = false


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivClose.setOnClickListener {
            dismiss()
        }

        fetchUserCreditDetails()
    }

    override fun onResume() {
        super.onResume()
        if (dialog != null){
            getDialog()?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        }
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
                    binding.tvCreditsRemaining.visibility = View.VISIBLE

                    availableCredits = response.body()?.data?.creditAvailable!!

                    if (response.body()?.data?.creditAvailable.toString() == "0"){
                        binding.tvCreditsRemaining.text = "0 Credits Remaining"
                    }else{
                        binding.tvCreditsRemaining.text = CreditUtils.getFormattedNumber(response.body()!!.data.creditAvailable) + " Credits Remaining"
                    }

                    try {

                        requireContext().captureEvent(" Wallet Credits Fetched", Properties())

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
                    }catch (e : IllegalStateException){
                        e.printStackTrace()
                    }

                } else {

                    retry++
                    if (retry < 4){
                        fetchUserCreditDetails()
                    }else{
                        onError()
                    }
                }
            }

            override fun onFailure(call: Call<CreditDetailsResponse>, t: Throwable) {
                binding.shimmer.startShimmer()
                retry++
                if (retry < 4){
                    fetchUserCreditDetails()
                }else{
                    onError()
                }
            }
        })

    }

    private fun onError() {
        try {
            requireContext().captureFailureEvent("Wallet Credits Fetch Failed", Properties(),"Server not responding")
            Toast.makeText(
                requireContext(),
                "Server not responding!!!",
                Toast.LENGTH_SHORT
            ).show()
        }catch (e : IllegalStateException){
            e.printStackTrace()
        }
    }
}