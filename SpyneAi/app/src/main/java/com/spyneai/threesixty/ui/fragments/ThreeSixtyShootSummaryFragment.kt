package com.spyneai.threesixty.ui.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.WhiteLabelConstants
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.Fragment360ShotSummaryBinding
import com.spyneai.fragment.TopUpFragment
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.service.Actions
import com.spyneai.service.ServerSyncTypes
import com.spyneai.service.getServiceState
import com.spyneai.service.log
import com.spyneai.shoot.ui.dialogs.AngleSelectionDialog
import com.spyneai.startUploadingService
import com.spyneai.threesixty.data.ThreeSixtyViewModel
import com.spyneai.threesixty.data.VideoUploadService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ThreeSixtyShootSummaryFragment : BaseFragment<ThreeSixtyViewModel, Fragment360ShotSummaryBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(requireContext()) // replace with 'this' if it's in activity
            .load(viewModel.videoDetails?.sample360)
            .error(R.mipmap.defaults) // show error drawable if the image is not a gif
            .into(binding.imageViewGif)

        binding.tvTotalCost.text = viewModel.videoDetails?.frames.toString() + " Credits"
        binding.tvSelectedFrames.text = viewModel.videoDetails?.frames.toString() + " Frames"

        binding.tvAvailableCredits.visibility = View.GONE

        binding.btnProceed.isEnabled = true

        binding.tvChangeFidelity.setOnClickListener {
            Navigation.findNavController(binding.tvChangeFidelity)
                .navigate(R.id.action_threeSixtyShootSummaryFragment_to_fidelitySelectionFragment2)

            viewModel.title.value = "Change Fidelity"
        }

        when(getString(R.string.app_name)){
            "Yalla Motors","Travo Photos"-> binding.tvTopUp.visibility = View.GONE

            else-> {
                binding.tvTopUp.setOnClickListener {
                    TopUpFragment().show(requireActivity().supportFragmentManager,"TopUpFragment")
                }
            }
        }

        binding.btnProceed.setOnClickListener {
            //process image call
            uploadWithService()
        }

        viewModel.isFramesUpdated.observe(viewLifecycleOwner,{
            if (it) {
                binding.tvTotalCost.text = viewModel.videoDetails?.frames.toString() + " Credits"
                binding.tvSelectedFrames.text = viewModel.videoDetails?.frames.toString() + " Frames"
            }
        })
    }

    private fun uploadWithService(){
        //update video background id
        GlobalScope.launch(Dispatchers.IO) {
            viewModel.updateBackground(0)
            viewModel.updateVideoBackground()

            GlobalScope.launch(Dispatchers.Main) {
                //start process sync service

                requireContext().startUploadingService(
                    ThreeSixtyShootSummaryFragment::class.java.simpleName,
                    ServerSyncTypes.PROCESS
                )

                //startService()

                Navigation.findNavController(binding.btnProceed)
                    .navigate(R.id.action_threeSixtyShootSummaryFragment_to_videoProcessingStartedFragment)

                viewModel.title.value = "Processing Started"
                viewModel.processingStarted.value = true
            }

        }



//        viewModel.updateVideoBackgroundId()
//
//        startService()
//
//        Navigation.findNavController(binding.btnProceed)
//            .navigate(R.id.action_threeSixtyShootSummaryFragment_to_videoProcessingStartedFragment)
//
//        viewModel.title.value = "Processing Started"
//        viewModel.processingStarted.value = true

//        if (showLoader)
//            Utilities.showProgressDialog(requireContext())

//        viewModel.process360(
//            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString())
//
//        viewModel.process360Res.observe(viewLifecycleOwner,{
//            when(it) {
//                is Resource.Success -> {
//                    //update project status
//                    viewModel.updateProjectStatus(viewModel.videoDetails?.projectId!!)
//
//                    Utilities.hideProgressDialog()
//                    Navigation.findNavController(binding.btnProceed)
//                        .navigate(R.id.action_threeSixtyShootSummaryFragment_to_videoProcessingStartedFragment)
//
//                    viewModel.title.value = "Processing Started"
//                    viewModel.processingStarted.value = true
//                }
//                is Resource.Failure -> {
//                    Utilities.hideProgressDialog()
//                    handleApiError(it) {processSku(true)}
//                }
//            }
//        })
    }

    private fun startService() {
        var action = Actions.START
        if (getServiceState(requireContext()) == com.spyneai.service.ServiceState.STOPPED && action == Actions.STOP)
            return

        val serviceIntent = Intent(requireContext(), VideoUploadService::class.java)
        serviceIntent.action = action.name

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            log("Starting the service in >=26 Mode")
            ContextCompat.startForegroundService(requireContext(), serviceIntent)
            return
        } else {
            log("Starting the service in < 26 Mode")
            requireActivity().startService(serviceIntent)
        }
    }

    override fun getViewModel() = ThreeSixtyViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = Fragment360ShotSummaryBinding.inflate(inflater,container,false)
}