package com.spyneai.videorecording

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import com.spyneai.R
import com.spyneai.dashboard.ui.MainDashboardActivity
import com.spyneai.databinding.ActivityProcessVideoTimerBinding
import com.spyneai.databinding.ActivityWalletBinding
import com.spyneai.gotoHome
import com.spyneai.videorecording.model.ProcessVideoEvent
import kotlinx.android.synthetic.main.activity_timer.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.concurrent.TimeUnit

class ProcessVideoTimerActivity : AppCompatActivity() {

    val progress = 1000
    private lateinit var binding : ActivityProcessVideoTimerBinding
    var i = 0
    lateinit var countDownTimer: CountDownTimer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProcessVideoTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvBackToHome.setOnClickListener {
           gotoHome()
        }

        countDownTimer(300000)
    }

    private fun countDownTimer(maxProgress: Long) {
        countDownTimer = object : CountDownTimer(maxProgress.toLong(), progress.toLong()) {
            override fun onTick(millisUntilFinished: Long) {
                i++
                circular_progress.setCurrentProgress((i * 100 / (maxProgress / progress)).toDouble())

                binding.tvMin.setText(""+ String.format("%02d", TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
                binding.tvSec.setText(""+ String.format("%02d", TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                        TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(
                                millisUntilFinished
                            )
                        )))
            }

            override fun onFinish() {
                binding.tvMin.setText("00")
                binding.tvSec.setText("00");
            }
        }.start()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ProcessVideoEvent?) {
        event?.getSkuId()?.let {
            var resultIntent = Intent(baseContext, ThreeSixtyInteriorViewActivity::class.java)
            resultIntent.putExtra("back_press_type",1)
            resultIntent.setAction(it)
            startActivity(resultIntent)
            finish()
        }
    }

    override fun onBackPressed() {
        //back press disabled
    }
}