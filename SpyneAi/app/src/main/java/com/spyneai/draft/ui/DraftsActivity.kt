package com.spyneai.draft.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.spyneai.R
import com.spyneai.databinding.ActivityDraftsBinding
import com.spyneai.databinding.ActivityOngoingOrdersBinding
import com.spyneai.orders.ui.fragment.OngoingProjectsFragment

class DraftsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDraftsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDraftsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager
            .beginTransaction()
            .add(binding.flContainer.id, DraftProjectsFragment())
            .commit()

        binding.imgBackCompleted.setOnClickListener{
            onBackPressed()
        }
    }
}