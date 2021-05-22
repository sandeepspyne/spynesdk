package com.spyneai.dashboard.ui.dashboard

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SimpleOnItemTouchListener
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
import com.spyneai.dashboard.network.Resource
import com.spyneai.dashboard.ui.base.BaseFragment
import com.spyneai.dashboard.ui.handleApiError
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
import kotlinx.android.synthetic.main.rv_dashboard_slider.*
import okhttp3.MultipartBody
import okhttp3.RequestBody


class HomeDashboardFragment :
    BaseFragment<DashboardViewModel, HomeDashboardFragmentBinding>() {

    lateinit var appUpdateManager: AppUpdateManager
    private val MY_REQUEST_CODE: Int = 1
    lateinit var PACKAGE_NAME: String
    lateinit var btnlistener: CategoriesDashboardAdapter.BtnClickListener

    lateinit var categoriesAdapter: CategoriesDashboardAdapter

    lateinit var ongoingDashboardAdapter : OngoingDashboardAdapter

    lateinit var completedDashboardAdapter : CompletedDashboardAdapter

    lateinit var sliderAdapter : SliderAdapter

    lateinit var sliderImageList: ArrayList<SliderModel>

    var tutorialVideosList = intArrayOf(R.drawable.ic_tv1, R.drawable.ic_tv2)

    lateinit var tutorialVideosAdapter : TutorialVideosAdapter

    var handelBaseUrl = 0
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

        tokenId = Utilities.getPreference(requireContext(), AppConstants.tokenId).toString()
        email = Utilities.getPreference(requireContext(), AppConstants.EMAIL_ID).toString()

        Utilities.showProgressDialog(requireContext())
        userFreeCreditEligiblityCheck()
        setOngoingProjectRecycler()
        setSliderRecycler()
        showTutorialVideos()
        lisners()

        btGetStarted.setOnClickListener {
            val intent = Intent(requireContext(), CategoriesActivity::class.java)
            startActivity(intent)
        }

        viewModel.getCategories(tokenId)
        viewModel.categoriesResponse.observe(viewLifecycleOwner, Observer {
            when(it){
                is Resource.Sucess -> {
                    categoriesAdapter = CategoriesDashboardAdapter(requireContext(),
                        it.value.payload.data as ArrayList<Data>,
                        object : CategoriesDashboardAdapter.BtnClickListener {
                            override fun onBtnClick(position: Int) {
                                if (position < 3) {
                                    categoryPosition = position
                                    Utilities.savePrefrence(
                                        requireContext(),
                                        AppConstants.CATEGORY_NAME,
                                        it.value.payload.data[position].displayName
                                    )

                                    catId =   it.value.payload.data[position].catId
                                    displayName =   it.value.payload.data[position].displayName
                                    displayThumbnail =   it.value.payload.data[position].displayThumbnail
                                    description =   it.value.payload.data[position].description
                                    colorCode =   it.value.payload.data[position].colorCode



                                    setShoot(it.value.payload.data as ArrayList<Data>, position)
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
//                is Resource.Loading -> {
//                    Utilities.showProgressDialog(requireContext())
//                }
                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
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
                    Utilities.hideProgressDialog()
                    completedDashboardAdapter = CompletedDashboardAdapter(requireContext(),
                        it.value as ArrayList<CompletedProjectResponse>,
                        object : CompletedDashboardAdapter.BtnClickListener {
                            override fun onBtnClick(position: Int) {
                                Utilities.savePrefrence(requireContext(),
                                    AppConstants.SKU_ID,
                                    it.value[position].sku_id)
                                val intent = Intent(requireContext(),
                                    ShowImagesActivity::class.java)
                                startActivity(intent)

                            }}
                    )

                    val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    rvCompletedShoots.setLayoutManager(layoutManager)
                    rvCompletedShoots.setAdapter(completedDashboardAdapter)

                        if (it.value.size == 0)
                            groupCompletedShoots.visibility = View.GONE

//                    categoriesAdapter.notifyDataSetChanged()
                }
//                is Resource.Loading -> {
//                    Utilities.showProgressDialog(requireContext())
//                }
                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it)
                }
            }
        })


        viewModel.createCollectionResponse.observe(viewLifecycleOwner, Observer {
            when(it){
                is Resource.Sucess -> {
                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.SHOOT_ID,
                        it.value.payload.data.shootId.toString())
                    setCategoryMap(it.value.payload.data.shootId.toString(), categoryPosition, it.value.payload.data.catId, it.value.payload.data.categoryName)
                }
//                is Resource.Loading -> {
//                    Utilities.showProgressDialog(requireContext())
//                }
                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it)
                }
            }
        })

        viewModel.updateShootCategoryResponse.observe(viewLifecycleOwner, Observer {
            when(it){
                is Resource.Sucess -> {
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
//                is Resource.Loading -> {
//                    Utilities.showProgressDialog(requireContext())
//                }
                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it)
                }
            }
        })

        viewModel.freeCreditEligblityResponse.observe(viewLifecycleOwner, Observer {
            when(it){
                is Resource.Sucess -> {
                    if (it.value.status == 200)
                        showFreeCreditDialog(it.value.message)
                }
//                is Resource.Loading -> {
//                    Utilities.showProgressDialog(requireContext())
//                }
                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it)
                }
            }
        })

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private fun setSliderRecycler(){

        ivBanner.setSliderThumb(ContextCompat.getDrawable(requireContext(),R.drawable.ic_sliderline))

        ivBanner.setBeforeImage(ContextCompat.getDrawable(requireContext(),R.drawable.car_before)).setAfterImage(ContextCompat.getDrawable(requireContext(),R.drawable.car_after))
        ivNext.setOnClickListener {
//            val tab: TabLayout.Tab = tbDashboard.getTabAt(1)!!
//            tab.select()
            ivBanner.setBeforeImage(ContextCompat.getDrawable(requireContext(),R.drawable.footwear_before)).setAfterImage(ContextCompat.getDrawable(requireContext(),R.drawable.footwear_after))
        }

        ivPrevious.setOnClickListener {
//            val tab: TabLayout.Tab = tbDashboard.getTabAt(0)!!
//            tab.select()
            ivBanner.setBeforeImage(ContextCompat.getDrawable(requireContext(),R.drawable.car_before)).setAfterImage(ContextCompat.getDrawable(requireContext(),R.drawable.car_after))

        }



    }
//    private fun sliderDots(layoutManager: LinearLayoutManager) {
//        rvSlider.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//                val itemPosition: Int = layoutManager.findFirstCompletelyVisibleItemPosition()
//                if (itemPosition == 0) { //  item position of uses
//                    val tab: TabLayout.Tab = tbDashboard.getTabAt(0)!!
//                    tab.select()
//                } else if (itemPosition == 1) { //  item position of side effects
//                    val tab: TabLayout.Tab = tbDashboard.getTabAt(1)!!
//                    tab.select()
//                } else if (itemPosition == 2) { //  item position of how it works
//                    val tab: TabLayout.Tab = tbDashboard.getTabAt(2)!!
//                    tab.select()
//                } else if (itemPosition == 3) { //  item position of precaution
//                    val tab: TabLayout.Tab = tbDashboard.getTabAt(3)!!
//                    tab.select()
//                }
//            }
//        })
//    }

    private fun setCategoryMap(shootId: String, categoryPosition: Int, catId: String, displayName: String) {
        val updateShootCategoryRequest = UpdateShootCategoryRequest(
            shootId,
            catId,
            displayName
        )
        viewModel.updateShootCategory(tokenId, updateShootCategoryRequest)
    }

    private fun setShoot(categoriesResponseList: ArrayList<Data>, position: Int){
        val createCollectionRequest = CreateCollectionRequest("Spyne Shoot");
        viewModel.createCollection(tokenId, createCollectionRequest)
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
            refreshList()
        }, 3000)

    }

    private fun showHideRecyclerView(tasksInProgress: ArrayList<Task>) {
        if (tasksInProgress.size == 0)
            groupOngoingProjects.visibility = View.GONE
    }

    private fun userFreeCreditEligiblityCheck(){
        val userId = RequestBody.create(
            MultipartBody.FORM,
            tokenId
        )

        val emaiId = RequestBody.create(
            MultipartBody.FORM,
            email
        )
        viewModel.userFreeCreditEligiblityCheck(userId, emaiId)
    }

    private fun showTutorialVideos(){

        tutorialVideosAdapter = TutorialVideosAdapter(requireContext(),
            tutorialVideosList)

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