package com.spyneai.processedimages.ui

import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.core.app.NotificationCompat
import androidx.core.view.MotionEventCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.spyneai.R
import com.spyneai.activity.DownloadingActivity
import com.spyneai.activity.OrderSummary2Activity
import com.spyneai.activity.ShowGifActivity
import com.spyneai.adapter.ShowReplacedImagesAdapter
import com.spyneai.adapter.ShowReplacedImagesFocusedAdapter
import com.spyneai.adapter.ShowReplacedImagesInteriorAdapter
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentProcessedImagesBinding
import com.spyneai.databinding.ViewImagesBinding
import com.spyneai.gotoHome
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.model.skumap.UpdateSkuResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.ScrollingLinearLayoutManager
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.response.ImagesOfSkuRes
import com.spyneai.processedimages.ui.data.ProcessedViewModel
import com.spyneai.videorecording.fragments.DialogEmbedCode
import com.spyneai.videorecording.model.TSVParams
import com.synnapps.carouselview.CarouselView
import com.synnapps.carouselview.ViewListener
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ProcessedImagesFragment : BaseFragment<ProcessedViewModel, FragmentProcessedImagesBinding>(),
    View.OnTouchListener,View.OnClickListener{

    private lateinit var frontFramesList: ArrayList<String>
    lateinit var tsvParamFront : TSVParams
    var handler = Handler()
    var shootId = ""

    lateinit var builder: NotificationCompat.Builder
    lateinit var imageList: List<String>
    lateinit var imageListAfter: List<String>
    lateinit var imageListInterior: List<String>
    lateinit var imageListFocused: List<String>

    lateinit var imageListWaterMark: ArrayList<String>
    lateinit var listHdQuality: ArrayList<String>
    lateinit var imageNameList: ArrayList<String>
    var catName: String = ""
    var numberOfImages: Int = 0

    private lateinit var showReplacedImagesAdapter: ShowReplacedImagesAdapter
    private lateinit var ShowReplacedImagesInteriorAdapter: ShowReplacedImagesInteriorAdapter
    private lateinit var ShowReplacedImagesFocusedAdapter: ShowReplacedImagesFocusedAdapter

    var downloadCount: Int = 0
    lateinit var Category: String
    var TAG = "ShowImagesActivity"

    var downloadHighQualityCount: Int = 5
    lateinit var intent : Intent

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        intent = requireActivity().intent

        viewModel.projectId = intent.getStringExtra(AppConstants.PROJECT_ID)
        viewModel.skuId = intent.getStringExtra(AppConstants.SKU_ID)
        viewModel.skuName = intent.getStringExtra(AppConstants.SKU_NAME)

        frontFramesList = ArrayList()

        setBulkImages()

        setListeners()

        imageNameList = ArrayList()


        if (intent.getStringExtra(AppConstants.CATEGORY_NAME) != null)
            catName = intent.getStringExtra(AppConstants.CATEGORY_NAME)!!
        else
            catName = Utilities.getPreference(requireContext(), AppConstants.CATEGORY_NAME)!!

        //checkThreeSixtyInterior()

        if (catName.equals("Footwear")) {
            binding.tvViewGif.visibility = View.GONE
        }

        observeSkuData()

        binding.tvReshoot.setOnClickListener {
            val imagesResponse = (viewModel.imagesOfSkuRes.value as Resource.Success).value

            val element = imagesResponse.data.firstOrNull {
                it.overlayId == null
            }

            if (element == null)
                viewModel.reshoot.value = true
            else
                Toast.makeText(requireContext(),"Reshoot not applciable for old shoots",Toast.LENGTH_LONG).show()
        }
    }

    private fun observeSkuData() {

        viewModel.imagesOfSkuRes.observe(viewLifecycleOwner,{
            when(it){
                is Resource.Success -> {
                    Utilities.hideProgressDialog()

                    var dataList: List<ImagesOfSkuRes.Data> = it.value.data

                    val s = ""
                    for (i in 0..(dataList.size) -1) {
                        if (dataList!![i].image_category.equals("Exterior")) {
                            Category = dataList!![i].image_category
                            (imageList as ArrayList).add(dataList!![i].input_image_lres_url)
                            (imageListAfter as ArrayList).add(dataList!![i].output_image_lres_wm_url)

                            //save for in case of user review
//                            if (imageListAfter != null && imageList.size > 0)
//                                ReviewHolder.orgUrl = imageList.get(0)
//
//                            if (imageListAfter != null && imageListAfter.size > 0)
//                                ReviewHolder.editedUrl = imageListAfter.get(0)

                            (imageListWaterMark as ArrayList).add(dataList!![i].output_image_lres_wm_url)
                            (listHdQuality as ArrayList).add(dataList!![i].output_image_hres_url)

                            imageNameList.add(dataList[i].image_name)
                            frontFramesList.add(dataList!![i].output_image_lres_url)

                            Utilities.savePrefrence(
                                requireContext(),
                                AppConstants.NO_OF_IMAGES,
                                imageListAfter.size.toString()
                            )

                            hideData(0)
                        } else  if (dataList!![i].image_category.equals("Food") || dataList!![i].image_category.equals("Food & Beverages") || dataList!![i].image_category.equals("E-Commerce")) {
                            Category = dataList!![i].image_category
                            (imageList as ArrayList).add(dataList!![i].input_image_lres_url)
                            (imageListAfter as ArrayList).add(dataList!![i].output_image_lres_wm_url)

//                            //save for in case of user review
//                            if (imageListAfter != null && imageList.size > 0)
//                                ReviewHolder.orgUrl = imageList.get(0)
//
//                            if (imageListAfter != null && imageListAfter.size > 0)
//                                ReviewHolder.editedUrl = imageListAfter.get(0)

                            (imageListWaterMark as ArrayList).add(dataList!![i].output_image_lres_wm_url)
                            (listHdQuality as ArrayList).add(dataList!![i].output_image_hres_url)

                            imageNameList.add(dataList[i].image_name)
                            frontFramesList.add(dataList!![i].output_image_lres_url)

                            Utilities.savePrefrence(
                                requireContext(),
                                AppConstants.NO_OF_IMAGES,
                                imageListAfter.size.toString()
                            )

                            binding.tvYourEmailIdReplaced.text = "Images"
                            hideData(0)
                        }

                        else if (dataList!![i].image_category.equals("Interior")) {
                            Category = dataList!![i].image_category
                            (imageListInterior as ArrayList).add(dataList!![i].output_image_lres_url)
                            (imageListWaterMark as ArrayList).add(dataList!![i].output_image_lres_wm_url)
                            (listHdQuality as ArrayList).add(dataList!![i].output_image_hres_url)
                            imageNameList.add(dataList[i].image_name)

                            Utilities.savePrefrence(
                                requireContext(),
                                AppConstants.NO_OF_IMAGES,
                                imageListAfter.size.toString()
                            )
                            hideData(0)
                        } else if (dataList!![i].image_category.equals("Focus Shoot")) {
                            Category = dataList!![i].image_category
                            (imageListFocused as ArrayList).add(dataList!![i].output_image_lres_url)
                            (imageListWaterMark as ArrayList).add(dataList!![i].output_image_lres_wm_url)
                            (listHdQuality as ArrayList).add(dataList!![i].output_image_hres_url)
                            imageNameList.add(dataList[i].image_name)

                            Utilities.savePrefrence(
                                requireContext(),
                                AppConstants.NO_OF_IMAGES,
                                imageListAfter.size.toString()
                            )
                            hideData(0)
                        } else {
                            Category = dataList!![i].image_category
                            (imageList as ArrayList).add(dataList!![i].input_image_lres_url)
                            (imageListAfter as ArrayList).add(dataList!![i].output_image_lres_wm_url)
                            (listHdQuality as ArrayList).add(dataList!![i].output_image_hres_url)
                            (imageListWaterMark as ArrayList).add(dataList!![i].output_image_lres_wm_url)
                            imageNameList.add(dataList[i].image_name)

                            Utilities.savePrefrence(
                                requireContext(),
                                AppConstants.NO_OF_IMAGES,
                                imageListAfter.size.toString()
                            )
                            hideData(1)
                        }
                    }

                    //show 360 view
                    if (intent.getBooleanExtra(AppConstants.IS_360,false))
                        showThreeSixtyView()

                    showReplacedImagesAdapter.notifyDataSetChanged()
                    ShowReplacedImagesInteriorAdapter.notifyDataSetChanged()
                    ShowReplacedImagesFocusedAdapter.notifyDataSetChanged()
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it){fetchBulkUpload()}
                }
            }
        })
    }

    private fun hideData(i: Int) {

        if (i == 0) {
            binding.tvYourEmailIdReplaced.visibility = View.VISIBLE
            binding.tvViewGif.visibility = View.GONE
            binding.tvInterior.visibility = View.GONE
            binding.tvFocused.visibility = View.GONE
//            llDownloads.visibility = View.VISIBLE
        } else {
            binding.tvYourEmailIdReplaced.visibility = View.GONE
            binding.tvViewGif.visibility = View.GONE
            binding.tvInterior.visibility = View.GONE
            binding.tvFocused.visibility = View.GONE
//            llDownloads.visibility = View.GONE
        }
    }

    private fun setListeners() {
        binding.tvViewGif.setOnClickListener(View.OnClickListener {
            val intent = Intent(
                requireContext(),
                ShowGifActivity::class.java
            )
            startActivity(intent)
        })

        binding.ivBackShowImages.setOnClickListener(View.OnClickListener {
            requireActivity().onBackPressed()
        })

        binding.ivHomeShowImages.setOnClickListener(View.OnClickListener {
            requireActivity().gotoHome()

            val updateSkuResponseList = ArrayList<UpdateSkuResponse>()
            updateSkuResponseList.clear()

            Utilities.setList(
                requireContext(),
                AppConstants.FRAME_LIST, updateSkuResponseList
            )
        })

        if (getString(R.string.app_name) == AppConstants.SWEEP){
            binding.tvDownloadFree.visibility = View.GONE
        }else {
            binding.tvDownloadFree.setOnClickListener {
                Utilities.savePrefrence(requireContext(), AppConstants.DOWNLOAD_TYPE, "watermark")
                val downloadIntent = Intent(requireContext(), DownloadingActivity::class.java)
                downloadIntent.putExtra(AppConstants.LIST_WATERMARK, imageListWaterMark)
                downloadIntent.putExtra(AppConstants.LIST_HD_QUALITY, listHdQuality)
                downloadIntent.putExtra(AppConstants.LIST_IMAGE_NAME, imageNameList)
                downloadIntent.putExtra("is_paid",intent.getBooleanExtra("is_paid",false))
                startActivity(downloadIntent)
            }
        }

        binding.llDownloadHdImages.setOnClickListener {
            Utilities.savePrefrence(requireContext(), AppConstants.DOWNLOAD_TYPE, "hd")
            val orderIntent = Intent(requireContext(), OrderSummary2Activity::class.java)
            orderIntent.putExtra(AppConstants.LIST_WATERMARK, imageListWaterMark)
            orderIntent.putExtra(AppConstants.LIST_HD_QUALITY, listHdQuality)
            orderIntent.putExtra(AppConstants.LIST_IMAGE_NAME, imageNameList)
            orderIntent.putExtra("is_paid",intent.getBooleanExtra("is_paid",false))

            var skuId = Utilities.getPreference(requireContext(), AppConstants.SKU_ID)
                .toString()

            var skuName = Utilities.getPreference(requireContext(), AppConstants.SKU_ID)
                .toString()

            orderIntent.putExtra(AppConstants.SKU_ID,skuId)
            orderIntent.putExtra(AppConstants.SKU_NAME,skuName)
            orderIntent.putExtra(AppConstants.IMAGE_TYPE,intent.getStringExtra(AppConstants.IMAGE_TYPE))
            startActivity(orderIntent)
        }

        binding.ivShare.setOnClickListener(this)
        binding.ivEmbed.setOnClickListener(this)
    }


    private fun setBulkImages() {
        Utilities.showProgressDialog(requireContext())
        imageList = ArrayList<String>()
        imageListAfter = ArrayList<String>()
        imageListWaterMark = ArrayList<String>()
        imageListInterior = ArrayList<String>()
        imageListFocused = ArrayList<String>()
        listHdQuality = ArrayList<String>()

        showReplacedImagesAdapter = ShowReplacedImagesAdapter(requireContext(),
            imageList as ArrayList<String>,
            imageListAfter as ArrayList<String>,
            object : ShowReplacedImagesAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    showImagesDialog(position)
                    Log.e("position preview", position.toString())
                }
            })

        ShowReplacedImagesInteriorAdapter = ShowReplacedImagesInteriorAdapter(requireContext(),
            imageListInterior as ArrayList<String>,
            object : ShowReplacedImagesInteriorAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    //   showImagesDialog(position)
                    Log.e("position preview", position.toString())
                }
            })


        ShowReplacedImagesFocusedAdapter = ShowReplacedImagesFocusedAdapter(requireContext(),
            imageListFocused as ArrayList<String>,
            object : ShowReplacedImagesFocusedAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    //   showImagesDialog(position)
                    Log.e("position preview", position.toString())
                }
            })

        binding.rvImagesBackgroundRemoved.setLayoutManager(
            ScrollingLinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                false
            )
        )

        binding.rvInteriors.setLayoutManager(
            GridLayoutManager(
                requireContext(),
                2
            )
        )

        binding.rvFocused.setLayoutManager(
            GridLayoutManager(
                requireContext(),
                2
            )
        )

        binding.rvImagesBackgroundRemoved.setAdapter(showReplacedImagesAdapter)
        binding.rvInteriors.setAdapter(ShowReplacedImagesInteriorAdapter)
        binding.rvFocused.setAdapter(ShowReplacedImagesFocusedAdapter)

        fetchBulkUpload()
    }

    //Fetch bulk data
    private fun fetchBulkUpload() {
        //Utilities.showProgressDialog(requireContext())

        shootId = Utilities.getPreference(requireContext(), AppConstants.SKU_ID)!!

        getSkuImages()
    }

    private fun showThreeSixtyView() {
        binding.llThreeSixtyView.visibility = View.VISIBLE

        tsvParamFront = TSVParams()
        tsvParamFront.type = 0
        tsvParamFront.framesList = frontFramesList
        tsvParamFront.mImageIndex = frontFramesList.size / 2

        binding.svFront.startShimmer()

        preLoadFront(tsvParamFront)

        //load front image
        Glide.with(this)
            .load(frontFramesList.get(tsvParamFront.mImageIndex))
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.ivFront.visibility = View.VISIBLE
                    binding.svFront.stopShimmer()
                    binding.svFront.visibility = View.GONE

                    //show images and set listener
                    binding.clFront.visibility = View.VISIBLE

                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.ivFront.visibility = View.VISIBLE
                    binding.svFront.stopShimmer()
                    binding.svFront.visibility = View.GONE

                    //show images and set listener
                    binding.clFront.visibility = View.VISIBLE
                    return false
                }

            })
            .into(binding.ivFront)
    }

    fun showImagesDialog(position: Int) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_show_images)

        val window: Window = dialog.getWindow()!!
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        val carouselViewImages: CarouselView = dialog.findViewById(R.id.carouselViewImages)
        val ivCrossImages: ImageView = dialog.findViewById(R.id.ivCrossImages)

        ivCrossImages.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
        })

        carouselViewImages.setPageCount(imageList.size);
        carouselViewImages.setViewListener(viewListener);
        carouselViewImages.setCurrentItem(position)

        dialog.show()
    }

    var viewListener = object : ViewListener {
        override fun setViewForPosition(position: Int): View? {
            val customView: View = layoutInflater.inflate(R.layout.view_images, null)
            val customBiding = ViewImagesBinding.bind(customView)


            Glide.with(requireContext())
                .load(imageList[position])
                .apply(
                    RequestOptions()
                        .error(com.spyneai.R.mipmap.defaults)
                )
                .listener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(
                        @Nullable e: GlideException?,
                        model: Any,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        //on load failed
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any,
                        target: Target<Drawable?>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        //on load success
                        return false
                    }
                })
                .into(customBiding.ivBefore)

//            Glide.with(this@ShowImagesActivity) // replace with 'this' if it's in activity
//                .load(imageListAfter[position])
//                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
//                .error(R.mipmap.defaults) // show error drawable if the image is not a gif
//                .into(customView.ivAfter)

            Glide.with(requireContext())
                .load(imageListAfter[position])
                .apply(
                    RequestOptions()
                        .error(com.spyneai.R.mipmap.defaults)
                )
                .listener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(
                        @Nullable e: GlideException?,
                        model: Any,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        //on load failed
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any,
                        target: Target<Drawable?>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        //on load success
                        return false
                    }
                })
                .into(customBiding.ivAfter)

            return customView
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {

        var action = MotionEventCompat.getActionMasked(event)

        when(v?.id){
            R.id.iv_front -> {
                when(action){
                    MotionEvent.ACTION_DOWN -> {
                        tsvParamFront.mStartX = event!!.x.toInt()
                        tsvParamFront.mStartY = event.y.toInt()
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        tsvParamFront.mEndX = event!!.x.toInt()
                        tsvParamFront.mEndY = event.y.toInt()

                        if (tsvParamFront.mEndX - tsvParamFront.mStartX > 8) {
                            tsvParamFront.mImageIndex++
                            if (tsvParamFront.mImageIndex >= tsvParamFront.framesList.size) tsvParamFront.mImageIndex = 0

                            loadImage(tsvParamFront,binding.ivFront)

                        }
                        if (tsvParamFront.mEndX - tsvParamFront.mStartX < -8) {
                            tsvParamFront.mImageIndex--
                            if (tsvParamFront.mImageIndex < 0) tsvParamFront.mImageIndex = tsvParamFront.framesList.size - 1

                            loadImage(tsvParamFront,binding.ivFront)
                        }
                        tsvParamFront.mStartX = event.x.toInt()
                        tsvParamFront.mStartY = event.y.toInt()

                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        tsvParamFront.mEndX = event!!.x.toInt()
                        tsvParamFront.mEndY = event.y.toInt()

                        return true
                    }
                    MotionEvent.ACTION_CANCEL -> return true
                    MotionEvent.ACTION_OUTSIDE -> return true
                }
            }
        }

        return false
    }

    override fun onClick(v: View?) {
        when(v?.id){

            R.id.ivEmbed -> {
                embed(getCode(0))
            }

            R.id.ivShare -> {
                share(getLink())
            }

            R.id.tv_go_to_home -> {
                requireActivity().gotoHome()
            }
        }
    }

    private fun embed(code: String) {
        var args = Bundle()
        args.putString("code",code)

        var dialogCopyEmbeddedCode = DialogEmbedCode()
        dialogCopyEmbeddedCode.arguments = args
        dialogCopyEmbeddedCode.show(requireActivity().supportFragmentManager,"DialogEmbedCode")
    }

    private fun share(code: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, code)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private fun getCode(type : Int) : String {
        return "<iframe \n" +
                "  src=\"https://www.spyne.ai/shoots/shoot?skuId="+shootId+"&type=exterior" +
                "  style=\"border:0; height: 100%; width: 100%;\" framerborder=\"0\"></iframe>"

    }

    private fun getLink() = "https://www.spyne.ai/shoots/shoot?skuId="+shootId+"&type=exterior"

    private fun preLoadFront(tsvParams: TSVParams) {
        for ((index, url) in tsvParams.framesList.withIndex()) {

            Glide.with(this)
                .load(url)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d(TAG, "onResourceReady: failed")
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d(TAG, "onResourceReady: paseed " + index)


                        if (index == tsvParams.framesList.size - 1) {

                            binding.ivFront.setOnTouchListener(this@ProcessedImagesFragment)
                        }

                        return false
                    }

                })
                .dontAnimate()
                .override(250, 250)
                .preload()

        }

        setListeners()
    }

    private fun loadImage(tsvParams: TSVParams, imageView: ImageView) {

        handler.removeCallbacksAndMessages(null)

        handler.postDelayed({

            Log.d(TAG, "loading: a"+tsvParams.type)
            Log.d(TAG, "loading: a"+tsvParams.framesList.get(tsvParams.mImageIndex))


            try {
                var glide = Glide.with(this)
                    .load(tsvParams.framesList.get(tsvParams.mImageIndex))

                if (tsvParams.placeholder != null)
                    glide.placeholder(tsvParams.placeholder)

                glide.listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d(TAG, "onResourceReady: failed")
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        tsvParams.placeholder = resource!!

                        return false
                    }

                })
                    .override(250, 250)
                    .dontAnimate()
                    .into(imageView)


                if (binding.ivFront.visibility == View.INVISIBLE) binding.ivFront.visibility = View.VISIBLE
            } catch (ex: UninitializedPropertyAccessException) {
                Log.d(TAG, "loadImage: ex " + tsvParams.type)
                Log.d(TAG, "loadImage: ex " + ex.localizedMessage)

            }
        }, 10)
    }

    private fun getSkuImages() {
       // Utilities.showProgressDialog(requireContext())

        viewModel.getImagesOfSku(
            Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString(),
            viewModel.skuId!!
        )
    }

    override fun getViewModel() = ProcessedViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentProcessedImagesBinding.inflate(inflater, container, false)


}