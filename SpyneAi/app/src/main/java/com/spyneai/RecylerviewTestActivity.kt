package com.spyneai

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.base.OnItemClickListener
import com.spyneai.databinding.ActivityRecylerviewTestBinding

class RecylerviewTestActivity : AppCompatActivity(),OnItemClickListener {

    lateinit var binding : ActivityRecylerviewTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRecylerviewTestBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val list = ArrayList<OngoingTest>()
        list.add(OngoingTest("Sandeep Singh","Sr. Andorid Developer"))
        list.add(OngoingTest("Pawan ","Sr. Software Developer"))



        binding.rvTestOne.apply {
            layoutManager = LinearLayoutManager(
                this@RecylerviewTestActivity,
                LinearLayoutManager.VERTICAL,
                false)

            adapter = OngoingTestAdapter(list,this@RecylerviewTestActivity)
        }

    }

    override fun onItemClick(view: View, position: Int, data: Any?) {
        when(view.id){

            R.id.tvDesignation -> {
                val test = data as OngoingTest
                Toast.makeText(this,test.designation,Toast.LENGTH_LONG).show()
            }
        }
    }
}