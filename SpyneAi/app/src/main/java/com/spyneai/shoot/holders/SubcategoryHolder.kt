package com.spyneai.shoot.holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.base.GenericAdapter
import com.spyneai.base.OnItemClickListener
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.databinding.ItemSubcategoriesBinding
import com.spyneai.needs.AppConstants

class SubcategoryHolder(
    itemView: View,
    listener: OnItemClickListener?
) : RecyclerView.ViewHolder(itemView), GenericAdapter.Binder<NewSubCatResponse.Subcategory> {

    var listener: OnItemClickListener? = null
    var binding : ItemSubcategoriesBinding? = null

    init {
        binding = ItemSubcategoriesBinding.bind(itemView)
        this.listener = listener
    }

    override fun bind(subcategory: NewSubCatResponse.Subcategory) {

        binding.apply {
            this?.tvSubcategories?.text = subcategory.sub_cat_name
        }

        Glide.with(itemView)
            .load(AppConstants.BASE_IMAGE_URL + subcategory.display_thumbnail)
            .into(binding?.ivSubCategories!!)

        binding?.llSubCategories?.setOnClickListener {
            listener?.onItemClick(
                it,
                adapterPosition,
                subcategory
            )
        }
    }
}