package com.spyneai.sdk

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.captureIdentity
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.DialogTopUpBinding
import com.spyneai.databinding.FragmentSignupSdkBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.permissions.Permission
import com.spyneai.permissions.PermissionManager
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.ui.base.ShootActivity

class SignUpSdkFragment : BaseFragment<ShootViewModel,FragmentSignupSdkBinding>() {

    private val permissionManager = PermissionManager.from(this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        permissionManager
            .request(Permission.CameraAndStorage)
            .rationale("We need camera and storage permission to click photos")
            .checkDetailedPermission { result: Map<Permission, Boolean> ->
                if (result.all { it.value }) {
                    signupUser()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Permissions not granted...",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun signupUser() {
        val properties = HashMap<String, Any?>()
        properties.apply {
            this.put("user_id", Spyne.userId)
        }

        viewModel.signupIntoSDK(
            HashMap<String,String>().apply {
                put("external_id", Spyne.userId.toString())
            }
        )

        //SENTRY
        requireContext().captureEvent(Events.LOGIN_INTIATED, properties)

        viewModel.signupIntoSDKRes.observe(
            viewLifecycleOwner, Observer {
                when (it) {
                    is Resource.Success -> {
                        Utilities.hideProgressDialog()
                        //sentry
                        requireContext().captureEvent(Events.LOGIN_SUCCEED, properties)
                        requireContext().captureIdentity(Spyne.userId.toString(), properties)

                        Utilities.savePrefrence(
                            requireContext(),
                            AppConstants.AUTH_KEY,
                            it.value.data.secretKey
                        )
                        Toast.makeText(requireContext(),"Login Success",Toast.LENGTH_LONG).show()

                        //start shoot activity
                        val intent = Intent(requireContext(), ShootActivity::class.java)
                        intent.putExtra(
                            AppConstants.CATEGORY_ID,
                            Utilities.getPreference(requireContext(), AppConstants.CATEGORY_ID))
                        intent.putExtra(AppConstants.CATEGORY_NAME, "Automobiles")
                        startActivity(intent)
                        requireActivity().finish()
                    }
                    is Resource.Failure -> {
                        Utilities.hideProgressDialog()
                        handleApiError(it) { signupUser() }
                        //sentry
                        requireContext().captureFailureEvent(
                            Events.LOGIN_FAILED,
                            properties,
                            it.errorMessage.toString()
                        )
                    }
                }
            }
        )
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    )= FragmentSignupSdkBinding.inflate(inflater,container,false)


}