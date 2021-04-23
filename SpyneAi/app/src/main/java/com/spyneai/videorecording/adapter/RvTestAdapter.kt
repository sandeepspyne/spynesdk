package com.spyneai.videorecording.adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.spyneai.R

class RvTestAdapter(var context: Context) : RecyclerView.Adapter<RvTestAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iv: ImageView = view.findViewById(R.id.iv)
        val preview: PlayerView = view.findViewById(R.id.player_view_lib)
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0){
            return 0
        }else{
            return 1
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
       if (viewType == 0){
           val view = LayoutInflater.from(viewGroup.context)
               .inflate(R.layout.fragment_one_three_sixty_shoot_demo, viewGroup, false)
           return ViewHolder(view)
       }else{
           val view = LayoutInflater.from(viewGroup.context)
               .inflate(R.layout.fragment_two_three_sixty_shoot_demo, viewGroup, false)
           return ViewHolder(view)
       }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position != 0){

            var playerView = holder.preview
            var videoPlayer = SimpleExoPlayer.Builder(context).build()
            playerView?.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT)
            playerView?.setPlayer(videoPlayer)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.CONTENT_TYPE_MOVIE)
                    .build()
                videoPlayer!!.setAudioAttributes(audioAttributes, true)
            }

            var uri = RawResourceDataSource.buildRawResourceUri(R.raw.how_to_shoot_interior_back)


            val mediaItem = MediaItem.fromUri(uri!!)
            //videoPlayer!!.volume = 0F
            videoPlayer!!.setMediaItem(mediaItem)
            // videoPlayer!!.setPlayWhenReady(playWhenReady)

            videoPlayer!!.addListener(object : Player.EventListener {
                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {

                }

                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_ENDED -> {
                            videoPlayer!!.seekTo(0)
                        }

                    }
                }
            })

            videoPlayer!!.seekTo(0)
            videoPlayer!!.prepare();

        }
    }

    override fun getItemCount(): Int {
        return 3
    }
}