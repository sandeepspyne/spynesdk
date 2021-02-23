package com.spyneai.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.activity.CameraActivity
import com.spyneai.activity.CameraPreviewActivity
import com.spyneai.activity.EditSkuActivity
import com.spyneai.model.channel.ChannelsResponse
import com.spyneai.model.channel.Data
import com.spyneai.model.order.Sku
import com.spyneai.needs.AppConstants

class SkuAdapter(val context: Context,
                 val skuList: List<Sku>,
                 val btnlistener: BtnClickListener?)
    : RecyclerView.Adapter<SkuAdapter.ViewHolder>() {

    companion object {
        var mClickListener: BtnClickListener? = null
    }
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val llSku: LinearLayout = view.findViewById(R.id.llSku)
        val imgSku: ImageView = view.findViewById(R.id.imgSku)
        val tvSkuName: TextView = view.findViewById(R.id.tvSkuName)
        val tvSkuCount: TextView = view.findViewById(R.id.tvSkuCount)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_sku, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        if (skuList[position].photos.size > 0)
            Glide.with(context).load(/*AppConstants.BASE_IMAGE_URL+*/skuList[position].photos[0].displayThumbnail).into(viewHolder.imgSku)
        viewHolder.tvSkuName.setText(skuList[position].displayName)
        viewHolder.tvSkuCount.setText(
            skuList[position].photosCount.toString() + "/" +
                    skuList[position].photos.size )

        mClickListener = btnlistener
        viewHolder.llSku.setOnClickListener(View.OnClickListener {
            if (mClickListener != null)
                mClickListener?.onBtnClick(position)
            val intent = Intent(context, EditSkuActivity::class.java)
            intent.putExtra(AppConstants.SKU_ID,skuList[position].skuId)
            context.startActivity(intent)
        })

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = skuList.size
    open interface BtnClickListener {
        fun onBtnClick(position: Int)
    }
}
