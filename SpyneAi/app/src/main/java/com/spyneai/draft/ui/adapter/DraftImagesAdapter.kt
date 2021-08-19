package com.spyneai.draft.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.base.OnItemClickListener
import com.spyneai.orders.data.response.ImagesOfSkuRes
import com.spyneai.processedimages.ui.adapter.ProcessedImagesAdapter

class DraftImagesAdapter(
    val context: Context,
    val imageList: ArrayList<ImagesOfSkuRes.Data>
) : BaseAdapter() {
    override fun getCount(): Int {
        return imageList.size
    }

    override fun getItem(position: Int): Any? {
        return imageList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = View.inflate(context, R.layout.item_raw_images,null)
        val ivRaw = view.findViewById<TextView>(R.id.ivRaw) as ImageView

        try {
            Glide.with(context) // replace with 'this' if it's in activity
                .load(imageList[position].input_image_hres_url)
                .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                .into(ivRaw)

        }catch (e: Exception){

        }



        return view
    }
}