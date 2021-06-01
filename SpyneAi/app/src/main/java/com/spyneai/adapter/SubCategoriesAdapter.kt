package com.spyneai.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.model.subcategories.Data
import com.spyneai.needs.AppConstants

class SubCategoriesAdapter(val context: Context,
                           val subCategoriesList: ArrayList<NewSubCatResponse.Data>,
                           var pos : Int,
                           val btnlistener: BtnClickListener?)
    : RecyclerView.Adapter<SubCategoriesAdapter.ViewHolder>() {

    companion object {
        var mClickListener: BtnClickListener? = null
    }
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val llSubCategories: LinearLayout = view.findViewById(R.id.llSubCategories)
        val llSubCategoriesImage: CardView = view.findViewById(R.id.llSubCategoriesImage)
        val ivSubCategories: ImageView = view.findViewById(R.id.ivSubCategories)
        val tvSubcategories: TextView = view.findViewById(R.id.tvSubcategories)
    }

    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(getAdapterPosition(), getItemViewType())
        }
        return this
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.sub_categories, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        Glide.with(context).load(AppConstants.BASE_IMAGE_URL +
                subCategoriesList[position].display_thumbnail)
                .into(viewHolder.ivSubCategories)

        viewHolder.tvSubcategories.setText(subCategoriesList[position].sub_cat_name)
        mClickListener = btnlistener
        if (position == pos)
            viewHolder.llSubCategoriesImage.setBackgroundResource(R.drawable.bg_selected)
        else
            viewHolder.llSubCategoriesImage.setBackgroundResource(R.drawable.bg_channel)

        viewHolder.llSubCategories.setOnClickListener(View.OnClickListener {
            if (mClickListener != null)
                mClickListener?.onBtnClick(position,subCategoriesList[position].sub_cat_name,subCategoriesList[position].display_thumbnail)
            pos = position

            viewHolder.llSubCategoriesImage.setBackgroundResource(R.drawable.bg_selected)
        })

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = subCategoriesList.size

    open interface BtnClickListener {
        fun onBtnClick(position: Int,subcategoryName : String,subcategoryImage : String)
    }

}
