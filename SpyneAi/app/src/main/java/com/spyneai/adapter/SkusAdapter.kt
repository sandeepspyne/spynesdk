package com.spyneai.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.activity.CameraActivity
import com.spyneai.activity.CameraPreviewActivity
import com.spyneai.activity.EditSkuActivity
import com.spyneai.activity.ShootSelectionActivity
import com.spyneai.model.channel.ChannelsResponse
import com.spyneai.model.channel.Data
import com.spyneai.model.order.Photo
import com.spyneai.model.order.Sku
import com.spyneai.model.sku.Photos
import com.spyneai.needs.AppConstants
import kotlinx.android.synthetic.main.activity_edit_sku.*

class SkusAdapter(val context: Context,
                   val skuList: List<Sku>,
                   val btnlistener: BtnClickListener?)
    : RecyclerView.Adapter<SkusAdapter.ViewHolder>() {

    private lateinit var photsAdapter: PhotosListAdapter
    private lateinit var photoList: List<Photo>

    companion object {
         var mClickListener: BtnClickListener? = null
     }
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val llSkuList: LinearLayout = view.findViewById(R.id.llSkuList)
        val tvSkuName: TextView = view.findViewById(R.id.tvSkuName)
        val tvSkuCount: TextView = view.findViewById(R.id.tvSkuCount)
        val ivEditSku: ImageView = view.findViewById(R.id.ivEditSku)
        val ivDeleteSku: ImageView = view.findViewById(R.id.ivDeleteSku)
        val rvSkusList : RecyclerView = view.findViewById(R.id.rvSkusList)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.row_sku_list, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element

        viewHolder.tvSkuName.setText(skuList[position].displayName)
        viewHolder.tvSkuCount.setText(
                skuList[position].photosCount.toString() + "/" +
                skuList[position].photos.size )

        mClickListener = btnlistener
/*
        viewHolder.llSkuList.setOnClickListener(View.OnClickListener {
            if (mClickListener != null)
                mClickListener?.onBtnClick(position)
        })
*/
        viewHolder.ivDeleteSku.setOnClickListener(View.OnClickListener {
            if (mClickListener != null)
                mClickListener?.onBtnClick(position)
        })

        photoList = ArrayList<Photo>()
        photsAdapter = PhotosListAdapter(context, photoList,
                object : PhotosListAdapter.BtnClickListener {
                    override fun onBtnClick(position: Int) {
                        Log.e("position preview", position.toString())
                        val intent = Intent(context, ShootSelectionActivity::class.java)
                        intent.putExtra(AppConstants.POSITION,position)
                        intent.putExtra(AppConstants.SKU_ID,skuList[position].skuId)
                        context.startActivity(intent)
                    }
                })

        val layoutManager: RecyclerView.LayoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        viewHolder.rvSkusList.setLayoutManager(layoutManager)
        viewHolder.rvSkusList.setAdapter(photsAdapter)

        (photoList as ArrayList).clear()
        (photoList as ArrayList).addAll(skuList[position].photos as ArrayList)

        photsAdapter.notifyDataSetChanged()

    }

     // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = skuList.size
     open interface BtnClickListener {
         fun onBtnClick(position: Int)
     }
}
