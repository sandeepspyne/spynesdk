package com.spyneai.credits

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.spyneai.credits.model.ReduceCreditResponse
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

    fun reduceCredit(creditReduced : String,skuId : String,context: Context) {
        //added base url as depedancy
        var call = RetrofitCreditClient("https://www.clippr.ai/api/v4/").buildService(CreditApiService::class.java)

        val userId = RequestBody.create(
            MultipartBody.FORM,Utilities.getPreference(context,AppConstants.tokenId)!!.toString()
        )

        val creditReduced = RequestBody.create(
            MultipartBody.FORM,creditReduced
        )

        val enterpriseId = RequestBody.create(
            MultipartBody.FORM,"TaD1VC1Ko"
        )

        val mSkuId = RequestBody.create(
            MultipartBody.FORM,skuId
        )


        call.reduceCredit(userId,creditReduced,enterpriseId,mSkuId)?.enqueue(object : Callback<ReduceCreditResponse>{
            override fun onResponse(
                call: Call<ReduceCreditResponse>,
                response: Response<ReduceCreditResponse>
            ) {
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

            override fun onFailure(call: Call<ReduceCreditResponse>, t: Throwable) {
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