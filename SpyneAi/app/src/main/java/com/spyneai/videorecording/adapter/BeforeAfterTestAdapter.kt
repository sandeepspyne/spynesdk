package com.spyneai.videorecording.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R

class BeforeAfterTestAdapter(
    val context: Context
)
    : RecyclerView.Adapter<BeforeAfterTestAdapter.ViewHolder>() {



    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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

        Glide.with(context).load(ContextCompat.getDrawable(context,R.drawable.before_after_dummy)).into(viewHolder.imgBefore)

        Glide.with(context).load(ContextCompat.getDrawable(context,R.drawable.before_after_dummy)).into(viewHolder.imgAfter)

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = 3

    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(getAdapterPosition(), getItemViewType())
        }
        return this
    }
}
