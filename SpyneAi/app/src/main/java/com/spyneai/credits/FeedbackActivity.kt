package com.spyneai.credits

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.spyneai.R
import com.spyneai.credits.fragments.FeedbackSubmittedFragment
import com.spyneai.credits.model.InsertReviewResponse
import com.spyneai.credits.model.ReviewHolder
import com.spyneai.dashboard.ui.MainDashboardActivity
import com.spyneai.databinding.ActivityCreditPlansBinding
import com.spyneai.databinding.ActivityFeedbackBinding
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
            var dashboardIntent = Intent(this, MainDashboardActivity::class.java)
            dashboardIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(dashboardIntent)
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

        var call = request.insertReview(Utilities.getPreference(this,AppConstants.tokenId).toString(),
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
        binding.progressBar.visibility = View.GONE
        binding.ivHome.visibility = View.VISIBLE
        backPressAllowed = false

        supportFragmentManager.beginTransaction()
            .add(binding.flContainer.id,FeedbackSubmittedFragment())
            .commit()

    }
}