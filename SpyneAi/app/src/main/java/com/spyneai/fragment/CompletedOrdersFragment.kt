package com.spyneai.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.activity.ShowImagesActivity
import com.spyneai.adapter.CompletedProjectAdapter
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.interfaces.Staging
import com.spyneai.model.projects.CompletedProjectResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_completed_projects.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class CompletedOrdersFragment : Fragment() {

    lateinit var completedProjectList: ArrayList<CompletedProjectResponse>
    lateinit var completedProjectAdapter: CompletedProjectAdapter
    protected lateinit var rootView: View
    lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_completed_orders, container, false)

        fatchCompletedProjects()

        return rootView
    }


    private fun fatchCompletedProjects() {
        completedProjectList = ArrayList<CompletedProjectResponse>()

        completedProjectAdapter = CompletedProjectAdapter(requireContext(),
            completedProjectList, object : CompletedProjectAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    Log.e("position cat", position.toString())
                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.SKU_ID,
                        completedProjectList[position].sku_id
                    )
                    val intent = Intent(
                        requireContext(),
                        ShowImagesActivity::class.java
                    )
                    startActivity(intent)

                }
            })

        recyclerView = rootView.findViewById(R.id.rv_completedFragment)
        val layoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView.setLayoutManager(layoutManager)
        recyclerView.setAdapter(completedProjectAdapter)


        Utilities.showProgressDialog(requireContext())

        val request = Staging.buildService(APiService::class.java)
        Utilities.savePrefrence(
            requireContext(), AppConstants.tokenId,
            Utilities.getPreference(requireContext(), AppConstants.tokenId)
        )
        val userId = RequestBody.create(
            MultipartBody.FORM,
            Utilities.getPreference(requireContext(), AppConstants.tokenId)!!
        )
        val enterpriseId = RequestBody.create(
            MultipartBody.FORM,
            "TaD1VC1Ko"
        )
        val call = request.getCompletedOngoingProjects(userId, enterpriseId)
        call?.enqueue(object : Callback<List<CompletedProjectResponse>> {
            override fun onResponse(
                call: Call<List<CompletedProjectResponse>>,
                response: Response<List<CompletedProjectResponse>>
            ) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful) {
                    if (response.body()!!.size > 0) {
                        for (i in 0..response.body()!!.size - 1) {
                            if (response.body()!![i].current_frame == response.body()!![i].total_frames){
                                completedProjectList.addAll(response.body()!!)
                                completedProjectList.reverse()
                            }
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "No projects found !", Toast.LENGTH_SHORT
                        ).show()
                    }

                    completedProjectAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<List<CompletedProjectResponse>>, t: Throwable) {
                Utilities.hideProgressDialog()
                Toast.makeText(requireContext(), "Server not responding!!!", Toast.LENGTH_SHORT)
                    .show()
            }
        })

    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            OnboardingThreeFragment().apply {
            }
    }


}