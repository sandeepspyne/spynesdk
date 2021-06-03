package com.spyneai.shoot.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.R
import com.spyneai.databinding.FragmentCameraBinding

class CameraFragment : Fragment() {

    private var fragmentCameraBinding: FragmentCameraBinding? = null
    private val binding get() = fragmentCameraBinding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentCameraBinding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

}