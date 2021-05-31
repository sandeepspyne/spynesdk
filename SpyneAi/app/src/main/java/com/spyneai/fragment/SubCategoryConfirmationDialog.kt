package com.spyneai.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.spyneai.databinding.DialogSubcategoryConfirmationBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities

class SubCategoryConfirmationDialog : DialogFragment() {

    private var _binding: DialogSubcategoryConfirmationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogSubcategoryConfirmationBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            Glide.with(requireContext()).load(AppConstants.BASE_IMAGE_URL +
                    arguments?.getString("subcat_image"))
                .into(binding.ivSubcat)

            binding.tvMessage.text = "Selected Category "+arguments?.getString("subcat_name")+
                    " Angles "+Utilities.getPreference(requireContext(), AppConstants.FRAME_SHOOOTS).toString()
        }catch (e : Exception){
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}