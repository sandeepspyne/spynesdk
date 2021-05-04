package com.spyneai.adapter

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.activity.ShowImagesActivity
import com.spyneai.model.processImageService.Task
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities

class OngoingProjectAdapter(
    val context: Context,
    val ongoingProjectList: ArrayList<Task>,
    val btnlistener: BtnClickListener
) : RecyclerView.Adapter<OngoingProjectAdapter.ViewHolder>() {


    companion object {
        var mClickListener: BtnClickListener? = null
    }

    open interface BtnClickListener {
        fun onBtnClick(position: Int)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rlSkuGifList: RelativeLayout = view.findViewById(R.id.rlSkuGifList)
        val ivImage: ImageView = view.findViewById(R.id.ivImage)
        val tvCatName: TextView = view.findViewById(R.id.tvCatName)
        val tvTotalImages: TextView = view.findViewById(R.id.tvTotalImages)
        val tvImageProcessing: TextView = view.findViewById(R.id.tvImageProcessing)
        val tvSkuId: TextView = view.findViewById(R.id.tvSkuId)
        val tvDots: TextView = view.findViewById(R.id.tvDots)
        val ivRemoveCard: ImageView = view.findViewById(R.id.ivRemoveCard)
        val tvImageCount: TextView = view.findViewById(R.id.tvImageCount)


    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.rv_ongoing_projects, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvCatName.text = ongoingProjectList[position].catName
        if (ongoingProjectList[position].catName.equals("Automobiles"))
            holder.tvTotalImages.text =
                ongoingProjectList[position].totalExteriorImages.toString() + " Exterior + " + ongoingProjectList[position].totalInteriorImages.toString() + " Interior + " + ongoingProjectList[position].totalFocusedImages.toString() + " Focused"
        else
            holder.tvTotalImages.text =
                ongoingProjectList[position].totalExteriorImages.toString() + " Images"
        holder.tvImageProcessing.text = ongoingProjectList[position].imageProcessing
        holder.tvSkuId.text = ongoingProjectList[position].skuId
        holder.tvDots.blink()
        holder.tvImageCount.text =
            ongoingProjectList[position].totalImageProcessed.toString() + "/" + ongoingProjectList[position].totalImageToProcessed.toString()
        Glide.with(context)
            .load(ongoingProjectList[position].imageFileList[0])
            .into(holder.ivImage)



        if (ongoingProjectList[position].isCompleted) {
//            removeItem(position)

        }

        holder.ivRemoveCard.setOnClickListener {
            ongoingProjectList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, ongoingProjectList.size)

        }
        if (ongoingProjectList[position].imageProcessing.equals("AI Image Processing")){
            holder.tvDots.text = ""
            holder.tvDots.text = "....."

        }

        if (ongoingProjectList[position].imageProcessing.equals("Image Processing Failed :( - Please try again!") || ongoingProjectList[position].imageProcessing.equals(
                "Order is Complete - View Now"
            )
        ) {
            if (ongoingProjectList[position].imageProcessing.equals("Image Processing Failed :( - Please try again!"))
                holder.tvImageProcessing.setTextColor(ContextCompat.getColor(context,R.color.errorcolor))
            if (ongoingProjectList[position].imageProcessing.equals("Order is Complete - View Now")){
                holder.tvImageProcessing.setTextColor(ContextCompat.getColor(context,R.color.green))
                holder.tvImageProcessing.setOnClickListener {
                    Utilities.savePrefrence(context,
                        AppConstants.SKU_ID,
                        ongoingProjectList[position].skuId)
                    viewOrder()
                }
            }
            holder.tvDots.clearAnimation()
            holder.tvDots.visibility = View.GONE
            holder.ivRemoveCard.visibility = View.VISIBLE
        }

        mClickListener = btnlistener
        holder.rlSkuGifList.setOnClickListener(View.OnClickListener {
            if (mClickListener != null)
                mClickListener?.onBtnClick(position)
        })
    }

    private fun viewOrder(){
        val intent = Intent(context,
            ShowImagesActivity::class.java)
        startActivity(context, intent, null)
    }


    fun removeItem(position: Int) {
        ongoingProjectList.removeAt(position)
        notifyDataSetChanged()
    }


    override fun getItemCount() = ongoingProjectList.size

    fun View.blink(
        times: Int = Animation.INFINITE,
        duration: Long = 50L,
        offset: Long = 20L,
        minAlpha: Float = 0.0f,
        maxAlpha: Float = 1.0f,
        repeatMode: Int = Animation.REVERSE
    ) {
        startAnimation(AlphaAnimation(minAlpha, maxAlpha).also {
            it.duration = duration
            it.startOffset = offset
            it.repeatMode = repeatMode
            it.repeatCount = times
        })
    }


}


