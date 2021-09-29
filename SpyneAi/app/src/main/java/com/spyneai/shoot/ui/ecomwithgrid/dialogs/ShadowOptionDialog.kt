package com.spyneai.shoot.ui.ecomwithgrid.dialogs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.posthog.android.Properties
import com.spyneai.R
import com.spyneai.base.BaseDialogFragment
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.ConfirmReshootEcomDialogBinding
import com.spyneai.databinding.DialogShadowOptionBinding
import com.spyneai.gotoHome
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.adapters.ProjectDetailAdapter
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.utils.log
import kotlinx.coroutines.launch

class ShadowOptionDialog : BaseDialogFragment<ShootViewModel, DialogShadowOptionBinding>() {

    var shadow = "false"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCancelable(false)

        binding.tbShadow.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                shadow = "true"
            } else {
                shadow = "false"
            }}

        binding.btConfirm.setOnClickListener {
            processWithShadowOption()
        }
        binding.btSkip.setOnClickListener {
            shadow = "false"
            processWithShadowOption()
        }
    }

    private fun processWithShadowOption() {
        Utilities.showProgressDialog(requireContext())

        viewModel.skuProcessStateWithShadowOption(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            viewModel.projectId.value.toString(),
            shadow)


        viewModel.skuProcessStateWithShadowResponse.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    requireContext().gotoHome()
                }

                is Resource.Failure -> {
                    log("create project id failed")
                    requireContext().captureFailureEvent(
                        Events.SKU_PROCESS_STATE_WITH_SHADOW_FAILED, Properties(),
                        it.errorMessage!!
                    )

                    Utilities.hideProgressDialog()
                    handleApiError(it) { processWithShadowOption() }
                }
            }
        })

    }


    override fun onResume() {
        super.onResume()
        dialog?.getWindow()?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        );
    }


    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogShadowOptionBinding.inflate(inflater, container, false)

}