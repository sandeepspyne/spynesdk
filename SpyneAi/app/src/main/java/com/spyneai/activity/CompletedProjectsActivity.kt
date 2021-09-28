package com.spyneai.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.spyneai.databinding.ActivityCompletedProjectsBinding
import com.spyneai.orders.ui.fragment.CompletedProjectsFragment

class CompletedProjectsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompletedProjectsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompletedProjectsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager
            .beginTransaction()
            .add(binding.flContainer.id, CompletedProjectsFragment())
            .commit()

        binding.imgBackCompleted.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })
    }
}