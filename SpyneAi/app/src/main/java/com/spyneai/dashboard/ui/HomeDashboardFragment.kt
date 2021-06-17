package com.spyneai.dashboard.ui

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.android.play.core.appupdate.AppUpdateManager
import com.spyneai.R
import com.spyneai.activity.CategoriesActivity
import com.spyneai.activity.CompletedProjectsActivity
import com.spyneai.activity.OngoingOrdersActivity
import com.spyneai.activity.ShowImagesActivity
import com.spyneai.adapter.CategoriesDashboardAdapter
import com.spyneai.dashboard.adapters.CompletedDashboardAdapter
import com.spyneai.dashboard.adapters.OngoingDashboardAdapter
import com.spyneai.dashboard.adapters.SliderAdapter
import com.spyneai.dashboard.adapters.TutorialVideosAdapter
import com.spyneai.dashboard.data.model.SliderModel
import com.spyneai.base.network.Resource
import com.spyneai.base.BaseFragment
import com.spyneai.dashboard.response.NewCategoriesResponse
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.databinding.HomeDashboardFragmentBinding
import com.spyneai.extras.BeforeAfterActivity
import com.spyneai.model.categories.Data
import com.spyneai.model.processImageService.Task
import com.spyneai.model.projects.CompletedProjectResponse
import com.spyneai.model.shoot.CreateCollectionRequest
import com.spyneai.model.shoot.UpdateShootCategoryRequest
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.service.ProcessImagesService
import kotlinx.android.synthetic.main.home_dashboard_fragment.*
import okhttp3.MultipartBody
import okhttp3.RequestBody


class HomeDashboardFragment :
    BaseFragment<DashboardViewModel, HomeDashboardFragmentBinding>() {

    lateinit var btnlistener: CategoriesDashboardAdapter.BtnClickListener

    lateinit var categoriesAdapter: CategoriesDashboardAdapter

    lateinit var ongoingDashboardAdapter : OngoingDashboardAdapter

    lateinit var completedDashboardAdapter : CompletedDashboardAdapter
    lateinit var completedProjectList: ArrayList<CompletedProjectResponse>

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

    private lateinit var tabLayout: TabLayout

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        tokenId = Utilities.getPreference(requireContext(), AppConstants.tokenId).toString()
        email = Utilities.getPreference(requireContext(), AppConstants.EMAIL_ID).toString()

        setOngoingProjectRecycler()
        setSliderRecycler()
        showTutorialVideos()
        lisners()

        if (Utilities.getPreference(requireContext(), AppConstants.USER_NAME).toString() != "") {
            tvWelcomeHome.visibility = View.VISIBLE
            viewWelcome.visibility = View.VISIBLE
            tvWelcomeHome.setText("Welcome "+
                Utilities.getPreference(requireContext(), AppConstants.USER_NAME).toString()
            )
            if (Utilities.getPreference(requireContext(), AppConstants.USER_NAME).toString().trim().equals("default")){
                tvWelcomeHome.visibility = View.VISIBLE
                viewWelcome.visibility = View.VISIBLE
                tvWelcomeHome.setText("Welcome Home")
            }
        }

        btGetStarted.setOnClickListener {
            val intent = Intent(requireContext(), CategoriesActivity::class.java)
            startActivity(intent)
        }

        viewModel.getCategories(tokenId)
        viewModel.categoriesResponse.observe(viewLifecycleOwner, Observer {
            when(it){
                is Resource.Sucess -> {
                    shimmerCategories.stopShimmer()
                    shimmerCategories.visibility = View.GONE
                    rvDashboardCategories.visibility = View.VISIBLE
                    categoriesAdapter = CategoriesDashboardAdapter(requireContext(),
                        it.value.data as ArrayList<NewCategoriesResponse.Data>,
                        object : CategoriesDashboardAdapter.BtnClickListener {
                            override fun onBtnClick(position: Int) {
                                if (position < 3) {
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
                    rvDashboardCategories.setLayoutManager(layoutManager)
                    rvDashboardCategories.setAdapter(categoriesAdapter)
//                    categoriesAdapter.notifyDataSetChanged()
                }
                is Resource.Loading -> {
                    shimmerCategories.startShimmer()
                }
                is Resource.Failure -> {

                    handleApiError(it)
                }
            }
        })

        val userId = RequestBody.create(
            MultipartBody.FORM,
            tokenId)
        viewModel.getCompletedProjects(userId)

        viewModel.completedProjectResponse.observe(viewLifecycleOwner, Observer {
            when(it){
                is Resource.Sucess -> {
                    rvCompletedShoots.visibility = View.VISIBLE
                    shimmerCompleted.stopShimmer()
                    shimmerCompleted.visibility = View.GONE
                    completedProjectList = ArrayList<CompletedProjectResponse>()
                    completedProjectList.addAll(it.value)
                    completedProjectList.reverse()

                    if (completedProjectList.size == 0)
                        rlCompletedShoots.visibility = View.GONE

                    completedDashboardAdapter = CompletedDashboardAdapter(requireContext(),
                        completedProjectList,
                        object : CompletedDashboardAdapter.BtnClickListener {
                            override fun onBtnClick(position: Int) {
                                Utilities.savePrefrence(requireContext(),
                                    AppConstants.SKU_ID,
                                    completedProjectList[position].sku_id)
                                val intent = Intent(requireContext(),
                                    ShowImagesActivity::class.java)
                                startActivity(intent)

                            }}
                    )

                    val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    rvCompletedShoots.setLayoutManager(layoutManager)
                    rvCompletedShoots.setAdapter(completedDashboardAdapter)




//                    categoriesAdapter.notifyDataSetChanged()
                }
                is Resource.Loading -> {
                    shimmerCompleted.startShimmer()
                }
                is Resource.Failure -> {
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

    private fun setSliderRecycler(){

        ivBanner.setSliderThumb(ContextCompat.getDrawable(requireContext(),R.drawable.ic_sliderline))

        tabLayout = tbDashboard
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());


        ivBanner.setBeforeImage(ContextCompat.getDrawable(requireContext(),R.drawable.car_before)).setAfterImage(ContextCompat.getDrawable(requireContext(),R.drawable.car_after))
        ivNext.setOnClickListener {
            val tab: TabLayout.Tab = tbDashboard.getTabAt(1)!!
            tab.select()
            ivBanner.setBeforeImage(ContextCompat.getDrawable(requireContext(),R.drawable.footwear_before)).setAfterImage(ContextCompat.getDrawable(requireContext(),R.drawable.footwear_after))
        }

        ivPrevious.setOnClickListener {
            val tab: TabLayout.Tab = tbDashboard.getTabAt(0)!!
            tab.select()
            ivBanner.setBeforeImage(ContextCompat.getDrawable(requireContext(),R.drawable.car_before)).setAfterImage(ContextCompat.getDrawable(requireContext(),R.drawable.car_after))

        }
    }


    private fun setOngoingProjectRecycler(){

        ongoingDashboardAdapter = OngoingDashboardAdapter(requireContext(),
            ProcessImagesService.tasksInProgress)

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvOngoingShoots.setLayoutManager(layoutManager)
        rvOngoingShoots.setAdapter(ongoingDashboardAdapter)
        refreshList()
        showHideRecyclerView(ProcessImagesService.tasksInProgress)
    }

    private fun refreshList(){
        Handler(Looper.getMainLooper()).postDelayed({
            ongoingDashboardAdapter.notifyDataSetChanged()
            showHideRecyclerView(ProcessImagesService.tasksInProgress)
            refreshList()
        }, 3000)

    }

    private fun showHideRecyclerView(tasksInProgress: ArrayList<Task>) {
        if (tasksInProgress.size == 0 && groupOngoingProjects!=null)
            groupOngoingProjects.visibility = View.GONE
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
        rvTutorialVideos.setLayoutManager(layoutManager)
        rvTutorialVideos.setAdapter(tutorialVideosAdapter)

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
        tvCompletedViewall.setOnClickListener {
            val intent = Intent(requireContext(), CompletedProjectsActivity::class.java)
            startActivity(intent)
        }

        tvOngoingViewall.setOnClickListener {
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