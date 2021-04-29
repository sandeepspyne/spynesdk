package com.spyneai.credits

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.model.credit.UpdateCreditResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreditManager {

    var TAG = "CreditManager"

     fun updateCredit(remainingCredit : String, price: String,skuId : String,context: Context) {
         Log.d(TAG, "updateCredit: "+remainingCredit)
         Log.d(TAG, "updateCredit: "+price)
         Log.d(TAG, "updateCredit: "+skuId)
         Log.d(TAG, "updateCredit: "+ Utilities.getPreference(context, AppConstants.tokenId))

        val userId = RequestBody.create(
            MultipartBody.FORM,
            Utilities.getPreference(context, AppConstants.tokenId)!!
        )

        val creditAvailable = RequestBody.create(
            MultipartBody.FORM,
            remainingCredit
        )

        val creditUsed = RequestBody.create(
            MultipartBody.FORM,
            price
        )

        val request = RetrofitClients.buildService(APiService::class.java)
        val call = request.userUpdateCredit(userId, creditAvailable, creditUsed)

        call?.enqueue(object : Callback<UpdateCreditResponse> {
            override fun onResponse(
                call: Call<UpdateCreditResponse>,
                response: Response<UpdateCreditResponse>
            ) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful) {
                    updateCreditOnLocal(context, skuId)
                } else {
                    Log.d(TAG, "onResponse: failed")
                    Toast.makeText(
                        context,
                        "Server not responding!!!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<UpdateCreditResponse>, t: Throwable) {
                Log.d(TAG, "onResponse: failure")
                Toast.makeText(
                    context,
                    "Server not responding!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun updateCreditOnLocal(context: Context, skuId: String) {
       //store sku id if credit used
        Utilities.savePrefrence(context,skuId+AppConstants.CREDIT_DECUCTED, true.toString())
    }
}