package com.spyneai.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.Image
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.model.categories.Data
import com.spyneai.needs.AppConstants


public class CategoriesDashboardAdapter(
        val context: Context,
        val categoriesResponseList: ArrayList<Data>,
        val btnlistener: BtnClickListener
)
    : RecyclerView.Adapter<CategoriesDashboardAdapter.ViewHolder>() {

    companion object {
        var mClickListener: BtnClickListener? = null
    }

    open interface BtnClickListener {
        fun onBtnClick(position: Int)
    }
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val llCategories: LinearLayout = view.findViewById(R.id.llCategories)
        val tvCategoryName: TextView = view.findViewById(R.id.tvCategoryName)
        val imgCategory: ImageView = view.findViewById(R.id.imgCategory)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.row_categories_dashboard, viewGroup, false)

/*
        return ViewHolder(view).listen { pos, type ->
            val item = items.get(pos)
            //TODO do other stuff here
        }
*/
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        /*  if (categoriesResponseList[position].displayName.equals("Footwear")
                  || categoriesResponseList[position].displayName.equals("Automobiles") ) {*/
        viewHolder.llCategories.visibility = View.VISIBLE
        viewHolder.tvCategoryName.text = categoriesResponseList[position].displayName

        Log.e("Color ", categoriesResponseList[position].colorCode)

        Glide.with(context).load(
                AppConstants.BASE_IMAGE_URL +
                        categoriesResponseList[position].displayThumbnail.toString()
        ).into(viewHolder.imgCategory)

        viewHolder.imgCategory.setBackgroundColor(Color.parseColor(categoriesResponseList[position].colorCode))

        if (position > 0)
            viewHolder.llCategories.alpha = 0.5F
        mClickListener = btnlistener
        viewHolder.llCategories.setOnClickListener(View.OnClickListener {
            Log.e("ok", "Ok way" + position)
            if (mClickListener != null)
                mClickListener?.onBtnClick(position)
        })
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = categoriesResponseList.size

    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(getAdapterPosition(), getItemViewType())
        }
        return this
    }
}
