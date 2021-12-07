package com.spyneai.draft.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentDraftSkuDetailsBinding
import com.spyneai.draft.data.DraftViewModel
import com.spyneai.draft.ui.adapter.DraftImagesAdapter
import com.spyneai.draft.ui.adapter.LocalDraftImagesAdapter
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.response.ImagesOfSkuRes
import com.spyneai.posthog.Events
import com.spyneai.setLocale
import com.spyneai.shoot.data.DraftClickedImages
import com.spyneai.shoot.data.model.Image
import com.spyneai.shoot.ui.base.ProcessActivity
import com.spyneai.shoot.ui.base.ShootActivity
import com.spyneai.shoot.ui.base.ShootPortraitActivity
import com.spyneai.shoot.ui.dialogs.NoMagnaotoMeterDialog

class DraftSkuDetailsFragment : BaseFragment<DraftViewModel, FragmentDraftSkuDetailsBinding>() {

    private var exterior = ArrayList<ImagesOfSkuRes.Data>()
    private var interiorList = ArrayList<ImagesOfSkuRes.Data>()
    private var miscList = ArrayList<ImagesOfSkuRes.Data>()
    private var threeSixtyInteriorList = ArrayList<ImagesOfSkuRes.Data>()

    private var localExterior = ArrayList<Image>()
    private var localInteriorList = ArrayList<Image>()
    private var localMiscList = ArrayList<Image>()
    private var localThreeSixtyInteriorList = ArrayList<Image>()
    private lateinit var intent: Intent

    val TAG = "DraftSkuDetailsFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireContext().setLocale()
        refreshText()

        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        intent = requireActivity().intent

        binding.tvProjectName.text = intent.getStringExtra(AppConstants.PROJECT_NAME)

        if (intent.getBooleanExtra(AppConstants.FROM_LOCAL_DB, false)) {
            binding.shimmerCompletedSKU.stopShimmer()
            binding.shimmerCompletedSKU.visibility = View.GONE
            binding.nsv.visibility = View.VISIBLE

            val list = viewModel.getImagesbySkuId(intent.getStringExtra(AppConstants.SKU_ID)!!)
            binding.tvTotalSku.text = list.size.toString()


            if (!list.isNullOrEmpty()) {
                if (intent.getStringExtra(AppConstants.CATEGORY_ID) == AppConstants.CARS_CATEGORY_ID
                    || intent.getStringExtra(AppConstants.CATEGORY_ID) == AppConstants.BIKES_CATEGORY_ID) {
                    localExterior = list?.filter {
                        it.categoryName == "Exterior"
                    } as ArrayList
                } else {
                    localExterior = list
                }

                if (localExterior.size > 0) {
                    if (intent.getStringExtra(AppConstants.CATEGORY_ID) == AppConstants.CATEGORY_ID
                        || intent.getStringExtra(AppConstants.CATEGORY_ID) == AppConstants.BIKES_CATEGORY_ID)
                        binding.tvExterior.visibility = View.VISIBLE

                    binding.rvExteriorImage.visibility = View.VISIBLE

                    binding.rvExteriorImage.apply {
                          adapter = LocalDraftImagesAdapter(requireContext(),
                              localExterior,
                              intent.getStringExtra(AppConstants.CATEGORY_NAME)!!)
                    }
                }

                localInteriorList = list?.filter {
                    it.categoryName == "Interior"
                } as ArrayList

                if (localInteriorList.size > 0) {
                    binding.tvInterior.visibility = View.VISIBLE
                    binding.rvInteriors.visibility = View.VISIBLE
                    binding.rvInteriors.apply {
                         adapter = LocalDraftImagesAdapter(requireContext(),
                             localInteriorList,
                             intent.getStringExtra(AppConstants.CATEGORY_NAME)!!
                         )
                    }
                }

                localMiscList = list?.filter {
                    it.categoryName == "Focus Shoot"
                } as ArrayList

                if (localMiscList.size > 0) {
                    binding.tvFocused.visibility = View.VISIBLE
                    binding.rvFocused.visibility = View.VISIBLE
                    binding.rvFocused.apply {
                        adapter = LocalDraftImagesAdapter(requireContext(),
                            localMiscList,
                            intent.getStringExtra(AppConstants.CATEGORY_NAME)!!)
                    }
                }

                if (getString(R.string.app_name) == AppConstants.OLA_CABS) {
                    localThreeSixtyInteriorList = list?.filter {
                        it.categoryName == "360int"
                    } as ArrayList
                }
            }

            binding.flContinueShoot.visibility = View.VISIBLE
        } else {
            getSkuDetails()

            observeSkuDeatils()
        }

        binding.btnContinueShoot.setOnClickListener {
            onResumeClick()
        }
    }

    private fun onResumeClick() {
        var shootIntent: Intent? = null

        when (intent.getStringExtra(AppConstants.CATEGORY_ID)) {
            AppConstants.CARS_CATEGORY_ID, AppConstants.BIKES_CATEGORY_ID -> {
                shootIntent = Intent(
                    context,
                    ShootActivity::class.java
                )
            }
            "cat_Ujt0kuFxX",
            "cat_Ujt0kuFxY",
            "cat_Ujt0kuFxF",
            "cat_Ujt0kuFxA",
            "cat_P4t6BRVCxx",
            "cat_P4t6BRVCAP",
            "cat_P4t6BRVAyy",
            "cat_P4t6BRVART",
            "cat_P4t6BRVAMN"-> {
                shootIntent = Intent(
                    context,
                    ShootPortraitActivity::class.java
                )
            }

            else -> {
            }
        }

        if (intent.getBooleanExtra(AppConstants.FROM_VIDEO, false)) {
            shootIntent?.apply {
                putExtra(AppConstants.FROM_VIDEO, true)
                putExtra(AppConstants.TOTAL_FRAME, intent.getIntExtra(AppConstants.TOTAL_FRAME, 0))
            }
        }

        shootIntent?.apply {
            putExtra(AppConstants.FROM_DRAFTS, true)
            putExtra(AppConstants.CATEGORY_NAME, intent.getStringExtra(AppConstants.CATEGORY_NAME))
            putExtra(AppConstants.CATEGORY_ID, intent.getStringExtra(AppConstants.CATEGORY_ID))
            putExtra(AppConstants.SUB_CAT_NAME, intent.getStringExtra(AppConstants.SUB_CAT_NAME))
            putExtra(AppConstants.SUB_CAT_ID, intent.getStringExtra(AppConstants.SUB_CAT_ID))
            putExtra(AppConstants.PROJECT_ID, intent.getStringExtra(AppConstants.PROJECT_ID))
            putExtra(AppConstants.SKU_NAME, intent.getStringExtra(AppConstants.SKU_NAME))
            putExtra(AppConstants.SKU_COUNT, intent.getIntExtra(AppConstants.SKU_COUNT, 0))
            putExtra(AppConstants.SKU_CREATED, true)
            putExtra(AppConstants.SKU_ID, intent.getStringExtra(AppConstants.SKU_ID))
            putExtra(
                AppConstants.EXTERIOR_ANGLES,
                intent.getIntExtra(AppConstants.EXTERIOR_ANGLES, 0)
            )
            putExtra(AppConstants.RESUME_EXTERIOR, resumeExterior())
            putExtra(AppConstants.RESUME_INTERIOR, resumeInterior())
            putExtra(AppConstants.RESUME_MISC, resumeMisc())
            putExtra("is_paid", false)
            putExtra(AppConstants.IMAGE_TYPE, intent.getStringExtra(AppConstants.IMAGE_TYPE))
            putExtra(AppConstants.IS_360, intent.getIntExtra(AppConstants.IS_360, 0))
        }

        if (requireActivity().intent.getBooleanExtra(AppConstants.FROM_LOCAL_DB, false)) {
            shootIntent?.apply {
                putExtra(AppConstants.FROM_LOCAL_DB, true)
                putExtra(AppConstants.EXTERIOR_SIZE, localExterior.size)
                putExtra(AppConstants.INTERIOR_SIZE, localInteriorList.size)
                putExtra(AppConstants.MISC_SIZE, localMiscList.size)
            }
        } else {

            val extPathList = exterior?.map {
                it.input_image_hres_url
            } as ArrayList<String>

            val imageNameList = exterior?.map {
                it.image_name
            } as ArrayList<String>

            shootIntent?.apply {
                putExtra(AppConstants.FROM_LOCAL_DB, false)
                putExtra(AppConstants.EXTERIOR_SIZE, exterior.size)
                putStringArrayListExtra(AppConstants.EXTERIOR_LIST, extPathList)
                putStringArrayListExtra(AppConstants.SHOOT_IMAGE_NAME_LIST, imageNameList)
                putExtra(AppConstants.INTERIOR_SIZE, interiorList.size)
                putExtra(AppConstants.MISC_SIZE, miscList.size)
            }
        }

        setDraftImage()

        Utilities.savePrefrence(
            requireContext(),
            AppConstants.CATEGORY_NAME,
            intent.getStringExtra(AppConstants.CATEGORY_NAME)
        )
        Utilities.savePrefrence(
            requireContext(),
            AppConstants.CATEGORY_ID,
            intent.getStringExtra(AppConstants.CATEGORY_ID)
        )


        if (getString(R.string.app_name) == AppConstants.OLA_CABS) {
            if (threeSixtyIntSelected()) {
                Log.d(TAG, "onViewCreated: " + "Three Sixty Selected")
                startProcessActivty(
                    shootIntent!!,
                    localInteriorList.size
                        .plus(localMiscList.size)
                        .plus(localThreeSixtyInteriorList.size)
                )
            } else {
                if (resumeMisc()) {
                    checkMiscSize(shootIntent!!)
                    observeMisc(shootIntent)
                } else {
                    startActivity(shootIntent)
                }
            }
        } else {
            if (resumeMisc()) {
                checkMiscSize(shootIntent!!)
                observeMisc(shootIntent)
            } else {
                startActivity(shootIntent)
            }
        }
    }

    private fun setDraftImage() {
        val fromLocalDb = requireActivity().intent.getBooleanExtra(AppConstants.FROM_LOCAL_DB, false)
        val map = HashMap<String,String>()

        when{
            resumeExterior() -> {
                if (fromLocalDb){
                    localExterior.forEach {
                        map.put(it.overlayId.toString(), it.imagePath!!)
                    }
                }else{
                    exterior.forEach {
                        map.put(it.overlayId.toString(), it.input_image_lres_url)
                    }
                }
            }

            resumeInterior() -> {
                if (fromLocalDb){
                    localInteriorList.forEach {
                        map.put(it.overlayId.toString(), it.imagePath!!)
                    }
                }else{
                    interiorList.forEach {
                        map.put(it.overlayId.toString(), it.input_image_lres_url)
                    }
                }
            }

            resumeMisc() -> {
                if (fromLocalDb){
                    localMiscList.forEach {
                        map.put(it.overlayId.toString(), it.imagePath!!)
                    }
                }else{
                    miscList.forEach {
                        map.put(it.overlayId.toString(), it.input_image_lres_url)
                    }
                }
            }
        }

        DraftClickedImages.clickedImagesMap = map
        val s = ""
    }

    private fun checkMiscSize(intent: Intent) {
        Utilities.showProgressDialog(requireContext())
        viewModel.getSubCategories(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            intent.getStringExtra(AppConstants.CATEGORY_ID).toString()
        )
    }

    private fun observeMisc(intent: Intent) {
        viewModel.subCategoriesResponse.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()

                    if (requireActivity().intent.getBooleanExtra(
                            AppConstants.FROM_LOCAL_DB,
                            false
                        )
                    ) {
                        if (requireActivity().intent.getStringExtra(AppConstants.CATEGORY_NAME) == "Bikes") {
                            val filteredList: List<NewSubCatResponse.Miscellaneous> =
                                it.value.miscellaneous.filter {
                                    it.prod_sub_cat_id == requireActivity().intent.getStringExtra(
                                        AppConstants.SUB_CAT_ID
                                    )
                                }

                            it.value.miscellaneous = filteredList
                        }

                        if (it.value.miscellaneous.size == localMiscList.size) {
                            //start procss activity
                            startProcessActivty(
                                intent,
                                localInteriorList.size.plus(localMiscList.size)
                            )
                        } else {
                            startActivity(intent)
                        }
                    } else {
                        if (it.value.miscellaneous.size == miscList.size) {
                            startProcessActivty(intent, interiorList.size.plus(miscList.size))
                        } else {
                            startActivity(intent)
                        }
                    }

                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) { checkMiscSize(intent) }
                }
            }
        })
    }

    private fun startProcessActivty(intent: Intent, count: Int) {
        Log.d(TAG, "onViewCreated: " + intent.getIntExtra(AppConstants.EXTERIOR_ANGLES, 0))
        val processIntent = Intent(requireContext(), ProcessActivity::class.java)

        processIntent.apply {
            putExtra(AppConstants.CATEGORY_NAME, intent.getStringExtra(AppConstants.CATEGORY_NAME))
            putExtra(AppConstants.CATEGORY_ID, intent.getStringExtra(AppConstants.CATEGORY_ID))
            putExtra("sku_id", intent.getStringExtra(AppConstants.SKU_ID))
            putExtra("project_id", intent.getStringExtra(AppConstants.PROJECT_ID))
            putExtra("exterior_angles", intent.getIntExtra(AppConstants.EXTERIOR_ANGLES, 0))
            this.putStringArrayListExtra("exterior_images_list", getExteriorImagesList())
        }

        when (intent.getStringExtra(AppConstants.CATEGORY_NAME)) {
            "Automobiles" -> {
                processIntent.putExtra("process_sku", true)
                processIntent.putExtra("interior_misc_count", count)
            }
            else -> {
                processIntent.putExtra("process_sku", false)
                processIntent.putExtra("interior_misc_count", count)
            }
        }

        startActivity(processIntent)
    }

    private fun getExteriorImagesList(): java.util.ArrayList<String> {
        if (requireActivity().intent.getBooleanExtra(AppConstants.FROM_LOCAL_DB, false)) {
            val s = localExterior?.map {
                it.imagePath
            }

            return s as ArrayList<String>
        } else {
            val s = exterior?.map {
                it.image_category
            }

            return s as ArrayList<String>
        }

    }

    private fun resumeMisc(): Boolean {
        return if (requireActivity().intent.getBooleanExtra(AppConstants.FROM_LOCAL_DB, false)) {
            localMiscList.size > 0
        } else {
            miscList.size > 0
        }
    }

    private fun threeSixtyIntSelected(): Boolean {
        return if (requireActivity().intent.getBooleanExtra(AppConstants.FROM_LOCAL_DB, false)) {
            localThreeSixtyInteriorList.size > 0
        } else {
            threeSixtyInteriorList.size > 0
        }
    }


    private fun resumeInterior() = !resumeExterior() && !resumeMisc()

    private fun resumeExterior(): Boolean {
        return if (requireActivity().intent.getBooleanExtra(AppConstants.FROM_LOCAL_DB, false)) {
            localExterior.size != requireActivity().intent.getIntExtra(
                AppConstants.EXTERIOR_ANGLES,
                0) && (localInteriorList.isEmpty() && localMiscList.isEmpty())

        } else {
            exterior.size != requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_ANGLES, 0)
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

    fun refreshText(){
        binding.tvExterior.text = getString(R.string.exterior)
        binding.tvInterior.text = getString(R.string.interior)
        binding.tvFocused.text = getString(R.string.focused)
        binding.btnContinueShoot.text = getString(R.string.resume_shoot)
    }

    private fun observeSkuDeatils() {
        viewModel.imagesOfSkuRes.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    binding.shimmerCompletedSKU.stopShimmer()
                    binding.shimmerCompletedSKU.visibility = View.GONE
                    binding.nsv.visibility = View.VISIBLE

                    binding.flContinueShoot.visibility = View.VISIBLE

                    var imageList = ArrayList<String>()

                    if (!it.value.data.isNullOrEmpty()) {
                        val list = it.value.data
                        binding.tvTotalSku.text = list.size.toString()

                        exterior =
                            when (requireActivity().intent.getStringExtra(AppConstants.CATEGORY_NAME)) {
                                "E-Commerce", "Footwear", "Food & Beverages", "Photo Box" -> {
                                    list as ArrayList
                                }
                                else -> {
                                    list?.filter {
                                        it.image_category == "Exterior"
                                    } as ArrayList

                                }
                            }

                        if (exterior.size > 0) {
                            if (requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID) == AppConstants.CARS_CATEGORY_ID
                                || requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID) == AppConstants.BIKES_CATEGORY_ID)
                                binding.tvExterior.visibility = View.VISIBLE

                            binding.rvExteriorImage.visibility = View.VISIBLE
                            binding.rvExteriorImage.apply {
                                adapter = DraftImagesAdapter(requireContext(), exterior,
                                        intent.getStringExtra(AppConstants.CATEGORY_NAME)!!)
                            }
                        }

                        interiorList = list?.filter {
                            it.image_category == "Interior"
                        } as ArrayList

                        if (interiorList.size > 0) {
                            binding.rvInteriors.visibility = View.VISIBLE
                            binding.tvInterior.visibility = View.VISIBLE
                            binding.rvInteriors.apply {
                                adapter = DraftImagesAdapter(requireContext(), interiorList,
                                    intent.getStringExtra(AppConstants.CATEGORY_NAME)!!)
                            }
                        }

                        miscList = list?.filter {
                            it.image_category == "Focus Shoot"
                        } as ArrayList

                        if (miscList.size > 0) {
                            binding.rvFocused.visibility = View.VISIBLE
                            binding.tvFocused.visibility = View.VISIBLE
                            binding.rvFocused.apply {
                                adapter = DraftImagesAdapter(requireContext(), miscList,
                                       intent.getStringExtra(AppConstants.CATEGORY_NAME)!!)
                            }
                        }

                        if (getString(R.string.app_name) == AppConstants.OLA_CABS) {
                            threeSixtyInteriorList = list?.filter {
                                it.image_category == "360int"
                            } as ArrayList
                        }
                    } else {
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
                        Events.GET_COMPLETED_ORDERS_FAILED, HashMap<String,Any?>(),
                        it.errorMessage!!
                    )
                    if (it.errorCode == 404) {
                        binding.flContinueShoot.visibility = View.VISIBLE
                    } else {
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