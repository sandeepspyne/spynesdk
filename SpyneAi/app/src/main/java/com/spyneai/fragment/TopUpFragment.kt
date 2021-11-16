package com.spyneai.fragment

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.spyneai.R
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.credits.CreditUtils
import com.spyneai.databinding.DialogTopUpBinding
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.model.credit.CreditDetailsResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TopUpFragment: DialogFragment() {

    private var _binding : DialogTopUpBinding? = null
    private val binding get() = _binding
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

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.ivClose?.setOnClickListener {
            dismiss()
        }

        when(getString(R.string.app_name)){
            AppConstants.KARVI,"Yalla Motors","Travo Photos" -> {
                binding?.tvReqCredit?.visibility = View.GONE
                binding?.tvSendRequest?.visibility = View.GONE
            }

            AppConstants.SWEEP -> {
                binding?.tvSendRequest?.setOnClickListener {

                    val selectorIntent = Intent(Intent.ACTION_SENDTO)
                    selectorIntent.data = Uri.parse("mailto:")
                    val emailIntent = Intent(Intent.ACTION_SEND)
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("photos@sweep.ie"))
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Credit Request")
                    emailIntent.putExtra(Intent.EXTRA_TEXT, Utilities.getPreference(requireContext(),AppConstants.USER_EMAIL).toString()+" is requesting 100 credits.")
                    emailIntent.selector = selectorIntent

                    requireContext().startActivity(
                        Intent.createChooser(
                            emailIntent,
                            "Send Credits Request To Sweep Photos"
                        )
                    )

                    dismiss()
                }
            }
            AppConstants.SPYNE_AI->{
                if (Utilities.getPreference(requireContext(),AppConstants.ENTERPRISE_ID)
                    == AppConstants.FLIPKART_ENTERPRISE_ID){
                    binding?.tvSendRequest?.visibility = View.GONE

                }

            }
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

        binding?.shimmer?.startShimmer()

        val request = RetrofitClients.buildService(APiService::class.java)
        val call = request.userCreditsDetails(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString()
        )

        call?.enqueue(object : Callback<CreditDetailsResponse> {
            override fun onResponse(
                call: Call<CreditDetailsResponse>,
                response: Response<CreditDetailsResponse>
            ) {
                Utilities.hideProgressDialog()
                binding?.shimmer?.startShimmer()

                if (response.isSuccessful) {
                    binding?.shimmer?.visibility = View.GONE
                    binding?.tvCreditsRemaining?.visibility = View.VISIBLE

                    availableCredits = response.body()?.data?.credit_available!!

                    if (response.body()?.data?.credit_available.toString() == "0") {
                        binding?.tvCreditsRemaining?.text = "0 "+getString(R.string.credits_remaining)
                    } else {
                        binding?.tvCreditsRemaining?.text =
                            CreditUtils.getFormattedNumber(response.body()!!.data.credit_available) + getString(R.string.credits_remaining)
                    }

                    try {
                        requireContext().captureEvent(" Wallet Credits Fetched", HashMap<String,Any?>())

                        Utilities.savePrefrence(
                            requireContext(),
                            AppConstants.CREDIT_ALLOTED,
                            response.body()?.data?.credit_allotted.toString()
                        )
                        Utilities.savePrefrence(
                            requireContext(),
                            AppConstants.CREDIT_AVAILABLE,
                            response.body()?.data?.credit_available.toString()
                        )
                        Utilities.savePrefrence(
                            requireContext(),
                            AppConstants.CREDIT_USED,
                            response.body()?.data?.credit_used.toString()
                        )
                    } catch (e: IllegalStateException) {
                        e.printStackTrace()
                    }

                } else {

                    onError()
                }
            }

            override fun onFailure(call: Call<CreditDetailsResponse>, t: Throwable) {
                binding?.shimmer?.startShimmer()
                onError()
            }
        })

    }

    private fun onError() {
        try {
            requireContext().captureFailureEvent(
                "Wallet Credits Fetch Failed",
                HashMap<String,Any?>(),
                "Server not responding"
            )
            Toast.makeText(
                requireContext(),
                "Server not responding!!!",
                Toast.LENGTH_SHORT
            ).show()
        }catch (e: IllegalStateException){
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}