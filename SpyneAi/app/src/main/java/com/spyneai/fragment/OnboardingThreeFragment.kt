package com.spyneai.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.spyneai.R

class OnboardingThreeFragment : Fragment() {

    private lateinit var tvGetStarted: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

    }
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater!!.inflate(R.layout.activity_onboard_three, container, false)
        // Return the fragment view/layout
        return view

    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            OnboardingThreeFragment().apply {
            }
    }
}