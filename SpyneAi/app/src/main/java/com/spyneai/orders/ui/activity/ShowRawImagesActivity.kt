package com.spyneai.orders.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.facebook.shimmer.ShimmerFrameLayout
import com.spyneai.R
import com.spyneai.base.network.Resource
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.draft.ui.ImageNotSyncedDialog
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.orders.data.viewmodel.MyOrdersViewModel
import com.spyneai.orders.ui.adapter.RawImagesAdapter
import com.spyneai.posthog.Events
import com.spyneai.shoot.repository.model.image.Image
import kotlinx.android.synthetic.main.activity_completed_skus.*
import kotlinx.coroutines.launch

class ShowRawImagesActivity : AppCompatActivity() {

    lateinit var gridView: GridView
    lateinit var skuName: TextView
    lateinit var tvTotalImages: TextView
    lateinit var shimmerRawImages: ShimmerFrameLayout
    lateinit var ivBack: ImageView
    var position = 0
    var projectPosition = 0
    lateinit var viewModel: MyOrdersViewModel
    val status = "ongoing"
    lateinit var rawImagesAdapter: RawImagesAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_raw_images)

        viewModel = ViewModelProvider(this, ViewModelFactory()).get(MyOrdersViewModel::class.java)

        position = intent.getIntExtra("position", 0)!!
        projectPosition = intent.getIntExtra("projectPosition", 0)!!
        gridView = findViewById(R.id.gvRawImages)
        skuName = findViewById(R.id.tvSkuNam)
        tvTotalImages = findViewById(R.id.tvTotalImage)
        shimmerRawImages = findViewById(R.id.shimmerRawImages)
        ivBack = findViewById(R.id.ivBack)

        ivBack.setOnClickListener {
            onBackPressed()
        }

        fetchImages()

        viewModel.imagesOfSkuRes.observe(this, {
            when (it) {
                is Resource.Success -> {
                    shimmerRawImages.stopShimmer()
                    shimmerRawImages.visibility = View.INVISIBLE
                    gridView.visibility = View.VISIBLE

                    if (it.value.data != null) {
                        val images = it.value.data.map { it.path }


                        skuName.text = intent.getStringExtra(AppConstants.SKU_NAME)
                        tvTotalImages.text = it.value.data.size.toString()

                        rawImagesAdapter = RawImagesAdapter(
                            this,
                            images as ArrayList<String>
                        )
                        gridView.adapter = rawImagesAdapter
                    }
                }

                is Resource.Failure -> {
                    shimmerRawImages.stopShimmer()
                    shimmerRawImages.visibility = View.INVISIBLE
                    gridView.visibility = View.VISIBLE

                    if (it.errorCode == 404) {
                        rvSkus.visibility = View.GONE
                    } else {
                        handleApiError(it) { fetchImages() }
                    }
                }
            }
        })


    }

    private fun fetchImages() {
        lifecycleScope.launch {
            viewModel.getImages(
                intent.getStringExtra(AppConstants.SKU_ID),
                intent.getStringExtra(AppConstants.PROJECT_UUIID).toString(),
                intent.getStringExtra(AppConstants.SKU_UUID).toString()
            )
        }
    }
}