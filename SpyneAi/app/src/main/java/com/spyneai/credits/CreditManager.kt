package com.spyneai.credits

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.spyneai.credits.model.DownloadHDRes
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
            Utilities.getPreference(context, AppConstants.TOKEN_ID)!!
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

    fun reduceCredit(creditReduced : Int,skuId : String,context: Context) {

        var request = RetrofitClients.buildService(CreditApiService::class.java)

        var call = request.reduceCredit(
            Utilities.getPreference(context,AppConstants.AUTH_KEY)!!.toString(),
            creditReduced.toString(),
            skuId
        )

        call?.enqueue(object : Callback<ReduceCreditResponse> {
            override fun onResponse(
                call: Call<ReduceCreditResponse>,
                response: Response<ReduceCreditResponse>
            ) {
                var s = ""
                if (response.isSuccessful) {
                    Log.d(TAG, "onResponse: reduce success")
                    updateCreditOnServer(context, skuId)
                } else {
                    Log.d(TAG, "onResponse:  reduce failed")
                    Toast.makeText(
                        context,
                        "Server not responding!!!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ReduceCreditResponse>, t: Throwable) {
                Log.d(TAG, "onResponse: reduce failure")
            }
        })
    }

    private fun updateCreditOnServer(context: Context, skuId: String) {

        var call = RetrofitCreditClient("https://www.clippr.ai/api/v4/").buildService(CreditApiService::class.java)
            .updateDownloadStatus(Utilities.getPreference(context,AppConstants.TOKEN_ID)!!.toString(),skuId,
                "WhiteLabelConstants.ENTERPRISE_ID",true)

            call?.enqueue(object : Callback<DownloadHDRes> {
                override fun onResponse(
                    call: Call<DownloadHDRes>,
                    response: Response<DownloadHDRes>
                ) {
                    if (response.isSuccessful) {
                        Log.d(TAG, "onResponse: sucess")
                    }else{
                        Log.d(TAG, "onResponse: failed")
                    }
                }

                override fun onFailure(call: Call<DownloadHDRes>, t: Throwable) {
                    Log.d(TAG, "onResponse: failure")
                }

            })

    }

    private fun updateCreditOnLocal(context: Context, skuId: String) {
       //store sku id if credit used
        Utilities.savePrefrence(context,skuId+AppConstants.CREDIT_DECUCTED, true.toString())
    }
}