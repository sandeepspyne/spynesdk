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
import com.spyneai.dashboard.response.Data
import com.spyneai.dashboard.response.NewCategoriesResponse
import com.spyneai.needs.AppConstants


public class CategoriesAdapter(
    val context: Context,
    val categoriesResponseList: ArrayList<NewCategoriesResponse.Data>,
    val btnlistener: BtnClickListener,
    val before: String,
    val after: String
)
    : RecyclerView.Adapter<CategoriesAdapter.ViewHolder>() {

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
        val llCategory: LinearLayout = view.findViewById(R.id.llCategory)
        val tvCategoryName: TextView = view.findViewById(R.id.tvCategoryName)
        val tvCategoryNameSub: TextView = view.findViewById(R.id.tvCategoryNameSub)
        val imgCategory: ImageView = view.findViewById(R.id.imgCategory)
        val llBeforeAfter: LinearLayout = view.findViewById(R.id.llBeforeAfter)
        val imgBefore: ImageView = view.findViewById(R.id.imgBefores)
        val imgAfter: ImageView = view.findViewById(R.id.imgAfters)
        val tvShootNow: TextView = view.findViewById(R.id.tvShootNow)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.row_categories, viewGroup, false)


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
        viewHolder.tvCategoryName.text = categoriesResponseList[position].prod_cat_name
        viewHolder.tvCategoryNameSub.text = categoriesResponseList[position].description


        Glide.with(context).load(
                AppConstants.BASE_IMAGE_URL +
                        categoriesResponseList[position].display_thumbnail.toString()
        ).into(viewHolder.imgCategory)

        val colors = intArrayOf(Color.parseColor(categoriesResponseList[position].color_code),
                Color.parseColor("#FFFFFF"))

        if (position > 4)
            viewHolder.llCategories.alpha = 0.5F

        val gd = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors)
        gd.cornerRadius = 0f
        viewHolder.llCategory.background = gd


        /*Glide.with(context).load(AppConstants.BASE_IMAGE_URL + before).into(viewHolder.imgBefore)

        Glide.with(context).load(AppConstants.BASE_IMAGE_URL + after).into(viewHolder.imgAfter)
*/
        Glide.with(context).load("https://spyne-cliq.s3.ap-south-1.amazonaws.com/spyne-cliq/category/sub/before/foot.JPG").into(viewHolder.imgBefore)

        Glide.with(context).load("https://spyne-cliq.s3.ap-south-1.amazonaws.com/spyne-cliq/category/sub/after/footafter.jpg").into(viewHolder.imgAfter)

        mClickListener = btnlistener
        viewHolder.llCategories.setOnClickListener(View.OnClickListener {
            Log.e("ok", "Ok way" + position)
            if (mClickListener != null)
                mClickListener?.onBtnClick(position)
/*
            if (position == 0) {
                if (viewHolder.llBeforeAfter.visibility == View.GONE)
                    viewHolder.llBeforeAfter.visibility = View.VISIBLE
                else
                    viewHolder.llBeforeAfter.visibility = View.GONE
            }
*/


        })
        viewHolder.tvShootNow.setOnClickListener(View.OnClickListener {
            if (mClickListener != null)
                mClickListener?.onBtnClick(position)
        })


        /*  }
          else{
              viewHolder.llCategories.visibility = View.GONE
          }*/

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
