package com.spyneai.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.spyneai.R

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
        val view: View = inflater!!.inflate(R.layout.activity_onboard_one, container, false)

        // Return the fragment view/layout
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            OnboardingOneFragment().apply { }
    }
}