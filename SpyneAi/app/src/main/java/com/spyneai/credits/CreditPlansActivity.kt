package com.spyneai.credits

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.R
import com.spyneai.credits.adapter.CreditsPlandAdapter
import com.spyneai.credits.model.CreditPlansRes
import com.spyneai.databinding.ActivityCreditPlansBinding
import com.spyneai.interfaces.RetrofitClients
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreditPlansActivity : AppCompatActivity() {

    private lateinit var binding : ActivityCreditPlansBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_credit_plans)

        getCreditplans()

        binding.tvBuyNow.setOnClickListener {
            Toast.makeText(this,"sandeep singh",Toast.LENGTH_LONG).show()
        }
    }

    private fun getCreditplans() {
        binding.sh.startShimmer()

        var request = RetrofitClients.buildService(CreditApiService::class.java)

        var call = request.getThreeSixtyInteriorByShootId()

        call?.enqueue(object : Callback<CreditPlansRes>{
            override fun onResponse(
                call: Call<CreditPlansRes>,
                response: Response<CreditPlansRes>
            ) {
                binding.sh.stopShimmer()

              if (response.isSuccessful){
                  binding.sh.visibility = View.GONE

                  binding.rvCredits.layoutManager = LinearLayoutManager(this@CreditPlansActivity,LinearLayoutManager.VERTICAL,false)
                  var adapter = CreditsPlandAdapter(this@CreditPlansActivity, response.body()!!)
                  binding.rvCredits.visibility = View.VISIBLE
                  binding.rvCredits.adapter = adapter

              }else{
                  Toast.makeText(this@CreditPlansActivity,"Request failed please try again",Toast.LENGTH_LONG).show()
              }
            }

            override fun onFailure(call: Call<CreditPlansRes>, t: Throwable) {
                binding.sh.stopShimmer()

                Toast.makeText(this@CreditPlansActivity,"Request failed please try again",Toast.LENGTH_LONG).show()
            }

        })
    }
}