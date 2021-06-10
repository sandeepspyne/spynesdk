package com.spyneai.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.activity.CompletedProjectsActivity
import com.spyneai.dashboard.response.Data
import com.spyneai.dashboard.response.NewCategoriesResponse
import com.spyneai.extras.BeforeAfterActivity
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClient
import com.spyneai.model.shoot.CreateCollectionRequest
import com.spyneai.model.shoot.CreateCollectionResponse
import com.spyneai.model.shoot.UpdateShootCategoryRequest
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.synnapps.carouselview.ViewListener
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.view_custom.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment(context: Context) : Fragment() {
    val contexts = context
    var contextFrag = context
    lateinit var categoriesResponseList : ArrayList<NewCategoriesResponse.Data>
    lateinit var categoriesAdapter : CategoriesDashboardAdapter
    lateinit var rv_categories : RecyclerView
    val sampleImages = intArrayOf(
        R.mipmap.w1,
        R.mipmap.w2,
        R.mipmap.w3,
        R.mipmap.w4
    )

    @SuppressLint("ValidFragment")
    fun HomeFragment(mContext: Context) {
        contextFrag = mContext
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for contexts fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        setRecycler(view)
        if (Utilities.isNetworkAvailable(contexts))
            fetchCategories(view)
        else
            Toast.makeText(
                contexts,
                "Please check your internet connection",
                Toast.LENGTH_SHORT
            ).show()

        setCarosels(view)
        return  view
    }

    private fun setCarosels(view: View) {
        view.tvCompleted.setOnClickListener(View.OnClickListener {
            val intent = Intent(contexts, CompletedProjectsActivity::class.java)
            contexts.startActivity(intent)
        })

        view.tvOngoing.setOnClickListener(View.OnClickListener {
            /* val intent = Intent(contexts, OngoingActivity::class.java)
            startActivity(intent)*/
            Toast.makeText(
                contexts,
                "Coming Soon !",
                Toast.LENGTH_SHORT
            ).show()

        })

        view.carouselView!!.setPageCount(sampleImages.size);
        view.carouselView.setViewListener(viewListener);

        view.tvHaveLook.setOnClickListener(View.OnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/watch?v=LV9sgTxsIgI")
                )
            )
            Log.i("Video", "Video Playing....")
        })
    }

    var viewListener = object : ViewListener {
        override fun setViewForPosition(position: Int): View? {
            val customView: View = layoutInflater.inflate(R.layout.view_custom, null)
            customView.ivCarosel.setImageResource(sampleImages[position])
            return customView
        }
    }

    private fun setRecycler(view: View) {
        Log.e("Token Mine", Utilities.getPreference(contexts, AppConstants.tokenId).toString())
        categoriesResponseList = ArrayList()
        categoriesAdapter = CategoriesDashboardAdapter(contexts, categoriesResponseList,
            object : CategoriesDashboardAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    Log.e("position cat", position.toString())
                    if (position < 2)
                        setShoot(position)
                    else
                        Toast.makeText(
                            contexts,
                            "Coming Soon !",
                            Toast.LENGTH_SHORT
                        ).show()
                }
            })

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(
            contexts,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        view.rvCategoriesDashboard.setLayoutManager(layoutManager)
        view.rvCategoriesDashboard.setAdapter(categoriesAdapter)

    }

    private fun fetchCategories(view: View) {
        Utilities.showProgressDialog(contexts)
        categoriesResponseList.clear()

        val request = RetrofitClient.buildService(APiService::class.java)
        val call = request.getCategories(Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString())

        call?.enqueue(object : Callback<NewCategoriesResponse> {
            override fun onResponse(
                call: Call<NewCategoriesResponse>,
                response: Response<NewCategoriesResponse>
            ) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful) {

                    var categoriesResponse = response.body()

                    if (categoriesResponse?.status == 200 && categoriesResponse.data.isNotEmpty()){
                        categoriesResponseList.addAll(categoriesResponse.data)

                        categoriesAdapter.notifyDataSetChanged()
                    }else{
                        Toast.makeText(
                            contexts,
                            categoriesResponse?.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }

            override fun onFailure(call: Call<NewCategoriesResponse>, t: Throwable) {
                Log.e("ok", "no way")
                Utilities.hideProgressDialog()
                Toast.makeText(
                    contexts,
                    "Server not responding!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun setShoot( position: Int) {
        Utilities.showProgressDialog(contexts)

        var s = ""
        val createCollectionRequest = CreateCollectionRequest("Spyne Shoot");

        val request = RetrofitClient.buildService(APiService::class.java)
        val call = request.createCollection(
            Utilities.getPreference(contexts, AppConstants.tokenId),
            createCollectionRequest
        )

        call?.enqueue(object : Callback<CreateCollectionResponse> {
            override fun onResponse(
                call: Call<CreateCollectionResponse>,
                response: Response<CreateCollectionResponse>
            ) {
                if (response.isSuccessful) {
                    Log.e("ok", response.body()?.payload?.data?.shootId.toString())
                    Utilities.savePrefrence(
                        contexts,
                        AppConstants.SHOOT_ID,
                        response.body()?.payload?.data?.shootId.toString()
                    )
                    setCategoryMap(response.body()?.payload?.data?.shootId.toString(), position)
                }
            }

            override fun onFailure(call: Call<CreateCollectionResponse>, t: Throwable) {
                Log.e("ok", "no way")

            }
        })
    }

    private fun setCategoryMap(shootId: String, position: Int) {

        val updateShootCategoryRequest = UpdateShootCategoryRequest(
            shootId,
            categoriesResponseList[position].prod_cat_id,
            categoriesResponseList[position].prod_cat_name
        )

        val request = RetrofitClient.buildService(APiService::class.java)
        val call = request.updateShootCategory(
            Utilities.getPreference(contexts, AppConstants.tokenId),
            updateShootCategoryRequest
        )

        call?.enqueue(object : Callback<CreateCollectionResponse> {
            override fun onResponse(
                call: Call<CreateCollectionResponse>,
                response: Response<CreateCollectionResponse>
            ) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful) {
                    val intent = Intent(contexts, BeforeAfterActivity::class.java)
                    intent.putExtra(
                        AppConstants.CATEGORY_ID,
                        categoriesResponseList[position].prod_cat_id
                    )
                    intent.putExtra(
                        AppConstants.CATEGORY_NAME,
                        categoriesResponseList[position].prod_cat_name
                    )
                    intent.putExtra(
                        AppConstants.IMAGE_URL,
                        categoriesResponseList[position].display_thumbnail
                    )
                    intent.putExtra(
                        AppConstants.DESCRIPTION,
                        categoriesResponseList[position].description
                    )
                    intent.putExtra(AppConstants.COLOR, categoriesResponseList[position].color_code)
                    startActivity(intent)
                    Log.e(
                        "Category map",
                        categoriesResponseList[position].prod_cat_id + " " + response.body()!!.msgInfo.msgDescription
                    )
                }
            }

            override fun onFailure(call: Call<CreateCollectionResponse>, t: Throwable) {
                Utilities.hideProgressDialog()
                Log.e("ok", "no way")

            }
        })
    }


}