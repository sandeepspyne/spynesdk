package com.spyneai.videorecording.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.spyneai.R
import com.spyneai.activity.DashboardActivity
import com.spyneai.databinding.DialogExitBinding
import com.spyneai.videorecording.service.UploadVideoService

class DialogExitWarning : DialogFragment() {

    private lateinit var binding : DialogExitBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_exit, container, false )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvSkuNameDialog.text = "Are you sure you want to exit?\n\n all your progress will be lost"

        binding.btnNo.setOnClickListener { dismiss() }

        binding.btnYes.setOnClickListener {
            //stop service
            if (requireActivity() != null){
                val serviceIntent = Intent(requireContext(), UploadVideoService::class.java)
                requireActivity().stopService(serviceIntent)

                //start dashboard activity
                val intent = Intent(requireContext(), DashboardActivity::class.java)
                requireActivity().startActivity(intent)
                requireActivity().finish()
                dismiss()
            }
        }
    }

}
