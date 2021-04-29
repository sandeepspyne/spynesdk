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
import com.spyneai.credits.model.CreditPlansResItem
import com.spyneai.databinding.ActivityCreditPlansBinding
import com.spyneai.interfaces.RetrofitClients
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.FieldPosition

class CreditPlansActivity : AppCompatActivity(),CreditsPlandAdapter.Listener {

    private lateinit var adapter: CreditsPlandAdapter
    private lateinit var binding : ActivityCreditPlansBinding
    private var lastSelectedItem : CreditPlansResItem? = null
    private var newSelectedItem : CreditPlansResItem? = null

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

                  setData(response.body())

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

    private fun setData(body: CreditPlansRes?) {
        binding.rvCredits.layoutManager = LinearLayoutManager(this@CreditPlansActivity,LinearLayoutManager.VERTICAL,false)
        adapter = CreditsPlandAdapter(this@CreditPlansActivity, body!!,this)
        binding.rvCredits.visibility = View.VISIBLE
        binding.rvCredits.adapter = adapter
    }

    override fun onSelected(item: CreditPlansResItem) {
          if (lastSelectedItem == null){
              lastSelectedItem = item
              lastSelectedItem!!.isSelected = true

              adapter.notifyDataSetChanged()


          }else{
              if (!lastSelectedItem!!.equals(item)){
                  lastSelectedItem!!.isSelected = false
                  newSelectedItem = item
                  newSelectedItem!!.isSelected = true

                  adapter.notifyDataSetChanged()

                  lastSelectedItem = newSelectedItem
              }
          }
    }
}