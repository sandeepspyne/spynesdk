package com.spyneai.shoot.ui.ecom

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import com.spyneai.base.BaseDialogFragment
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.EndProjectDialogBinding
import com.spyneai.shoot.data.ShootViewModel


class EndProjectDialog : BaseDialogFragment<ShootViewModel, EndProjectDialogBinding>() {



    override fun onResume() {
        super.onResume()
        dialog?.getWindow()?.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT);
    }
    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = EndProjectDialogBinding.inflate(inflater, container, false)

}