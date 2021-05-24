package com.spyneai.dashboard.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R


class TutorialVideosAdapter(
    val context: Context,
    val tutorialVideosList: IntArray,
) : RecyclerView.Adapter<TutorialVideosAdapter.ViewHolder>() {

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

        holder.ivTutorialImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://www.youtube.com/channel/UCXFGiawbLL2pBWI84VY5sJw")
            startActivity(context, intent, null)
        }

    }
    override fun getItemCount() = tutorialVideosList.size

}

