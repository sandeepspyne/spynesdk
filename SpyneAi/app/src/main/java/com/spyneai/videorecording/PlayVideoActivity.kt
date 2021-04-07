package com.spyneai.videorecording


import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.MediaController
import android.widget.Toast
import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener
import com.spyneai.R
import kotlinx.android.synthetic.main.activity_play_video.*

class PlayVideoActivity : AppCompatActivity() {

    var TAG : String = "Play video"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_video)
    }

    override fun onStart() {
        super.onStart()
        intializePlayer();
    }

    private fun intializePlayer(){
        var mediaController : MediaController? = MediaController(this);
        mediaController?.setAnchorView(videoview);

        videoview.setMediaController(mediaController);
        videoview.setVideoURI(intent.data);
        videoview.start();
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            videoview.pause();
        }
    }

    override fun onStop() {
        super.onStop()
        releasePlayer();
    }

    private fun releasePlayer(){
        videoview.stopPlayback();
    }
}