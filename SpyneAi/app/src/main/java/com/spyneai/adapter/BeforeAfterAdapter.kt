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
import com.spyneai.model.beforeafter.Data
import com.spyneai.needs.AppConstants


public class BeforeAfterAdapter(
    val context: Context,
    val beforeAfterList: ArrayList<Data>,
    val btnlistener: BtnClickListener,
)
    : RecyclerView.Adapter<BeforeAfterAdapter.ViewHolder>() {

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
        val llBeforeAfters: LinearLayout = view.findViewById(R.id.llBeforeAfters)
        val imgBefore: ImageView = view.findViewById(R.id.imgBefore)
        val imgAfter: ImageView = view.findViewById(R.id.imgAfter)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_before_after, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element

        Glide.with(context).load(
            AppConstants.BASE_IMAGE_URL +
                    beforeAfterList[position].beforeThumb.toString()
        ).into(viewHolder.imgBefore)

        Glide.with(context).load(
            AppConstants.BASE_IMAGE_URL +
                    beforeAfterList[position].afterThumb.toString()
        ).into(viewHolder.imgAfter)

        mClickListener = btnlistener
        viewHolder.llBeforeAfters.setOnClickListener(View.OnClickListener {
            Log.e("ok", "Ok way" + position)
            if (mClickListener != null)
                mClickListener?.onBtnClick(position)
        })
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = beforeAfterList.size

    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(getAdapterPosition(), getItemViewType())
        }
        return this
    }
}
