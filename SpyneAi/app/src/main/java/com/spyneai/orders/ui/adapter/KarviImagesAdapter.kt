package com.spyneai.orders.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.spyneai.R
import com.spyneai.orders.data.ProcessedImage


class KarviImagesAdapter(
    val context: Context,
    val imageListAfter: ArrayList<ProcessedImage>,
    val btnlistener: BtnClickListener,
)
    : RecyclerView.Adapter<KarviImagesAdapter.ViewHolder>() {

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
        val imgAfterReplaced: ImageView = view.findViewById(R.id.ivProcessed)
        val llBeforeAfterReplaced: ConstraintLayout = view.findViewById(R.id.llBeforeAfterReplaced)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_processed_image, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {

         val shimmer = Shimmer.AlphaHighlightBuilder()// The attributes for a ShimmerDrawable is set by this builder
            .setDuration(1800) // how long the shimmering animation takes to do one full sweep
            .setBaseAlpha(0.7f) //the alpha of the underlying children
            .setHighlightAlpha(0.6f) // the shimmer alpha amount
            .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
            .setAutoStart(true)
            .build()

// This is the placeholder for the imageView
        val shimmerDrawable = ShimmerDrawable().apply {
            setShimmer(shimmer)
        }


        Glide.with(context) // replace with 'this' if it's in activity
            .load(imageListAfter[position].imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .placeholder(shimmerDrawable)
            .into(viewHolder.imgAfterReplaced)


        mClickListener = btnlistener

        viewHolder.llBeforeAfterReplaced.setOnClickListener(View.OnClickListener {
            Log.e("ok", "Ok way" + position)
            if (mClickListener != null)
                mClickListener?.onBtnClick(position)
        })
    }

    private fun loadFailed(position: Int) {
        notifyItemChanged(position)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = imageListAfter.size

    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(getAdapterPosition(), getItemViewType())
        }
        return this
    }
}
