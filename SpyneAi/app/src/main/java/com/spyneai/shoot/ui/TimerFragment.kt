package com.spyneai.shoot.ui

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.FragmentTimerBinding
import com.spyneai.shoot.data.ProcessViewModel

import java.util.concurrent.TimeUnit

class TimerFragment : BaseFragment<ProcessViewModel, FragmentTimerBinding>()  {

    val progress = 1000

    var i = 0
    lateinit var countDownTimer: CountDownTimer

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setCustomTimer()
    }

    private fun setCustomTimer() {
        when(viewModel.exteriorAngles.value){
            4 ->  CountDownTimer(480000)
            8 ->  CountDownTimer(480000 * 2)
            12 ->   CountDownTimer(480000 * 3)
            24 ->  CountDownTimer(480000 * 4)
            5 ->  CountDownTimer(480000)
            6 ->  CountDownTimer(480000)
            7 ->  CountDownTimer(580000)
        }
    }

    private fun CountDownTimer(maxProgress: Long) {
        countDownTimer = object : CountDownTimer(maxProgress.toLong(), progress.toLong()) {
            override fun onTick(millisUntilFinished: Long) {
                i++
                binding.circularProgress.setCurrentProgress((i * 100 / (maxProgress / progress)).toDouble())

                binding.tvMinSec.setText(
                    "" + String.format(
                        "%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(
                            millisUntilFinished
                        ),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(
                                    TimeUnit.MILLISECONDS.toMinutes(
                                        millisUntilFinished
                                    )
                                )
                    )
                );
            }

            override fun onFinish() {
                binding.tvMinSec.setText("00:00")
            }
        }.start()
    }


    override fun getViewModel() = ProcessViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentTimerBinding.inflate(inflater, container, false)
}