package com.spyneai.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.spyneai.R

public class ShowReplacedImagesInteriorAdapter(
        val context: Context,
        val imageList: ArrayList<String>,
        val btnlistener: BtnClickListener,
)
    : RecyclerView.Adapter<ShowReplacedImagesInteriorAdapter.ViewHolder>() {

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
        val ivInterior: ImageView = view.findViewById(R.id.ivInterior)
        val llBeforeAfterReplacedInterior: LinearLayout = view.findViewById(R.id.llBeforeAfterReplacedInterior)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.row_replaced_images_interior, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element

        //Glide.with(context).load(imageList[position]).into(viewHolder.imgReplaced)

        Glide.with(context) // replace with 'this' if it's in activity
                .load(imageList[position])
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                .into(viewHolder.ivInterior)


        mClickListener = btnlistener

        viewHolder.llBeforeAfterReplacedInterior.setOnClickListener(View.OnClickListener {
            Log.e("ok", "Ok way" + position)
            if (mClickListener != null)
                mClickListener?.onBtnClick(position)
        })
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = imageList.size

    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(getAdapterPosition(), getItemViewType())
        }
        return this
    }
}
