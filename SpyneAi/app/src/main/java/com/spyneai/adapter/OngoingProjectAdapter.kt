package com.spyneai.adapter

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.model.processImageService.Task

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
        holder.tvImageCount.text = ongoingProjectList[position].totalImageToProcessed.toString()+"/"+ongoingProjectList[position].totalImageProcessed.toString()
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

        if (ongoingProjectList[position].imageProcessing.equals("image processing failed :( please try again with new shoot!") || ongoingProjectList[position].imageProcessing.equals(
                "your order is now completed :) email sent!") || ongoingProjectList[position].imageProcessing.equals(
                "your order is now completed :) sending email...")){
                    holder.tvDots.clearAnimation()
            holder.tvDots.visibility = View.GONE
            holder.tvImageCount.visibility = View.GONE
            holder.ivRemoveCard.visibility = View.VISIBLE
        }






            mClickListener = btnlistener
        holder.rlSkuGifList.setOnClickListener(View.OnClickListener {
            if (mClickListener != null)
                mClickListener?.onBtnClick(position)
        })
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


