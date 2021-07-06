package com.spyneai.dashboard.ui

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.Observer
import com.posthog.android.Properties
import com.spyneai.R
import com.spyneai.activity.CompletedProjectsActivity
import com.spyneai.activity.OngoingOrdersActivity
import com.spyneai.adapter.CategoriesDashboardAdapter
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.databinding.HomeDashboardFragmentBinding
import com.spyneai.extras.BeforeAfterActivity
import com.spyneai.fragment.TopUpFragment
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events


class HomeDashboardFragment :
    BaseFragment<DashboardViewModel, HomeDashboardFragmentBinding>() {

    lateinit var btnlistener: CategoriesDashboardAdapter.BtnClickListener

    var categoryPosition: Int = 0
    lateinit var tokenId: String
    lateinit var email: String

    lateinit var catId: String
    lateinit var displayName: String
    lateinit var displayThumbnail: String
    lateinit var description: String
    lateinit var colorCode: String


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        tokenId = Utilities.getPreference(requireContext(), AppConstants.TOKEN_ID).toString()
        email = Utilities.getPreference(requireContext(), AppConstants.EMAIL_ID).toString()

        if (getString(R.string.app_name) == "Karvi.com"){
            binding.ivWallet.visibility = View.GONE
        }

        if (viewModel.isNewUser.value == true && getString(R.string.app_name) != "Karvi.com"){
            showFreeCreditDialog(viewModel.creditsMessage.value.toString())
            viewModel.isNewUser.value = false
        }

        lisners()

        viewModel.getCategories(Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString())
        viewModel.categoriesResponse.observe(viewLifecycleOwner, Observer {
            when(it){
                is Resource.Sucess -> {
                    requireContext().captureEvent(Events.GOT_CATEGORIES, Properties())

                }
                is Resource.Loading -> {

                }
                is Resource.Failure -> {
                    requireContext().captureFailureEvent(Events.GET_CATEGORIES_FAILED, Properties(),
                        it.errorMessage!!)

                    handleApiError(it)
                }
            }
        })
    }


    private fun startBeforeAfter() {
        val intent = Intent(requireContext(), BeforeAfterActivity::class.java)
        intent.putExtra(
            AppConstants.CATEGORY_NAME,
            displayName
        )
        intent.putExtra(
            AppConstants.CATEGORY_ID,
            catId
        )
        intent.putExtra(
            AppConstants.IMAGE_URL,
            displayThumbnail
        )
        intent.putExtra(
            AppConstants.DESCRIPTION,
            description
        )
        intent.putExtra(AppConstants.COLOR, colorCode)
        startActivity(intent)
    }


    private fun showFreeCreditDialog(message: String) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)

        var dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.free_credit_dialog, null)
        var tvMessage: TextView = dialogView.findViewById(R.id.tvSkuNameDialog)
        tvMessage.text = message

        dialog.setContentView(dialogView)

        dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        val llOk: LinearLayout = dialog.findViewById(R.id.llOk)


        llOk.setOnClickListener(View.OnClickListener {

            dialog.dismiss()

        })
        dialog.show()

    }

    private fun lisners(){
        binding.ivWallet.setOnClickListener {
            TopUpFragment().show(requireActivity().supportFragmentManager,"TopUpFragment")
        }

        binding.llCompleted.setOnClickListener {
            val intent = Intent(requireContext(), CompletedProjectsActivity::class.java)
            startActivity(intent)
        }

        binding.llOngoing.setOnClickListener {
            val intent = Intent(requireContext(), OngoingOrdersActivity::class.java)
            startActivity(intent)
        }
    }


    override fun getViewModel() = DashboardViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = HomeDashboardFragmentBinding.inflate(inflater, container, false)

}