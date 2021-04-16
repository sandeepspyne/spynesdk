package com.spyneai.adapter

import FrameImages
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.needs.AppConstants

public class FocusedFramesAdapter(val context: Context,
                                  val subCategoriesList: ArrayList<FrameImages>,
                                  var pos : Int,
                                  val btnlistener: BtnClickListener?)
    : RecyclerView.Adapter<FocusedFramesAdapter.ViewHolder>() {

    companion object {
        var mClickListener: BtnClickListener? = null
    }
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardIFrameImages: CardView = view.findViewById(R.id.cardIFrameImages)
        val ivIFrameImages: ImageView = view.findViewById(R.id.ivIFrameImages)
        val tvIFrameImages: TextView = view.findViewById(R.id.tvIFrameImages)
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
                .inflate(R.layout.sub_frames_focused, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        Glide.with(context).load(AppConstants.BASE_IMAGE_URL +
                subCategoriesList[position].displayImage.toString())
                .into(viewHolder.ivIFrameImages)

        viewHolder.tvIFrameImages.setText(
            subCategoriesList[position].displayImage.split("/")
                    [subCategoriesList[position].displayImage.split("/").size-1]
                .replace("%20"," ")
                .replace("%26","&")
                .replace(".png",""))

        mClickListener = btnlistener
        if (position == pos)
            viewHolder.cardIFrameImages.setBackgroundResource(R.drawable.bg_selected)
        else
            viewHolder.cardIFrameImages.setBackgroundResource(R.drawable.bg_channel)

/*
        viewHolder.llIFrameImages.setOnClickListener(View.OnClickListener {
            if (mClickListener != null)
                mClickListener?.onBtnClick(position)
            pos = position

            viewHolder.llIFrameImages.setBackgroundResource(R.drawable.bg_selected)
        })
*/

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = subCategoriesList.size

    open interface BtnClickListener {
        fun onBtnClick(position: Int)
    }

}
