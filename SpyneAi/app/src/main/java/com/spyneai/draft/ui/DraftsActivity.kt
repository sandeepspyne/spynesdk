package com.spyneai.draft.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.spyneai.databinding.ActivityDraftsBinding

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