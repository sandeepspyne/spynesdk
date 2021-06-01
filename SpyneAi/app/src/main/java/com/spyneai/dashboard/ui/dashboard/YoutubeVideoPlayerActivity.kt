package com.spyneai.dashboard.ui.dashboard

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.spyneai.R
import com.spyneai.model.skumap.UpdateSkuResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_show_images.*
import kotlinx.android.synthetic.main.activity_youtube_video_player.*

class YoutubeVideoPlayerActivity : AppCompatActivity() {

    private var videoPlayer: SimpleExoPlayer? = null
    lateinit var videoUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_youtube_video_player)

        videoUrl = intent.getStringExtra(AppConstants.VIDEO_URL).toString()

        initializePlayer()

        imgBack.setOnClickListener {
            releasePlayer()
            onBackPressed()
        }

        ivDashboardHome.setOnClickListener(View.OnClickListener {
            releasePlayer()
            val intent = Intent(this, MainDashboardActivity::class.java)
            startActivity(intent)
            finish()
        })

    }

    private fun initializePlayer() {
        videoPlayer = SimpleExoPlayer.Builder(this).build()
        youtubeVideoPlayerView?.player = videoPlayer
        buildMediaSource()?.let {
            videoPlayer?.prepare(it)
        }
    }

    private fun buildMediaSource(): MediaSource? {
        val dataSourceFactory = DefaultDataSourceFactory(this, "sample")
        return ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(Uri.parse(videoUrl))
    }

    override fun onResume() {
        super.onResume()
        videoPlayer?.playWhenReady = true
    }

    override fun onStop() {
        super.onStop()
        videoPlayer?.playWhenReady = false
        if (isFinishing) {
        releasePlayer()
        }
    }

    private fun releasePlayer() {
        videoPlayer?.release()
    }

}