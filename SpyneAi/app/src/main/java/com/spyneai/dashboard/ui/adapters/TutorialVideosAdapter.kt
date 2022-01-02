package com.spyneai.dashboard.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R


class TutorialVideosAdapter(
    val context: Context,
    val tutorialVideosList: IntArray,
    val btnlistener: BtnClickListener
) : RecyclerView.Adapter<TutorialVideosAdapter.ViewHolder>() {

    companion object {
        var mClickListener: BtnClickListener? = null
    }

    open interface BtnClickListener {
        fun onBtnClick(position: Int)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivTutorialImage: ImageView = view.findViewById(R.id.ivTutorialImage)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.rv_tutorial_videos, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        Glide.with(context)
            .load(tutorialVideosList[position])
            .into(holder.ivTutorialImage)

       mClickListener = btnlistener
        holder.ivTutorialImage.setOnClickListener {
            if (TutorialVideosAdapter.mClickListener != null)
                TutorialVideosAdapter.mClickListener?.onBtnClick(position)
        }

    }
    override fun getItemCount() = tutorialVideosList.size

}


