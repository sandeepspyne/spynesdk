package com.spyneai.videorecording

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.camera.core.*


import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R

import com.spyneai.databinding.ActivityThreeSixtyViewBinding
import com.spyneai.needs.ScrollingLinearLayoutManager
import com.spyneai.videorecording.adapter.BeforeAfterTestAdapter
import kotlinx.android.synthetic.main.activity_before_after.*
import java.lang.Math.*
import java.util.*


class ThreeSixtyViewActivity : AppCompatActivity() {

    private lateinit var binding : ActivityThreeSixtyViewBinding
    private lateinit var beforeAfterAdapter: BeforeAfterTestAdapter


    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_three_sixty_view)

        binding.flShootNow.setOnClickListener {
            startActivity(Intent(this@ThreeSixtyViewActivity, RecordVideoActivity::class.java))
        }

        setRecycler()
    }



    private fun setRecycler() {
        beforeAfterAdapter = BeforeAfterTestAdapter(this)

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )

        binding.rvBeforeAfter.setLayoutManager(
            ScrollingLinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        )

        binding.rvBeforeAfter.setAdapter(beforeAfterAdapter)

    }





}