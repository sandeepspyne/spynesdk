package com.spyneai.orders.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.spyneai.R
import com.spyneai.orders.ui.fragment.MyOrdersFragment


class MyOrdersActivity : AppCompatActivity() {

       private var TAG = "MyOrderActivity"

        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_orders)


            supportFragmentManager.beginTransaction()
            .add(R.id.flContainer, MyOrdersFragment())
            .commit()

    }
}


