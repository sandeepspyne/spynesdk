package com.spyneai.credits

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.spyneai.credits.fragments.FeedbackSubmittedFragment
import com.spyneai.credits.model.InsertReviewResponse
import com.spyneai.credits.model.ReviewHolder
import com.spyneai.databinding.ActivityFeedbackBinding
import com.spyneai.gotoHome
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FeedbackActivity : AppCompatActivity() {

    private lateinit var binding : ActivityFeedbackBinding
    private var TAG = "FeedbackActivity"
    private var backPressAllowed = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        binding.ivHome.setOnClickListener {
            gotoHome()
        }

        binding.tvSubmit.setOnClickListener {
            if (binding.etComment.text.isEmpty()){
                binding.etComment.setError("Please enter you comment here")
            }else{
                submitFeedback()
            }
        }
    }

    override fun onBackPressed() {
        if (backPressAllowed)
            super.onBackPressed()
    }

    private fun submitFeedback() {
        binding.progressBar.visibility = View.VISIBLE
        var request = RetrofitCreditClient("https://www.spyne.ai/credit-user/").buildService(CreditApiService::class.java)

        var call = request.insertReview(Utilities.getPreference(this,AppConstants.TOKEN_ID).toString(),
        binding.etComment.text.toString(),ReviewHolder.editedUrl,intent.getBooleanExtra("like",false),ReviewHolder.orgUrl,"Automobile")


        call?.enqueue(object : Callback<InsertReviewResponse> {
            override fun onResponse(
                call: Call<InsertReviewResponse>,
                response: Response<InsertReviewResponse>
            ) {
                if (response.isSuccessful) {
                    Log.d(TAG, "onResponse: success")
                    onFeedbackSubmitted()
                } else {
                    onFeedbackSubmitted()
                    Log.d(TAG, "onResponse: " + "credit log creation failed")
                }
            }

            override fun onFailure(call: Call<InsertReviewResponse>, t: Throwable) {
                Log.d(TAG, "onFailure: credit log creation failure")
                onFeedbackSubmitted()
            }
        })
    }

    private fun onFeedbackSubmitted() {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etComment.getWindowToken(), 0)

        binding.progressBar.visibility = View.GONE
        binding.ivBack.visibility = View.GONE
        binding.ivHome.visibility = View.VISIBLE
        backPressAllowed = false

        supportFragmentManager.beginTransaction()
            .add(binding.flContainer.id,FeedbackSubmittedFragment())
            .commit()

    }
}