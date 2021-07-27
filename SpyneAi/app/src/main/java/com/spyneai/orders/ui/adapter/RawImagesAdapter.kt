package com.spyneai.orders.ui.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.orders.ui.activity.ShowRawImagesActivity
import kotlinx.android.synthetic.main.item_raw_images.view.*

class RawImagesAdapter(
    val activity: ShowRawImagesActivity,
    val imageList: ArrayList<GetProjectsResponse.Images>
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
        val view: View = View.inflate(activity, R.layout.item_raw_images,null)
        val ivRaw = view.findViewById<TextView>(R.id.ivRaw) as ImageView

        try {
            Glide.with(activity) // replace with 'this' if it's in activity
                .load(imageList[position].input_lres)
                .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                .into(ivRaw)

        }catch (e: Exception){

        }



        return view
    }
}