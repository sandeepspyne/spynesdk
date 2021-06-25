package com.spyneai.orders.ui.adapter

import android.content.Context
import android.content.Intent
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.activity.OnboardingsActivity
import com.spyneai.activity.ShowImagesActivity
import com.spyneai.loginsignup.activity.LoginActivity
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.response.CompletedSKUsResponse
import com.spyneai.orders.ui.KarviShowImagesActivity

class MyCompletedOrdersAdapter(
    val context: Context,
    val completedSkuList: ArrayList<CompletedSKUsResponse.Data>
) : RecyclerView.Adapter<MyCompletedOrdersAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tv_images_count: TextView = view.findViewById(R.id.tv_images_count)
        val tvProjectName: TextView = view.findViewById(R.id.tvProjectName)
        val tvSubCat: TextView = view.findViewById(R.id.tvSubCat)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvCategoryName: TextView = view.findViewById(R.id.tvCategoryName)
        val tvSkuName: TextView = view.findViewById(R.id.tvSkuName)
        val tvSource: TextView = view.findViewById(R.id.tvSource)
        val tvPaid: TextView = view.findViewById(R.id.tvPaid)
        val iv_thumbnail_completed: ImageView = view.findViewById(R.id.iv_thumbnail_completed)
        val ivDownloadSKU: ImageView = view.findViewById(R.id.ivDownloadSKU)
    }

    override fun getItemViewType(position: Int): Int {
        if (position == completedSkuList.size - 1){
            return 1
        }
        return 0
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyCompletedOrdersAdapter.ViewHolder {
        val view : View?

        if (viewType == 0){
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_my_completed_orders, parent, false)
        }else{
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_last_completed_order, parent, false)
        }


        return MyCompletedOrdersAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyCompletedOrdersAdapter.ViewHolder, position: Int) {

        val dots = "..."

        if (completedSkuList[position].paid)
            holder.tvPaid.visibility = View.VISIBLE

        holder.tv_images_count.text = completedSkuList[position].total_images.toString()
        holder.tvSubCat.text = completedSkuList[position].sub_category
        if (completedSkuList[position].sub_category == null)
            holder.tvSubCat.text = "prod_Gcg69Rkxa"

        holder.tvDate.text = completedSkuList[position].created_date
        holder.tvCategoryName.text = completedSkuList[position].category
        holder.tvSkuName.text = completedSkuList[position].sku_name + dots
        holder.tvProjectName.text = completedSkuList[position].project_name
        holder.tvSource.text = completedSkuList[position].source

        Glide.with(context) // replace with 'this' if it's in activity
            .load(completedSkuList[position].thumbnail)
            .error(R.mipmap.defaults) // show error drawable if the image is not a gif
            .into(holder.iv_thumbnail_completed)

        holder.ivDownloadSKU.setOnClickListener {
            Utilities.savePrefrence(
                context,
                AppConstants.SKU_ID,
                completedSkuList[position].sku_id
            )
            var intent : Intent? = null
            intent = if (context.getString(R.string.app_name) == "Karvi.com") Intent(context,
                KarviShowImagesActivity::class.java) else Intent(context, ShowImagesActivity::class.java)


            intent.putExtra("is_paid",completedSkuList[position].paid)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return completedSkuList.size
    }
}