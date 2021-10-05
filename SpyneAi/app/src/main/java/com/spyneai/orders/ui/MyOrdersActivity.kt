package com.spyneai.orders.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.spyneai.R


class MyOrdersActivity : AppCompatActivity() {

       private var TAG = "MyOrderActivity"

        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_orders)


            supportFragmentManager.beginTransaction()
            .add(R.id.flContainer,MyOrdersFragment())
            .commit()

    }
}


