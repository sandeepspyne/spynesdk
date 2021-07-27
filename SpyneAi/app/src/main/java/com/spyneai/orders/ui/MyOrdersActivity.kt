package com.spyneai.orders.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.spyneai.R
import com.spyneai.orders.data.repository.MyOrdersRepository
import com.spyneai.orders.data.viewmodel.MyOrdersViewModel

private val viewModel = MyOrdersViewModel()

class MyOrdersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_orders)

        supportFragmentManager.beginTransaction()
            .add(R.id.flContainer,MyOrdersFragment())
            .commit()
    }
}