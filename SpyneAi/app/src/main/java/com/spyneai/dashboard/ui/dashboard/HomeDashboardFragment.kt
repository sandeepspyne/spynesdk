package com.spyneai.dashboard.ui.dashboard

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.play.core.appupdate.AppUpdateManager
import com.spyneai.R
import com.spyneai.activity.CompletedProjectsActivity
import com.spyneai.activity.OngoingOrdersActivity
import com.spyneai.adapter.CategoriesDashboardAdapter
import com.spyneai.dashboard.adapters.CompletedDashboardAdapter
import com.spyneai.dashboard.adapters.OngoingDashboardAdapter
import com.spyneai.dashboard.adapters.TutorialVideosAdapter
import com.spyneai.dashboard.data.repository.DashboardRepository
import com.spyneai.dashboard.network.DashboardApi
import com.spyneai.dashboard.network.RemoteDataSourceSpyneAi
import com.spyneai.dashboard.network.Resource
import com.spyneai.dashboard.ui.base.BaseFragment
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.HomeDashboardFragmentBinding
import com.spyneai.extras.BeforeAfterActivity
import com.spyneai.model.categories.Data
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
    BaseFragment<DashboardViewModel, HomeDashboardFragmentBinding, DashboardRepository>() {

    lateinit var appUpdateManager: AppUpdateManager
    private val MY_REQUEST_CODE: Int = 1
    lateinit var PACKAGE_NAME: String
    lateinit var btnlistener: CategoriesDashboardAdapter.BtnClickListener

    lateinit var categoriesResponseList: ArrayList<Data>
    lateinit var categoriesAdapter: CategoriesDashboardAdapter

    lateinit var ongoingDashboardAdapter : OngoingDashboardAdapter
    lateinit var ongoingProjectList : ArrayList<com.spyneai.model.processImageService.Task>

    lateinit var completedProjectList : ArrayList<CompletedProjectResponse>
    lateinit var completedDashboardAdapter : CompletedDashboardAdapter

    var tutorialVideosList = intArrayOf(R.drawable.ic_tv1, R.drawable.ic_tv2)
    lateinit var tutorialVideosAdapter : TutorialVideosAdapter

    var handelBaseUrl = 0

    var categoryPosition: Int = 0

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Utilities.showProgressDialog(requireContext())
        userFreeCreditEligiblityCheck()
        setOngoingProjectRecycler()
        showTutorialVideos()
        lisners()

        categoriesResponseList = ArrayList<Data>()
        viewModel.getCategories("utq10CZvW")
        viewModel.categoriesResponse.observe(viewLifecycleOwner, Observer {
            when(it){
                is Resource.Sucess -> {
                    categoriesResponseList.addAll(it.value.payload.data)
//                    categoriesAdapter.notifyDataSetChanged()
                    setCategoriesRecycler()
                }
//                is Resource.Loading -> {
//                    Utilities.showProgressDialog(requireContext())
//                }
                is Resource.Failure -> {
                    handleApiError(it)
                }
            }
        })

        val userId = RequestBody.create(
            MultipartBody.FORM,
            "utq10CZvW")
        handelBaseUrl = 1
        viewModel.getCompletedProjects(userId)
        completedProjectList = ArrayList<CompletedProjectResponse>()

        viewModel.completedProjectResponse.observe(viewLifecycleOwner, Observer {
            Utilities.hideProgressDialog()
            when(it){
                is Resource.Sucess -> {
                    completedProjectList.addAll(it.value)
                    completedProjectList.reverse()
//                    categoriesAdapter.notifyDataSetChanged()
                    setCompletedProjectRecycler()
                }
//                is Resource.Loading -> {
//                    Utilities.showProgressDialog(requireContext())
//                }
                is Resource.Failure -> {
                    handleApiError(it)
                }
            }
        })


        viewModel.createCollectionResponse.observe(viewLifecycleOwner, Observer {
            Utilities.hideProgressDialog()
            when(it){
                is Resource.Sucess -> {
                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.SHOOT_ID,
                        it.value.payload.data.shootId.toString())
                    setCategoryMap(it.value.payload.data.shootId.toString(), categoryPosition)
                }
//                is Resource.Loading -> {
//                    Utilities.showProgressDialog(requireContext())
//                }
                is Resource.Failure -> {
                    handleApiError(it)
                }
            }
        })

        viewModel.updateShootCategoryResponse.observe(viewLifecycleOwner, Observer {
            Utilities.hideProgressDialog()
            when(it){
                is Resource.Sucess -> {
                    val intent = Intent(requireContext(), BeforeAfterActivity::class.java)
                    intent.putExtra(
                        AppConstants.CATEGORY_ID,
                        categoriesResponseList[categoryPosition].catId
                    )
                    intent.putExtra(
                        AppConstants.CATEGORY_NAME,
                        categoriesResponseList[categoryPosition].displayName
                    )
                    intent.putExtra(
                        AppConstants.IMAGE_URL,
                        categoriesResponseList[categoryPosition].displayThumbnail
                    )
                    intent.putExtra(
                        AppConstants.DESCRIPTION,
                        categoriesResponseList[categoryPosition].description
                    )
                    intent.putExtra(AppConstants.COLOR, categoriesResponseList[categoryPosition].colorCode)
                    startActivity(intent)
                }
//                is Resource.Loading -> {
//                    Utilities.showProgressDialog(requireContext())
//                }
                is Resource.Failure -> {
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
                    handleApiError(it)
                }
            }
        })

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    private fun setCompletedProjectRecycler(){
        Utilities.hideProgressDialog()
        completedDashboardAdapter = CompletedDashboardAdapter(requireContext(),
            completedProjectList)

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvCompletedShoots.setLayoutManager(layoutManager)
        rvCompletedShoots.setAdapter(completedDashboardAdapter)

        hideRecyclerView()

    }

    private fun hideRecyclerView(){
        if (completedProjectList.size == 0)
            groupCompletedShoots.visibility = View.GONE
    }

    private fun setCategoriesRecycler(){
        Utilities.hideProgressDialog()
        categoriesAdapter = CategoriesDashboardAdapter(requireContext(), categoriesResponseList,
            object : CategoriesDashboardAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    if (position < 3) {
                        categoryPosition = position
                        Utilities.savePrefrence(
                            requireContext(),
                            AppConstants.CATEGORY_NAME,
                            categoriesResponseList[position].displayName
                        )
                        setShoot(categoriesResponseList, position)
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
    }

    private fun setCategoryMap(shootId: String, categoryPosition: Int) {
        val updateShootCategoryRequest = UpdateShootCategoryRequest(
            shootId,
            categoriesResponseList[categoryPosition].catId,
            categoriesResponseList[categoryPosition].displayName
        )
        viewModel.updateShootCategory("utq10CZvW", updateShootCategoryRequest)
    }

    private fun setShoot(categoriesResponseList: ArrayList<Data>, position: Int){
        val createCollectionRequest = CreateCollectionRequest("Spyne Shoot");
        viewModel.createCollection("utq10CZvW", createCollectionRequest)
    }

    private fun setOngoingProjectRecycler(){
        ongoingProjectList = ProcessImagesService.tasksInProgress
        ongoingDashboardAdapter = OngoingDashboardAdapter(requireContext(),
            ongoingProjectList)

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvOngoingShoots.setLayoutManager(layoutManager)
        rvOngoingShoots.setAdapter(ongoingDashboardAdapter)
        refreshList()
        showHideRecyclerView()
    }

    private fun refreshList(){
        Handler(Looper.getMainLooper()).postDelayed({
            ongoingDashboardAdapter.notifyDataSetChanged()
            refreshList()
        }, 3000)

    }

    private fun showHideRecyclerView(){
        if (ongoingProjectList.size == 0)
            groupOngoingProjects.visibility = View.GONE
    }

    private fun userFreeCreditEligiblityCheck(){
        val userId = RequestBody.create(
            MultipartBody.FORM,
            "utq105CZvW"
        )

        val emaiId = RequestBody.create(
            MultipartBody.FORM,
            "xxyyz55z@gmail.com"
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

    override fun getFragmentRepository() : DashboardRepository{
        return when(handelBaseUrl){
            0 -> DashboardRepository(RemoteDataSourceSpyneAi("https://www.clippr.ai/api/").buildApi(DashboardApi::class.java))
            1 -> DashboardRepository(RemoteDataSourceSpyneAi("https://www.clippr.ai/api/").buildApi(DashboardApi::class.java))

            else -> DashboardRepository(RemoteDataSourceSpyneAi("https://api.spyne.ai/").buildApi(DashboardApi::class.java))
        }
    }



}