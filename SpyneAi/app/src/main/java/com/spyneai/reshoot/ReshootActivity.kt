package com.spyneai.reshoot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.spyneai.R
import com.spyneai.orders.ui.MyOrdersFragment

class ReshootActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reshoot)

        supportFragmentManager.beginTransaction()
            .add(R.id.flContainer, ReshootFragment())
            .commit()
    }
}