package com.spyneai.draft.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.posthog.android.Properties
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentDraftProjectsBinding
import com.spyneai.databinding.FragmentDraftSkuDetailsBinding
import com.spyneai.draft.data.DraftViewModel
import com.spyneai.draft.ui.adapter.DraftImagesAdapter
import com.spyneai.draft.ui.adapter.LocalDraftImagesAdapter
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.response.ImagesOfSkuRes
import com.spyneai.posthog.Events
import com.spyneai.processedimages.ui.adapter.ProcessedImagesAdapter
import com.spyneai.shoot.data.model.Image
import com.spyneai.shoot.ui.base.ProcessActivity
import com.spyneai.shoot.ui.base.ShootActivity
import com.spyneai.shoot.ui.base.ShootPortraitActivity
import kotlinx.android.synthetic.main.activity_credit_plans.*

class DraftSkuDetailsFragment : BaseFragment<DraftViewModel, FragmentDraftSkuDetailsBinding>() {

    private var exterior = ArrayList<ImagesOfSkuRes.Data>()
    private var interiorList = ArrayList<ImagesOfSkuRes.Data>()
    private var miscList = ArrayList<ImagesOfSkuRes.Data>()
    private var threeSixtyInteriorList = ArrayList<ImagesOfSkuRes.Data>()

    private var localExterior = ArrayList<Image>()
    private var localInteriorList = ArrayList<Image>()
    private var localMiscList = ArrayList<Image>()
    private var localThreeSixtyInteriorList = ArrayList<Image>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
        val intent = requireActivity().intent

        binding.tvProjectName.text =intent.getStringExtra(AppConstants.PROJECT_NAME)

            if (intent.getBooleanExtra(AppConstants.FROM_LOCAL_DB,false)){
            binding.shimmerCompletedSKU.stopShimmer()
            binding.shimmerCompletedSKU.visibility = View.GONE
            binding.nsv.visibility = View.VISIBLE

            val list = viewModel.getImagesbySkuId(intent.getStringExtra(AppConstants.SKU_ID)!!)

                binding.tvTotalSku.text = list.size.toString()

            if (!list.isNullOrEmpty()) {

                if (intent.getStringExtra(AppConstants.CATEGORY_NAME) == "Automobiles"){
                    localExterior = list?.filter {
                        it.categoryName == "Exterior"
                    } as ArrayList
                }else {
                    localExterior = list
                }

                if (localExterior.size > 0) {
                    if (intent.getStringExtra(AppConstants.CATEGORY_NAME) == "Automobiles")
                        binding.tvExterior.visibility = View.VISIBLE

                    binding.rvExteriorImage.visibility = View.VISIBLE

                    binding.rvExteriorImage.apply {
                          adapter = LocalDraftImagesAdapter(requireContext(),localExterior)
                    }
                }

                localInteriorList = list?.filter {
                    it.categoryName == "Interior"
                } as ArrayList

                if (localInteriorList.size > 0) {
                    binding.tvInterior.visibility = View.VISIBLE
                    binding.rvInteriors.visibility = View.VISIBLE
                    binding.rvInteriors.apply {
                         adapter = LocalDraftImagesAdapter(requireContext(),localInteriorList)
                    }
                }

                localMiscList = list?.filter {
                    it.categoryName == "Focus Shoot"
                } as ArrayList

                if (localMiscList.size > 0) {
                    binding.tvFocused.visibility = View.VISIBLE
                    binding.rvFocused.visibility = View.VISIBLE
                    binding.rvFocused.apply {
                        adapter = LocalDraftImagesAdapter(requireContext(),localMiscList)
                    }
                }

//                if (getString(R.string.app_name) == AppConstants.OLA_CABS) {
//                    localThreeSixtyInteriorList = list?.filter {
//                        it.categoryName == "360int"
//                    } as ArrayList
//                }
            }

                binding.flContinueShoot.visibility = View.VISIBLE
        }else {
            getSkuDetails()

            observeSkuDeatils()
        }


        binding.btnContinueShoot.setOnClickListener{
            var shootIntent : Intent? = null

            when(intent.getStringExtra(AppConstants.CATEGORY_NAME)){
                "Automobiles","Bikes" -> {
                    shootIntent = Intent(
                        context,
                        ShootActivity::class.java)
                }

                "Footwear","E-Commerce" -> {
                    shootIntent = Intent(
                        context,
                        ShootPortraitActivity::class.java)
                }

                else -> {
                }
            }

            shootIntent?.apply {
                putExtra(AppConstants.FROM_DRAFTS, true)
                putExtra(AppConstants.CATEGORY_NAME, intent.getStringExtra(AppConstants.CATEGORY_NAME))
                putExtra(AppConstants.CATEGORY_ID, intent.getStringExtra(AppConstants.CATEGORY_ID))
                putExtra(AppConstants.SUB_CAT_NAME,intent.getStringExtra(AppConstants.SUB_CAT_NAME))
                putExtra(AppConstants.SUB_CAT_ID, intent.getStringExtra(AppConstants.SUB_CAT_ID))
                putExtra(AppConstants.PROJECT_ID, intent.getStringExtra(AppConstants.PROJECT_ID))
                putExtra(AppConstants.SKU_NAME, intent.getStringExtra(AppConstants.SKU_NAME))
                putExtra(AppConstants.SKU_COUNT, intent.getIntExtra(AppConstants.SKU_COUNT,0))
                putExtra(AppConstants.SKU_CREATED, true)
                putExtra(AppConstants.SKU_ID, intent.getStringExtra(AppConstants.SKU_ID))
                putExtra(AppConstants.EXTERIOR_ANGLES, intent.getIntExtra(AppConstants.EXTERIOR_ANGLES,0))
                putExtra(AppConstants.RESUME_EXTERIOR, resumeExterior())
                putExtra(AppConstants.RESUME_INTERIOR, resumeInterior())
                putExtra(AppConstants.RESUME_MISC, resumeMisc())
                putExtra("is_paid",false)
                putExtra(AppConstants.IMAGE_TYPE,intent.getStringExtra(AppConstants.IMAGE_TYPE))
                putExtra(AppConstants.IS_360,intent.getStringExtra(AppConstants.IS_360))
            }

            if (requireActivity().intent.getBooleanExtra(AppConstants.FROM_LOCAL_DB,false)){
                shootIntent?.apply {
                    putExtra(AppConstants.FROM_LOCAL_DB, true)
                    putExtra(AppConstants.EXTERIOR_SIZE, localExterior.size)
                    putExtra(AppConstants.INTERIOR_SIZE, localInteriorList.size)
                    putExtra(AppConstants.MISC_SIZE, localMiscList.size)
                }
            }else {

                val s = exterior?.map {
                    it.input_image_hres_url
                } as ArrayList<String>


               shootIntent?.apply {
                   putExtra(AppConstants.FROM_LOCAL_DB, false)
                   putExtra(AppConstants.EXTERIOR_SIZE, exterior.size)
                   putStringArrayListExtra(AppConstants.EXTERIOR_LIST, s)
                   putExtra(AppConstants.INTERIOR_SIZE, interiorList.size)
                   putExtra(AppConstants.MISC_SIZE, miscList.size)
               }
            }

            Utilities.savePrefrence(requireContext(),AppConstants.CATEGORY_NAME,intent.getStringExtra(AppConstants.CATEGORY_NAME))
            Utilities.savePrefrence(requireContext(),AppConstants.CATEGORY_ID,intent.getStringExtra(AppConstants.CATEGORY_ID))


            if (resumeMisc()){
                checkMiscSize(shootIntent!!)
                observeMisc(shootIntent)
            }else {
                startActivity(shootIntent)
            }
        }
    }

    private fun checkMiscSize(intent: Intent) {
        Utilities.showProgressDialog(requireContext())
        viewModel.getSubCategories(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            intent.getStringExtra(AppConstants.CATEGORY_ID).toString()
        )
    }

    private fun observeMisc(intent: Intent) {
        viewModel.subCategoriesResponse.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()

                    if (requireActivity().intent.getBooleanExtra(AppConstants.FROM_LOCAL_DB,false)){
                        if (requireActivity().intent.getStringExtra(AppConstants.CATEGORY_NAME) == "Bikes") {
                            val filteredList: List<NewSubCatResponse.Miscellaneous> = it.value.miscellaneous.filter {
                                it.prod_sub_cat_id ==  requireActivity().intent.getStringExtra(AppConstants.SUB_CAT_ID)
                            }

                            it.value.miscellaneous = filteredList
                        }

                        if (it.value.miscellaneous.size == localMiscList.size) {
                            //start procss activity
                            startProcessActivty(intent,localInteriorList.size.plus(localMiscList.size))
                        }else {
                            startActivity(intent)
                        }
                    }else {
                        if (it.value.miscellaneous.size == miscList.size) {
                            startProcessActivty(intent,interiorList.size.plus(miscList.size))
                        }else {
                            startActivity(intent)
                        }
                    }

                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) {checkMiscSize(intent)}
                }
            }
        })
    }

    private fun startProcessActivty(intent: Intent,count : Int) {
        val processIntent = Intent(requireContext(), ProcessActivity::class.java)

        processIntent.apply {
            putExtra(AppConstants.CATEGORY_NAME, intent.getStringExtra(AppConstants.CATEGORY_NAME))
            putExtra("sku_id", intent.getStringExtra(AppConstants.SKU_ID))
            putExtra("project_id", intent.getStringExtra(AppConstants.PROJECT_ID))
            putExtra("exterior_angles", intent.getStringExtra(AppConstants.EXTERIOR_ANGLES))
            this.putStringArrayListExtra("exterior_images_list",getExteriorImagesList())
        }

        when(intent.getStringExtra(AppConstants.CATEGORY_NAME)) {
            "Automobiles" -> {
                processIntent.putExtra("process_sku", true)
                processIntent.putExtra("interior_misc_count",count)
            }
            else -> {
                processIntent.putExtra("process_sku", false)
                processIntent.putExtra("interior_misc_count",count)
            }
        }

        startActivity(processIntent)
    }

    private fun getExteriorImagesList(): java.util.ArrayList<String> {

        if (requireActivity().intent.getBooleanExtra(AppConstants.FROM_LOCAL_DB,false)) {
            val s = localExterior?.map {
                it.imagePath
            }

            return s as ArrayList<String>
        }else {
            val s = exterior?.map {
                it.image_category
            }

            return s as ArrayList<String>
        }

    }

    private fun resumeMisc() : Boolean {
        return if (requireActivity().intent.getBooleanExtra(AppConstants.FROM_LOCAL_DB,false)){
            localMiscList.size > 0
        }else {
            miscList.size > 0
        }
    }

    private fun resumeInterior() = !resumeExterior() && !resumeMisc()

    private fun resumeExterior() : Boolean {
        return if (requireActivity().intent.getBooleanExtra(AppConstants.FROM_LOCAL_DB,false)){
            localExterior.size != requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_ANGLES,0)
                    && (localInteriorList.isEmpty() && localMiscList.isEmpty())

        }else {
            exterior.size != requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_ANGLES,0)
                    && (interiorList.isEmpty() && miscList.isEmpty())
        }
    }
    private fun getSkuDetails() {
        binding.shimmerCompletedSKU.visibility = View.VISIBLE
        binding.shimmerCompletedSKU.startShimmer()

        viewModel.getImagesOfSku(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            requireActivity().intent.getStringExtra(AppConstants.SKU_ID)!!
        )
    }

    private  fun observeSkuDeatils() {
        viewModel.imagesOfSkuRes.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Success -> {
                    binding.shimmerCompletedSKU.stopShimmer()
                    binding.shimmerCompletedSKU.visibility = View.GONE
                    binding.nsv.visibility = View.VISIBLE

                    binding.flContinueShoot.visibility = View.VISIBLE


                    var imageList = ArrayList<String>()

                    if (!it.value.data.isNullOrEmpty()) {

                        val list = it.value.data
                        binding.tvTotalSku.text = list.size.toString()

                        exterior = when(requireActivity().intent.getStringExtra(AppConstants.CATEGORY_NAME)) {
                            "E-Commerce","Footwear" -> {
                                list as ArrayList
                            } else -> {
                                list?.filter {
                                    it.image_category == "Exterior"
                                } as ArrayList

                            }
                        }

                        if (exterior.size > 0) {
                            if (requireActivity().intent.getStringExtra(AppConstants.CATEGORY_NAME) == "Automobiles")
                                binding.tvExterior.visibility = View.VISIBLE

                            binding.rvExteriorImage.visibility = View.VISIBLE
                            binding.rvExteriorImage.apply {
                                    adapter = DraftImagesAdapter(requireContext(),exterior)
                            }
                        }

                        interiorList = list?.filter {
                            it.image_category == "Interior"
                        } as ArrayList

                        if (interiorList.size > 0) {
                            binding.rvInteriors.visibility = View.VISIBLE
                            binding.tvInterior.visibility = View.VISIBLE
                            binding.rvInteriors.apply {
                                adapter = DraftImagesAdapter(requireContext(),interiorList)
                            }
                        }

                        miscList = list?.filter {
                            it.image_category == "Focus Shoot"
                        } as ArrayList

                        if (miscList.size > 0) {
                            binding.rvFocused.visibility = View.VISIBLE
                            binding.tvFocused.visibility = View.VISIBLE
                            binding.rvFocused.apply {
                                   adapter = DraftImagesAdapter(requireContext(),miscList)
                            }
                        }

//                        if (getString(R.string.app_name) == AppConstants.OLA_CABS){
//                            threeSixtyInteriorList = list?.filter {
//                                it.image_category == "360int"
//                            } as ArrayList
//                        }
                    }else {
                        binding.tvTotalSku.text = "0"
                    }

                    it.value.data.forEach {
                        imageList.add(it.input_image_hres_url)
                    }
                }

                is Resource.Failure -> {
                    binding.shimmerCompletedSKU.stopShimmer()
                    binding.shimmerCompletedSKU.visibility = View.GONE

                    requireContext().captureFailureEvent(
                        Events.GET_COMPLETED_ORDERS_FAILED, Properties(),
                        it.errorMessage!!
                    )
                    if (it.errorCode == 404){
                        binding.flContinueShoot.visibility = View.VISIBLE
                    }else{
                        handleApiError(it) { getSkuDetails() }
                    }

                }
            }
        })
    }

    override fun getViewModel() = DraftViewModel::class.java
    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentDraftSkuDetailsBinding.inflate(inflater, container, false)

}