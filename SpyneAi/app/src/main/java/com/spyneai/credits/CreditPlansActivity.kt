package com.spyneai.credits

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import com.spyneai.R
import com.spyneai.credits.adapter.CreditsPlandAdapter
import com.spyneai.credits.fragments.CreditPaymentFailedFragment
import com.spyneai.credits.fragments.CreditPyamentSuccessFragment
import com.spyneai.credits.model.*
import com.spyneai.databinding.ActivityCreditPlansBinding
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.math.roundToInt

class CreditPlansActivity : AppCompatActivity(),CreditsPlandAdapter.Listener,
    PaymentResultListener {

    private lateinit var adapter: CreditsPlandAdapter
    private lateinit var binding : ActivityCreditPlansBinding
    private var lastSelectedItem : CreditPlansResItem? = null
    private var newSelectedItem : CreditPlansResItem? = null
    private var updateCredit = true
    private var TAG = "CreditPlansActivity"
    private var amount = 0
    private lateinit var razorPayOrderId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_credit_plans)

        Checkout.preload(applicationContext)

        getCreditplans()

        binding.ivWallet.setOnClickListener {
            startActivity(Intent(this,WalletActivity::class.java))
        }

        binding.ivBack.setOnClickListener { onBackPressed() }

        binding.tvBuyNow.setOnClickListener {
           if (lastSelectedItem == null){
               Toast.makeText(this, "Please select a plan ", Toast.LENGTH_LONG).show()
           }else {
               //start Payment Flow
               createOrder()
           }
        }
    }

    private fun createOrder() {
        binding.progressBar.visibility = View.VISIBLE

        var body = CreateOrderBody(
            false, "USD", getOrderId(),
            0, lastSelectedItem!!.price, lastSelectedItem!!.planId.toString(),
            lastSelectedItem!!.price, "CREATED", "New",
            Utilities.getPreference(this, AppConstants.tokenId).toString()
        )

//        var body = CreateOrderBody(
//            false, "INR", "ord_clippr_4e81kot",
//            0, 10, 2.toString(),
//            10, "CREATED", "New",
//            Utilities.getPreference(this, AppConstants.tokenId).toString()
//        )

        var call = RetrofitClientPayment.buildService(CreditApiService::class.java).createOrder(body)


        call?.enqueue(object : Callback<CreateOrderResponse> {
            override fun onResponse(
                call: Call<CreateOrderResponse>,
                response: Response<CreateOrderResponse>
            ) {
                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    var createOrderResponse = response.body()

                    amount  = createOrderResponse?.planFinalCost!!.roundToInt()

                    amount = amount * 100

                    lastSelectedItem!!.finalPrice = amount
                    razorPayOrderId = createOrderResponse.razorpayOrderId

                    prepareCheckOut()
                } else {
                    Toast.makeText(
                        this@CreditPlansActivity,
                        "Server not responded please try again",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<CreateOrderResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE

                Toast.makeText(this@CreditPlansActivity, t.localizedMessage, Toast.LENGTH_LONG).show()
            }
        })

    }

     fun prepareCheckOut() {
        val co = Checkout()
         co.setKeyID("rzp_live_IHhXp2aSS9Ys4p")

        try {
            val options = JSONObject()
            options.put("name","Sypne")
            options.put("description","Buying "+lastSelectedItem!!.credits+" credits")
            //You can omit the image option to fetch the image from dashboard
            options.put("image","https://play-lh.googleusercontent.com/b4BzZiP4gey3FVCXPGQbrX1DNABnoDionTG05HaG2qWeZshkSp33NT2aDSBYOfEQPkU=s360-rw")
            options.put("theme.color", "#FF7700");
            options.put("currency","USD")
            options.put("order_id", razorPayOrderId)
            options.put("amount",amount)//pass amount in currency subunits

            val retryObj = JSONObject()
            retryObj.put("enabled", false)
            retryObj.put("max_count", 0)
            options.put("retry", retryObj)

//            val prefill = JSONObject()
//            prefill.put("email","gaurav.kumar@example.com")
//            prefill.put("contact","9876543210")

           // options.put("prefill",prefill)
            co.open(this,options)
        }catch (e: Exception){
            Toast.makeText(this,"Error in payment: "+ e.message,Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun getOrderId(): String {
        return "ord_clippr_"+ getSaltString()
    }

    private fun getSaltString(): String? {
        val SALTCHARS = "abcdefghijklmnopqrstuvwxyz1234567890"
        val salt = StringBuilder()
        val rnd = Random()
        while (salt.length < 7) { // length of the random string.
            //val index = (rnd.nextFloat() * SALTCHARS.length) as Int
                val index = rnd.nextInt(SALTCHARS.length)
            salt.append(SALTCHARS[index])
        }
        return salt.toString()
    }


    private fun getCreditplans() {
        binding.sh.startShimmer()

        var request = RetrofitCreditClient("https://www.clippr.ai/api/v2/").buildService(CreditApiService::class.java)

        var call = request.getThreeSixtyInteriorByShootId()

        call?.enqueue(object : Callback<CreditPlansRes> {
            override fun onResponse(
                call: Call<CreditPlansRes>,
                response: Response<CreditPlansRes>
            ) {
                binding.sh.stopShimmer()

                if (response.isSuccessful) {
                    binding.sh.visibility = View.GONE

                    setData(response.body())

                } else {
                    Toast.makeText(
                        this@CreditPlansActivity,
                        "Request failed please try again",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<CreditPlansRes>, t: Throwable) {
                binding.sh.stopShimmer()

                Toast.makeText(
                    this@CreditPlansActivity,
                    "Request failed please try again",
                    Toast.LENGTH_LONG
                ).show()
            }

        })
    }

    private fun setData(body: CreditPlansRes?) {
        binding.rvCredits.layoutManager = LinearLayoutManager(
            this@CreditPlansActivity,
            LinearLayoutManager.VERTICAL,
            false
        )
        adapter = CreditsPlandAdapter(this@CreditPlansActivity, body!!, this)
        binding.rvCredits.visibility = View.VISIBLE
        binding.rvCredits.adapter = adapter
    }

    override fun onSelected(item: CreditPlansResItem, position : Int) {
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

        lastSelectedItem!!.planId = position

        binding.tvPricePerImage.text = "＄ "+ lastSelectedItem!!.pricePerImage
    }

    override fun onPaymentSuccess(razorpayPaymentID: String?) {
        //update credit
        GlobalScope.launch(Dispatchers.IO) {
            createCreditPurchaseLog()
        }

            try {
                var fragment = CreditPyamentSuccessFragment()

                var agrs = Bundle()

                agrs.putInt("amount",lastSelectedItem!!.credits)
                fragment.arguments = agrs

                binding.tvBuyNow.visibility = View.GONE

                supportFragmentManager.beginTransaction()
                    .add(R.id.fl_container,fragment)
                    .commitAllowingStateLoss()

            }catch (ex : Exception){

            }


    }

    override fun onPaymentError(code: Int, response: String?) {
        Log.d(TAG, "onPaymentError: "+response)
       if (code == Checkout.PAYMENT_CANCELED){
           Toast.makeText(this,"Payment Cancelled",Toast.LENGTH_LONG).show()
       }else{
           binding.tvBuyNow.visibility = View.GONE

           supportFragmentManager.beginTransaction()
               .add(R.id.fl_container,CreditPaymentFailedFragment())
               .commitAllowingStateLoss()
       }
    }

    private suspend fun createCreditPurchaseLog() {
        var call = RetrofitCreditClient("https://www.spyne.ai/").buildService(CreditApiService::class.java)
            .createCreditPurchaseLog(Utilities.getPreference(this,AppConstants.tokenId)!!.toString(),
            lastSelectedItem!!.creditId,
            lastSelectedItem!!.credits,Utilities.getPreference(this, AppConstants.CREDIT_USED).toString(),
                Utilities.getPreference(this, AppConstants.CREDIT_AVAILABLE).toString()
            )

        call?.enqueue(object : Callback<CreditPurchaseLogRes> {
            override fun onResponse(
                call: Call<CreditPurchaseLogRes>,
                response: Response<CreditPurchaseLogRes>
            ) {
                if (response.isSuccessful) {
                    GlobalScope.launch(Dispatchers.IO) {
                        updatePurchasedCredits()
                    }
                } else {
                    Log.d(TAG, "onResponse: "+"credit log creation failed")
                }
            }

            override fun onFailure(call: Call<CreditPurchaseLogRes>, t: Throwable) {
                Log.d(TAG, "onFailure: credit log creation failure")
            }
        })
    }

    private suspend fun updatePurchasedCredits() {
        var creditAvailable = lastSelectedItem!!.credits + intent.getIntExtra("credit_available",0)

        Log.d(TAG, "updatePurchasedCredits: "+ lastSelectedItem!!.credits)
        Log.d(TAG, "updatePurchasedCredits: "+creditAvailable)


        var call = RetrofitCreditClient("https://www.spyne.ai/").buildService(CreditApiService::class.java)
            .updatePurchasedCredit(Utilities.getPreference(this,AppConstants.tokenId)!!.toString(),
                lastSelectedItem!!.credits,Utilities.getPreference(this,AppConstants.CREDIT_USED)!!.toString(),creditAvailable)

        call?.enqueue(object : Callback<UpdatePurchaseCreditRes>{
            override fun onResponse(
                call: Call<UpdatePurchaseCreditRes>,
                response: Response<UpdatePurchaseCreditRes>
            ) {
                if (response.isSuccessful) {
                    Log.d(TAG, "onResponse: "+"credit UDPATE SUCCESS")
                } else {
                    Log.d(TAG, "onResponse: "+"credit log creation failed")
                }
            }

            override fun onFailure(call: Call<UpdatePurchaseCreditRes>, t: Throwable) {
                Log.d(TAG, "onFailure: credit UDPATE  failure")
            }

        })

    }



}