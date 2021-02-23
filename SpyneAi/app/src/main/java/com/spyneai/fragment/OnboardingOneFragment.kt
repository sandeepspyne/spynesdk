package com.spyneai.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.spyneai.R
import com.spyneai.activity.OnboardingsActivity
import com.spyneai.activity.SignInActivity
import com.spyneai.needs.AppConstants

class OnboardingOneFragment : Fragment() {
    private lateinit var tvGetStarted: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater!!.inflate(R.layout.fragment_onboarding_one, container, false)

        tvGetStarted = view.findViewById(R.id.tv_get_started);
        tvGetStarted.setOnClickListener(View.OnClickListener {
            val intent = Intent(context, OnboardingsActivity::class.java)
            intent.putExtra(AppConstants.ONBOARD,2)
            startActivity(intent)
        })

        // Return the fragment view/layout
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            OnboardingOneFragment().apply { }
    }
}