package com.spyneai.dashboard.ui


import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.posthog.android.Properties
import com.spyneai.R
import com.spyneai.activity.CategoriesActivity
import com.spyneai.activity.CompletedProjectsActivity
import com.spyneai.activity.OngoingOrdersActivity
import com.spyneai.adapter.CategoriesDashboardAdapter
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.base.network.ServerException
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.adapters.CompletedDashboardAdapter
import com.spyneai.dashboard.adapters.OngoingDashboardAdapter
import com.spyneai.dashboard.adapters.TutorialVideosAdapter
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.dashboard.response.NewCategoriesResponse
import com.spyneai.databinding.HomeDashboardFragmentBinding
import com.spyneai.extras.BeforeAfterActivity
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.response.CompletedSKUsResponse
import com.spyneai.orders.data.response.GetOngoingSkusResponse
import com.spyneai.posthog.Events
import com.spyneai.shoot.utils.log


class HomeDashboardFragment :
    BaseFragment<DashboardViewModel, HomeDashboardFragmentBinding>() {

    lateinit var btnlistener: CategoriesDashboardAdapter.BtnClickListener

    lateinit var categoriesAdapter: CategoriesDashboardAdapter

    lateinit var ongoingDashboardAdapter : OngoingDashboardAdapter

    lateinit var completedDashboardAdapter : CompletedDashboardAdapter
    lateinit var completedProjectList: ArrayList<CompletedSKUsResponse.Data>
    lateinit var ongoingProjectList: ArrayList<GetOngoingSkusResponse.Data>

    lateinit var handler: Handler
    lateinit var runnable: Runnable

    var tutorialVideosList = intArrayOf(R.drawable.ic_tv1, R.drawable.ic_tv2)

    lateinit var tutorialVideosAdapter : TutorialVideosAdapter

    var categoryPosition: Int = 0
    lateinit var tokenId: String
    lateinit var email: String

    lateinit var catId: String
    lateinit var displayName: String
    lateinit var displayThumbnail: String
    lateinit var description: String
    lateinit var colorCode: String
    private var refreshData = true

    private lateinit var tabLayout: TabLayout

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        tokenId = Utilities.getPreference(requireContext(), AppConstants.TOKEN_ID).toString()
        email = Utilities.getPreference(requireContext(), AppConstants.EMAIL_ID).toString()

        if (viewModel.isNewUser.value == true){
            showFreeCreditDialog(viewModel.creditsMessage.value.toString())
            viewModel.isNewUser.value = false
        }

        repeatRefreshData()
        setSliderRecycler()
        showTutorialVideos()
        lisners()

        if (Utilities.getPreference(requireContext(), AppConstants.USER_NAME).toString() != "") {
            binding.tvWelcomeHome.visibility = View.VISIBLE
            binding.viewWelcome.visibility = View.VISIBLE
            binding.tvWelcomeHome.setText("Welcome "+
                Utilities.getPreference(requireContext(), AppConstants.USER_NAME).toString()
            )
            if (Utilities.getPreference(requireContext(), AppConstants.USER_NAME).toString().trim().equals("default")){
                binding.tvWelcomeHome.visibility = View.VISIBLE
                binding.viewWelcome.visibility = View.VISIBLE
                binding.tvWelcomeHome.setText("Welcome Home")
            }
        }

        binding.btGetStarted.setOnClickListener {
            val intent = Intent(requireContext(), CategoriesActivity::class.java)
            startActivity(intent)
        }

        viewModel.getCategories(Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString())
        viewModel.categoriesResponse.observe(viewLifecycleOwner, Observer {
            when(it){
                is Resource.Sucess -> {
                    requireContext().captureEvent(Events.GOT_CATEGORIES, Properties())

                    binding.shimmerCategories.stopShimmer()
                    binding.shimmerCategories.visibility = View.GONE
                    binding.rvDashboardCategories.visibility = View.VISIBLE
                    categoriesAdapter = CategoriesDashboardAdapter(requireContext(),
                        it.value.data as ArrayList<NewCategoriesResponse.Data>,
                        object : CategoriesDashboardAdapter.BtnClickListener {
                            override fun onBtnClick(position: Int) {
                                if (position < 1) {
                                    categoryPosition = position
                                    Utilities.savePrefrence(
                                        requireContext(),
                                        AppConstants.CATEGORY_NAME,
                                        it.value.data[position].prod_cat_name
                                    )

                                    catId =   it.value.data[position].prod_cat_id
                                    displayName =   it.value.data[position].prod_cat_name
                                    displayThumbnail =   it.value.data[position].display_thumbnail
                                    description =   it.value.data[position].description
                                    colorCode =   it.value.data[position].color_code

                                    startBeforeAfter()
                                } else
                                    Toast.makeText(
                                        requireContext(),
                                        "Coming Soon !",
                                        Toast.LENGTH_SHORT
                                    ).show()
                            }

                        })
                    val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    binding.rvDashboardCategories.setLayoutManager(layoutManager)
                    binding.rvDashboardCategories.setAdapter(categoriesAdapter)
//                    categoriesAdapter.notifyDataSetChanged()
                }
                is Resource.Loading -> {
                    binding.shimmerCategories.startShimmer()
                }
                is Resource.Failure -> {
                    requireContext().captureFailureEvent(Events.GET_CATEGORIES_FAILED, Properties(),
                        it.errorMessage!!
                    )
                    handleApiError(it)
                }
            }
        })


        viewModel.completedSkusResponse.observe(viewLifecycleOwner, Observer {
            when(it){
                is Resource.Sucess -> {
                    requireContext().captureEvent(Events.GET_COMPLETED_ORDERS, Properties())
                    completedProjectList = ArrayList()
                    if (it.value.data.isNullOrEmpty()){
                        binding.rlCompletedShoots.visibility = View.GONE
                        refreshData = false
                    }

                    binding.rvCompletedShoots.visibility = View.VISIBLE
                    binding.shimmerCompleted.stopShimmer()
                    binding.shimmerCompleted.visibility = View.GONE
                    if (it.value.data != null){
                        completedProjectList.clear()
                        completedProjectList.addAll(it.value.data)
                        completedProjectList.reverse()

                        if (completedProjectList.size == 0)
                            binding.rlCompletedShoots.visibility = View.GONE

                        completedDashboardAdapter = CompletedDashboardAdapter(requireContext(),
                            completedProjectList)

                        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                        binding.rvCompletedShoots.setLayoutManager(layoutManager)
                        binding.rvCompletedShoots.setAdapter(completedDashboardAdapter)
                    }
                }
                is Resource.Loading -> {
                    binding.shimmerCompleted.startShimmer()
                }
                is Resource.Failure -> {
                    binding.shimmerCompleted.stopShimmer()
                    binding.shimmerCompleted.visibility = View.GONE

                    if (it.errorCode == 404){
                        binding.rlCompletedShoots.visibility = View.GONE
                        refreshData = false
                    }else{
                        requireContext().captureFailureEvent(Events.GET_COMPLETED_ORDERS_FAILED, Properties(),
                            it.errorMessage!!
                        )
                        handleApiError(it)
                    }

                }
            }
        })


        viewModel.getOngoingSkusResponse.observe(
            viewLifecycleOwner, androidx.lifecycle.Observer {
                when (it) {
                    is Resource.Sucess -> {
                        binding.rvOngoingShoots.visibility = View.VISIBLE
                        binding.shimmerOngoing.stopShimmer()
                        binding.shimmerOngoing.visibility = View.GONE
                        if (it.value.data.isNullOrEmpty()){
                            binding.rlOngoingShoots.visibility = View.GONE
                            refreshData = false
                        }

                        if (it.value.data != null){
                            ongoingProjectList = ArrayList()
                            ongoingProjectList.clear()
                            ongoingProjectList.addAll(it.value.data)
                            ongoingDashboardAdapter = OngoingDashboardAdapter(requireContext(),
                                ongoingProjectList
                            )

                            val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                            binding.rvOngoingShoots.setLayoutManager(layoutManager)
                            binding.rvOngoingShoots.setAdapter(ongoingDashboardAdapter)

                        }

                    }
                    is Resource.Loading -> {
                        binding.shimmerOngoing.startShimmer()

                    }
                    is Resource.Failure -> {
                        binding.shimmerOngoing.stopShimmer()
                        binding.shimmerOngoing.visibility = View.GONE

                        if (it.errorCode == 404){
                            binding.rlOngoingShoots.visibility = View.GONE
                            refreshData = false
                        }else{
                            requireContext().captureFailureEvent(Events.GET_ONGOING_ORDERS_FAILED, Properties(),
                                it.errorMessage!!
                            )
                            handleApiError(it)
                        }
                    }

                }
            }
        )

    }

    fun repeatRefreshData(){
        viewModel.getOngoingSKUs(Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString())
        viewModel.getCompletedSKUs(Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString())
        handler = Handler()
        runnable = Runnable { if (refreshData)
            repeatRefreshData()  }
        handler.postDelayed(runnable,15000)
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

    private fun setSliderRecycler(){

        binding.ivBanner.setSliderThumb(ContextCompat.getDrawable(requireContext(),R.drawable.ic_sliderline))

        tabLayout = binding.tbDashboard
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());


        binding.ivBanner.setBeforeImage(ContextCompat.getDrawable(requireContext(),R.drawable.car_before)).setAfterImage(ContextCompat.getDrawable(requireContext(),R.drawable.car_after))
        binding.ivNext.setOnClickListener {
            val tab: TabLayout.Tab = binding.tbDashboard.getTabAt(1)!!
            tab.select()
            binding.ivBanner.setBeforeImage(ContextCompat.getDrawable(requireContext(),R.drawable.footwear_before)).setAfterImage(ContextCompat.getDrawable(requireContext(),R.drawable.footwear_after))
        }

        binding.ivPrevious.setOnClickListener {
            val tab: TabLayout.Tab = binding.tbDashboard.getTabAt(0)!!
            tab.select()
            binding.ivBanner.setBeforeImage(ContextCompat.getDrawable(requireContext(),R.drawable.car_before)).setAfterImage(ContextCompat.getDrawable(requireContext(),R.drawable.car_after))
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0)
                    binding.ivBanner.setBeforeImage(ContextCompat.getDrawable(requireContext(),R.drawable.car_before)).setAfterImage(ContextCompat.getDrawable(requireContext(),R.drawable.car_after))
                else
                    binding.ivBanner.setBeforeImage(ContextCompat.getDrawable(requireContext(),R.drawable.footwear_before)).setAfterImage(ContextCompat.getDrawable(requireContext(),R.drawable.footwear_after))
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

    }


    private fun setOngoingProjectRecycler(){
        viewModel.getOngoingSKUs(Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString())
        viewModel.getOngoingSkusResponse.observe(
            viewLifecycleOwner, androidx.lifecycle.Observer {
                when (it) {
                    is Resource.Sucess -> {
                        if (it.value.data.isNullOrEmpty())
                            binding.rlOngoingShoots.visibility = View.GONE
                        if (it.value.data != null){
                            requireContext().captureEvent(Events.GOT_ONGOING_ORDERS, Properties())
                            ongoingDashboardAdapter = OngoingDashboardAdapter(requireContext(),
                                it.value.data as ArrayList<GetOngoingSkusResponse.Data>
                            )

                            val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                            binding.rvOngoingShoots.setLayoutManager(layoutManager)
                            binding.rvOngoingShoots.setAdapter(ongoingDashboardAdapter)

                        }

                    }
                    is Resource.Loading -> {

                    }
                    is Resource.Failure -> {
                        requireContext().captureFailureEvent(Events.GET_ONGOING_ORDERS_FAILED, Properties(),
                            it.errorMessage!!
                        )
                        handleApiError(it)
                    }

                }
            }
        )

    }



    private fun showTutorialVideos(){
        tutorialVideosAdapter = TutorialVideosAdapter(requireContext(),
            tutorialVideosList,
            object : TutorialVideosAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    if (position == 0){
                        val intent = Intent(requireContext(), YoutubeVideoPlayerActivity::class.java)
                        intent.putExtra(AppConstants.VIDEO_URL, "https://storage.googleapis.com/spyne-cliq/spyne-cliq/AboutVideo/car_spyne.mp4")
                        startActivity(intent)
                    }else{
                        val intent = Intent(requireContext(), YoutubeVideoPlayerActivity::class.java)
                        intent.putExtra(AppConstants.VIDEO_URL, "https://storage.googleapis.com/spyne-cliq/spyne-cliq/AboutVideo/footwear_spyne.mp4")
                        startActivity(intent)
                    }

                }}
        )

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvTutorialVideos.setLayoutManager(layoutManager)
        binding.rvTutorialVideos.setAdapter(tutorialVideosAdapter)
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
        binding.tvCompletedViewall.setOnClickListener {
            val intent = Intent(requireContext(), CompletedProjectsActivity::class.java)
            startActivity(intent)
        }

        binding.tvOngoingViewall.setOnClickListener {
            val intent = Intent(requireContext(), OngoingOrdersActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onPause() {
        handler.removeCallbacks(runnable)
        super.onPause()
    }

//    override fun onResume() {
//        repeatRefreshData()
//        super.onResume()
//    }



    override fun getViewModel() = DashboardViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = HomeDashboardFragmentBinding.inflate(inflater, container, false)

}