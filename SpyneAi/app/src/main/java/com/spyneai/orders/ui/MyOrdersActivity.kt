package com.spyneai.orders.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.paging.ExperimentalPagingApi
import com.spyneai.R
import com.spyneai.base.BaseActivity
import com.spyneai.databinding.ActivityMyOrdersBinding
import com.spyneai.orders.ui.fragment.MyOrdersFragment
import com.spyneai.showConnectionChangeView


class MyOrdersActivity : BaseActivity() {

    lateinit var binding: ActivityMyOrdersBinding

    @OptIn(ExperimentalPagingApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .add(R.id.flContainer, MyOrdersFragment())
            .commit()

    }

    override fun onConnectionChange(isConnected: Boolean) {
        showConnectionChangeView(isConnected, binding.root)
    }
}


