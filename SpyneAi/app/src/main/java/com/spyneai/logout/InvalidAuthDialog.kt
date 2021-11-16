package com.spyneai.logout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.FragmentInvalidAuthDialogBinding
import com.spyneai.gotoLogin
import com.spyneai.shoot.data.ShootViewModel

class InvalidAuthDialog : BaseDialogFragment<ShootViewModel, FragmentInvalidAuthDialogBinding>() {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCancelable(false)

        binding.btnLogin.setOnClickListener {
            requireContext().gotoLogin()
            dismiss()
        }
    }





    override fun onResume() {
        super.onResume()
        dialog?.getWindow()?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT);
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentInvalidAuthDialogBinding.inflate(inflater, container, false)

}