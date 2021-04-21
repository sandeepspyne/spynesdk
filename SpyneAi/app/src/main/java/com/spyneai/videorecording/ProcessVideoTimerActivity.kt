package com.spyneai.videorecording

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import androidx.databinding.DataBindingUtil
import com.spyneai.R
import com.spyneai.databinding.ActivityProcessVideoTimerBinding
import kotlinx.android.synthetic.main.activity_timer.*
import java.util.concurrent.TimeUnit

class ProcessVideoTimerActivity : AppCompatActivity() {

    val progress = 1000
    private lateinit var binding : ActivityProcessVideoTimerBinding
    var i = 0
    lateinit var countDownTimer: CountDownTimer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_process_video_timer)

        countDownTimer(300000)
    }

    private fun countDownTimer(maxProgress: Long) {
        countDownTimer = object : CountDownTimer(maxProgress.toLong(), progress.toLong()) {
            override fun onTick(millisUntilFinished: Long) {
                i++
                circular_progress.setCurrentProgress((i * 100 / (maxProgress / progress)).toDouble())

                tvMin.setText(""+ String.format("%02d", TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
                tvSec.setText(""+ String.format("%02d", TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                        TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(
                                millisUntilFinished
                            )
                        )));
            }

            override fun onFinish() {
                tvMin.setText("00")
                tvSec.setText("00");
            }
        }.start()
    }

    override fun onBackPressed() {
        //back press disabled
    }
}