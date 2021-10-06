package com.spyneai.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.spyneai.databinding.ActivityOngoingOrdersBinding
import com.spyneai.orders.ui.fragment.OngoingProjectsFragment


class OngoingOrdersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOngoingOrdersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOngoingOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager
            .beginTransaction()
            .add(binding.flContainer.id, OngoingProjectsFragment())
            .commit()

        binding.imgBackCompleted.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })
    }


}