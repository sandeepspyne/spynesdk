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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.startUpdateFlowForResult
import com.spyneai.R
import com.spyneai.activity.CategoriesActivity
import com.spyneai.adapter.CategoriesDashboardAdapter
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.adapters.CompletedDashboardAdapter
import com.spyneai.dashboard.adapters.OngoingDashboardAdapter
import com.spyneai.dashboard.adapters.TutorialVideosAdapter
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.dashboard.response.NewCategoriesResponse
import com.spyneai.databinding.HomeDashboardFragmentBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.orders.ui.MyOrdersActivity
import com.spyneai.posthog.Events
import com.spyneai.shoot.ui.StartShootActivity
import com.spyneai.shoot.ui.base.ShootActivity
import com.spyneai.shoot.ui.base.ShootPortraitActivity
import com.spyneai.shoot.utils.log


class HomeDashboardFragment :
    BaseFragment<DashboardViewModel, HomeDashboardFragmentBinding>() {

    lateinit var btnlistener: CategoriesDashboardAdapter.BtnClickListener

    var categoriesAdapter: CategoriesDashboardAdapter? = null

    lateinit var ongoingDashboardAdapter: OngoingDashboardAdapter

    lateinit var completedDashboardAdapter: CompletedDashboardAdapter
    lateinit var completedProjectList: ArrayList<GetProjectsResponse.Project_data>
    lateinit var ongoingProjectList: ArrayList<GetProjectsResponse.Project_data>
    var categoriesList : ArrayList<NewCategoriesResponse.Data>? = null
    var filteredList = ArrayList<NewCategoriesResponse.Data>()


    lateinit var handler: Handler
    private var runnable: Runnable? = null

    //var tutorialVideosList = intArrayOf(R.drawable.ic_tv1, R.drawable.ic_tv2)

    lateinit var tutorialVideosAdapter: TutorialVideosAdapter

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

    lateinit var appUpdateManager: AppUpdateManager
    private val MY_REQUEST_CODE: Int = 1
    lateinit var PACKAGE_NAME: String

    var tutorialVideosList = intArrayOf(R.drawable.ic_tv1, R.drawable.ic_tv2)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handler = Handler()

        tokenId = Utilities.getPreference(requireContext(), AppConstants.TOKEN_ID).toString()
        email = Utilities.getPreference(requireContext(), AppConstants.EMAIL_ID).toString()

        PACKAGE_NAME = requireContext().packageName.toString()
        appUpdateManager = AppUpdateManagerFactory.create(requireContext())




            binding.tvCatViewall.setOnClickListener {
                if(binding.tvCatViewall.text=="View All"){
                    if (categoriesAdapter != null && categoriesList != null){
                        categoriesAdapter?.categoriesResponseList = categoriesList as ArrayList<NewCategoriesResponse.Data>
                        categoriesAdapter?.notifyDataSetChanged()

                        binding.tvCatViewall.text = "View Less"
                    }
                }else{
                    categoriesAdapter?.categoriesResponseList = filteredList as ArrayList<NewCategoriesResponse.Data>
                    categoriesAdapter?.notifyDataSetChanged()

                    binding.tvCatViewall.text = "View All"
                }
            }



        if (PACKAGE_NAME == "com.spyneai.debug" || PACKAGE_NAME == "com.spyneai.spyneautomobile.debug") {
            newUserCreditDialog()
            repeatRefreshData()
           setSliderRecycler()
            showTutorialVideos()
            lisners()
            welcomeHomeText()
            getCategories()
        } else
            autoUpdates()
    }

    private fun autoUpdates() {

        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {

                // Request the update.
                appUpdateManager.startUpdateFlowForResult(
                    // Pass the intent that is returned by 'getAppUpdateInfo()'.
                    appUpdateInfo,
                    // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                    AppUpdateType.IMMEDIATE,
                    // The current activity making the update request.
                    this,
                    // Include a request code to later monitor this update request.
                    MY_REQUEST_CODE
                )
            } else {

                newUserCreditDialog()
                repeatRefreshData()
                setSliderRecycler()
                showTutorialVideos()
                lisners()
                welcomeHomeText()
                getCategories()
            }
        }
    }

    private fun getOngoingOrders() {
        log("Completed SKUs(auth key): "+ Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY))

        viewModel.getProjects(Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(), "ongoing")

        viewModel.getProjectsResponse.observe(
            viewLifecycleOwner, Observer {
                when (it) {
                    is Resource.Success -> {
                        binding.rvOngoingShoots.visibility = View.VISIBLE
                        binding.shimmerOngoing.stopShimmer()
                        binding.shimmerOngoing.visibility = View.GONE
                        if (it.value.data.project_data.isNullOrEmpty()) {
                            binding.rlOngoingShoots.visibility = View.GONE
                            refreshData = false
                        }

                        if (it.value.data != null) {
                            ongoingProjectList = ArrayList()
                            ongoingProjectList.clear()
                            ongoingProjectList.addAll(it.value.data.project_data)
                            ongoingDashboardAdapter = OngoingDashboardAdapter(
                                requireContext(),
                                ongoingProjectList
                            )

                            val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(
                                requireContext(),
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                            binding.rvOngoingShoots.layoutManager = layoutManager
                            binding.rvOngoingShoots.adapter = ongoingDashboardAdapter

                        }
                    }
                    is Resource.Loading -> {
                        binding.shimmerOngoing.startShimmer()
                    }
                    is Resource.Failure -> {
                        binding.shimmerOngoing.stopShimmer()
                        binding.shimmerOngoing.visibility = View.GONE

                        if (it.errorCode == 404) {
                            binding.rlOngoingShoots.visibility = View.GONE
                            refreshData = false
                        } else {
                            requireContext().captureFailureEvent(
                                Events.GET_ONGOING_ORDERS_FAILED, HashMap<String,Any?>(),
                                it.errorMessage!!
                            )
                            handleApiError(it)
                        }
                    }

                }
            }
        )
    }

    private fun newUserCreditDialog() {
        if (viewModel.isNewUser.value == true) {
            showFreeCreditDialog(viewModel.creditsMessage.value.toString())
            viewModel.isNewUser.value = false
        }
    }

    private fun getCompletedOrders() {

        viewModel.getCompletedProjects(Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(), "completed")

        log("Completed SKUs(auth key): "+ Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY))
        viewModel.getCompletedProjectsResponse.observe(
            viewLifecycleOwner, Observer {
                when (it) {
                    is Resource.Success -> {

                        requireContext().captureEvent(Events.GET_COMPLETED_ORDERS, HashMap<String,Any?>())
                        completedProjectList = ArrayList()
                        if (it.value.data.project_data.isNullOrEmpty()) {
                            binding.rlCompletedShoots.visibility = View.GONE
                            refreshData = false
                        }

                        binding.rvCompletedShoots.visibility = View.VISIBLE
                        binding.shimmerCompleted.stopShimmer()
                        binding.shimmerCompleted.visibility = View.GONE
                        if (it.value.data != null) {
                            completedProjectList.clear()
                            completedProjectList.addAll(it.value.data.project_data)

                            completedDashboardAdapter = CompletedDashboardAdapter(
                                requireContext(),
                                completedProjectList
                            )

                            val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(
                                requireContext(),
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                            binding.rvCompletedShoots.layoutManager = layoutManager
                            binding.rvCompletedShoots.adapter = completedDashboardAdapter
                        }

                    }
                    is Resource.Loading -> {
                        binding.shimmerCompleted.startShimmer()
                    }
                    is Resource.Failure -> {
                        binding.shimmerCompleted.stopShimmer()
                        binding.shimmerCompleted.visibility = View.GONE

                        if (it.errorCode == 404) {
                            binding.rlCompletedShoots.visibility = View.GONE
                            refreshData = false
                        } else {
                            requireContext().captureFailureEvent(
                                Events.GET_COMPLETED_ORDERS_FAILED, HashMap<String,Any?>(),
                                it.errorMessage!!
                            )
                            handleApiError(it)
                        }
                    }

                }
            }
        )

    }

    private fun getCategories() {
        viewModel.getCategories(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString()
        )
        viewModel.categoriesResponse.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    requireContext().captureEvent(Events.GOT_CATEGORIES, HashMap<String,Any?>())

                    binding.shimmerCategories.stopShimmer()
                    binding.shimmerCategories.visibility = View.GONE
                    binding.rvDashboardCategories.visibility = View.VISIBLE

                    categoriesList = it.value.data as ArrayList<NewCategoriesResponse.Data>


                    filteredList.clear()
                        if (categoriesList!!.size > 9){
                            for (i in 0..7){
                                filteredList.add(categoriesList!![i])
                            }
                            binding.tvCatViewall.visibility = View.VISIBLE

                        }
                        else {
                            filteredList = categoriesList as ArrayList<NewCategoriesResponse.Data>
                            binding.tvCatViewall.visibility = View.GONE
                    }





                        categoriesAdapter = CategoriesDashboardAdapter(requireContext(),
                            filteredList!!, object : CategoriesDashboardAdapter.BtnClickListener {
                            override fun onBtnClick(position: Int) {

                                Utilities.savePrefrence(requireContext(), AppConstants.CATEGORY_ID, it.value.data[position].prod_cat_id)

                                catId = it.value.data[position].prod_cat_id
                                displayName = it.value.data[position].prod_cat_name
                                displayThumbnail = it.value.data[position].display_thumbnail
                                description = it.value.data[position].description
                                colorCode = it.value.data[position].color_code

                                when(catId){
                                    AppConstants.CARS_CATEGORY_ID -> {
                                        val intent = Intent(requireContext(), StartShootActivity::class.java)
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
                                    AppConstants.BIKES_CATEGORY_ID -> {
                                        val intent = Intent(requireContext(), ShootActivity::class.java)
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
                                    AppConstants.ECOM_CATEGORY_ID,
                                    AppConstants.FOOTWEAR_CATEGORY_ID,
                                    AppConstants.FOOD_AND_BEV_CATEGORY_ID,
                                    AppConstants.HEALTH_AND_BEAUTY_CATEGORY_ID,
                                    AppConstants.ACCESSORIES_CATEGORY_ID,
                                    AppConstants.WOMENS_FASHION_CATEGORY_ID,
                                    AppConstants.MENS_FASHION_CATEGORY_ID,
                                    AppConstants.CAPS_CATEGORY_ID,
                                    AppConstants.FASHION_CATEGORY_ID,
                                    AppConstants.PHOTO_BOX_CATEGORY_ID-> {
                                        val intent = Intent(requireContext(), ShootPortraitActivity::class.java)
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

                                    else -> {
                                        Toast.makeText(
                                            requireContext(),
                                            "Coming Soon !",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                            }

                        })




                    val layoutManager: RecyclerView.LayoutManager = GridLayoutManager(
                        requireContext(), 4)
                        false

                    binding.rvDashboardCategories.setLayoutManager(layoutManager)
                    binding.rvDashboardCategories.setAdapter(categoriesAdapter)
//                    categoriesAdapter.notifyDataSetChanged()
                }
                is Resource.Loading -> {
                    binding.shimmerCategories.startShimmer()
                }
                is Resource.Failure -> {
                    requireContext().captureFailureEvent(
                        Events.GET_CATEGORIES_FAILED, HashMap<String,Any?>(),
                        it.errorMessage!!
                    )
                    handleApiError(it)
                }
            }
        })
    }

    private fun welcomeHomeText() {
        if (Utilities.getPreference(requireContext(), AppConstants.USER_NAME).toString() != "") {
            binding.tvWelcomeHome.visibility = View.VISIBLE
            binding.viewWelcome.visibility = View.VISIBLE

            val user_name=Utilities.getPreference(requireContext(), AppConstants.USER_NAME).toString()
            if(user_name.isNullOrEmpty()){
                binding.tvWelcomeHome.text = "Welcome "
            }else {
                binding.tvWelcomeHome.text = "Welcome $user_name"}

            if (Utilities.getPreference(requireContext(), AppConstants.USER_NAME).toString().trim()
                    .equals("default")
            ) {
                binding.tvWelcomeHome.visibility = View.VISIBLE
                binding.viewWelcome.visibility = View.VISIBLE
                binding.tvWelcomeHome.text = "Welcome Home"
            }
        }
    }

    fun repeatRefreshData(){
        try {
            getOngoingOrders()
            getCompletedOrders()
            runnable = Runnable {
                if (refreshData)
                    repeatRefreshData()  }
            if (runnable != null)
                handler.postDelayed(runnable!!,15000)
        }catch (e : IllegalArgumentException){
            e.printStackTrace()
        }catch (e : Exception){
            e.printStackTrace()
        }
    }

    override fun onPause() {
        if (runnable != null)
            handler.removeCallbacks(runnable!!)
        super.onPause()
    }




    private fun setSliderRecycler() {

        try {
            binding.ivBanner.setSliderThumb(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_sliderline
                )
            )

            tabLayout = binding.tbDashboard
            tabLayout.addTab(tabLayout.newTab());
            tabLayout.addTab(tabLayout.newTab());


            binding.ivBanner.setBeforeImage(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.car_before)
            ).setAfterImage(ContextCompat.getDrawable(requireContext(), R.drawable.car_after))
//            binding.ivNext.setOnClickListener {
//                val tab: TabLayout.Tab = binding.tbDashboard.getTabAt(1)!!
//                tab.select()
//                binding.ivBanner.setBeforeImage(
//                    ContextCompat.getDrawable(
//                        requireContext(),
//                        R.drawable.footwear_before
//                    )
//                ).setAfterImage(ContextCompat.getDrawable(requireContext(), R.drawable.footwear_after))
//            }

//            binding.ivPrevious.setOnClickListener {
//                val tab: TabLayout.Tab = binding.tbDashboard.getTabAt(0)!!
//                tab.select()
//                binding.ivBanner.setBeforeImage(
//                    ContextCompat.getDrawable(
//                        requireContext(),
//                        R.drawable.car_before
//                    )
//                ).setAfterImage(ContextCompat.getDrawable(requireContext(), R.drawable.car_after))
//            }

            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if (tab?.position == 0)
                        binding.ivBanner.setBeforeImage(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.car_before
                            )
                        ).setAfterImage(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.car_after
                            )
                        )
                    else
                        binding.ivBanner.setBeforeImage(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.footwear_before
                            )
                        ).setAfterImage(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.footwear_after
                            )
                        )
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }
            })
        }catch (e : Exception){
            e.printStackTrace()
        }

    }


    private fun showTutorialVideos() {
        tutorialVideosAdapter = TutorialVideosAdapter(requireContext(),
            tutorialVideosList,
            object : TutorialVideosAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    if (position == 0) {
                        val intent =
                            Intent(requireContext(), YoutubeVideoPlayerActivity::class.java)
                        intent.putExtra(
                            AppConstants.VIDEO_URL,
                            "https://storage.googleapis.com/spyne-cliq/spyne-cliq/AboutVideo/car_spyne.mp4"
                        )
                        startActivity(intent)
                    } else {
                        val intent =
                            Intent(requireContext(), YoutubeVideoPlayerActivity::class.java)
                        intent.putExtra(
                            AppConstants.VIDEO_URL,
                            "https://storage.googleapis.com/spyne-cliq/spyne-cliq/AboutVideo/footwear_spyne.mp4"
                        )
                        startActivity(intent)
                    }

                }
            }
        )

        val layoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvTutorialVideos.setLayoutManager(layoutManager)
        binding.rvTutorialVideos.setAdapter(tutorialVideosAdapter)
    }

    private fun showFreeCreditDialog(message: String) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)

        var dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.free_credit_dialog, null)
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

    private fun lisners() {
        binding.tvCompletedViewall.setOnClickListener {
            val intent = Intent(requireContext(), MyOrdersActivity::class.java)
            intent.putExtra("TAB_ID", 2)
            startActivity(intent)
        }

        binding.tvOngoingViewall.setOnClickListener {
            val intent = Intent(requireContext(), MyOrdersActivity::class.java)
            intent.putExtra("TAB_ID", 1)
            startActivity(intent)
        }
        binding.btGetStarted.setOnClickListener {
            val intent = Intent(requireContext(), CategoriesActivity::class.java)
            startActivity(intent)
        }
    }



    override fun onResume() {
        super.onResume()

        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability()
                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                ) {
                    // If an in-app update is already running, resume the update.
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        this,
                        MY_REQUEST_CODE
                    )
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode != AppCompatActivity.RESULT_OK) {
                activity?.moveTaskToBack(true)
                activity?.finish()
                Toast.makeText(
                    requireContext(),
                    "Update flow failed!" + requestCode,
                    Toast.LENGTH_SHORT
                ).show()

                log("MY_APP\", \"Update flow failed! Result code: "+resultCode)
                // If the update is cancelled or fails,
                // you can request to start the update again.
            }
        }
    }

    override fun getViewModel() = DashboardViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = HomeDashboardFragmentBinding.inflate(inflater, container, false)

}