package com.spyneai.orders.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.GridView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout
import com.posthog.android.Properties
import com.spyneai.R
import com.spyneai.base.network.Resource
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.orders.data.viewmodel.MyOrdersViewModel
import com.spyneai.orders.ui.adapter.RawImagesAdapter
import com.spyneai.posthog.Events
import kotlinx.android.synthetic.main.activity_completed_skus.*

class ShowRawImagesActivity : AppCompatActivity() {

    lateinit var gridView: GridView
    lateinit var skuName: TextView
    lateinit var tvTotalImages: TextView
    lateinit var shimmerRawImages: ShimmerFrameLayout
    var position = 0
    var projectPosition = 0
    lateinit var viewModel: MyOrdersViewModel
    val status = "ongoing"
    lateinit var rawImagesAdapter: RawImagesAdapter
    lateinit var imageList: ArrayList<GetProjectsResponse.Images>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_raw_images)

        imageList = ArrayList()

        viewModel = ViewModelProvider(this, ViewModelFactory()).get(MyOrdersViewModel::class.java)

        position = intent.getIntExtra("position", 0)!!
        projectPosition = intent.getIntExtra("projectPosition", 0)!!
        gridView = findViewById(R.id.gvRawImages)
        skuName = findViewById(R.id.tvSkuNam)
        tvTotalImages = findViewById(R.id.tvTotalImage)
        shimmerRawImages = findViewById(R.id.shimmerRawImages)

        viewModel.getProjects(
            Utilities.getPreference(this, AppConstants.AUTH_KEY).toString(), status
        )

        viewModel.getProjectsResponse.observe(
            this, Observer {
                when (it) {
                    is Resource.Success -> {
                        shimmerRawImages.stopShimmer()
                        shimmerRawImages.visibility = View.INVISIBLE
                        gridView.visibility = View.VISIBLE

                        if (it.value.data != null) {
                            imageList.clear()
                            imageList.addAll(it.value.data.project_data[projectPosition].sku[position].images)
                            skuName.text = it.value.data.project_data[projectPosition].sku[position].sku_name
                            tvTotalImages.text = it.value.data.project_data[projectPosition].sku.size.toString()

                            rawImagesAdapter = RawImagesAdapter(
                                this,
                                imageList
                            )
                            gridView.adapter = rawImagesAdapter

                        }
                    }
                    is Resource.Loading -> {
                        shimmerRawImages.startShimmer()
                    }
                    is Resource.Failure -> {
                        shimmerRawImages.stopShimmer()
                        shimmerRawImages.visibility = View.INVISIBLE
                        gridView.visibility = View.VISIBLE

                        if (it.errorCode == 404) {
                            rvSkus.visibility = View.GONE
                        } else {
                            this.captureFailureEvent(
                                Events.GET_COMPLETED_ORDERS_FAILED, Properties(),
                                it.errorMessage!!
                            )

                        }
                    }

                }
            }
        )


    }
}