package com.spyneai.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.Nullable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target





public class ShowReplacedImagesAdapter(
    val context: Context,
    val rawImageList: ArrayList<String>,
    val processedImageList: ArrayList<String>,
    val btnlistener: BtnClickListener,
)
    : RecyclerView.Adapter<ShowReplacedImagesAdapter.ViewHolder>() {

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
        val imgBeforeReplaced: ImageView = view.findViewById(com.spyneai.R.id.imgBeforeReplaced)
        val imgAfterReplaced: ImageView = view.findViewById(com.spyneai.R.id.imgAfterReplaced)
        val llBeforeAfterReplaced: LinearLayout = view.findViewById(com.spyneai.R.id.llBeforeAfterReplaced)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(com.spyneai.R.layout.row_replaced_images, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element

        //Glide.with(context).load(imageList[position]).into(viewHolder.imgReplaced)

//        Glide.with(context) // replace with 'this' if it's in activity
//                .load(rawImageList[position])
//                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
//                .error(R.mipmap.defaults) // show error drawable if the image is not a gif
//                .into(viewHolder.imgBeforeReplaced)


        Glide.with(context)
            .load(rawImageList[position])
            .apply(
                RequestOptions()
                    .error(com.spyneai.R.mipmap.defaults)
                    .centerCrop()
            )
            .listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    @Nullable e: GlideException?,
                    model: Any,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    //on load failed
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any,
                    target: Target<Drawable?>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    //on load success
                    return false
                }
            })
            .transition(withCrossFade())
            .into(viewHolder.imgBeforeReplaced)

//        Glide.with(context) // replace with 'this' if it's in activity
//                .load(processedImageList[position])
//                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
//                .error(com.spyneai.R.mipmap.defaults) // show error drawable if the image is not a gif
//                .into(viewHolder.imgAfterReplaced)

        Glide.with(context)
            .load(processedImageList[position])
            .apply(
                RequestOptions()
                    .error(com.spyneai.R.mipmap.defaults)
                    .centerCrop()
            )
            .listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    @Nullable e: GlideException?,
                    model: Any,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    //on load failed
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any,
                    target: Target<Drawable?>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    //on load success
                    return false
                }
            })
            .transition(withCrossFade())
            .into(viewHolder.imgAfterReplaced)

        mClickListener = btnlistener

        viewHolder.llBeforeAfterReplaced.setOnClickListener(View.OnClickListener {
            Log.e("ok", "Ok way" + position)
            if (mClickListener != null)
                mClickListener?.onBtnClick(position)
        })
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = rawImageList.size

    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(getAdapterPosition(), getItemViewType())
        }
        return this
    }
}
