package com.spyneai.videorecording

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.spyneai.R
import com.spyneai.databinding.DialogShootThreeSixtyDemoBinding


class DialogThreeSixtyDemo : DialogFragment() {

    private lateinit var demoCollectionAdapter: ThreeSixtyShootDemoAdapter
    lateinit var binding : DialogShootThreeSixtyDemoBinding
    private lateinit var fragmentList: ArrayList<Fragment>
    val shootMode  = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        getDialog()?.setCancelable(false);
        binding = DataBindingUtil.inflate(inflater,R.layout.dialog_shoot_three_sixty_demo, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvNext.setOnClickListener { dismiss() }
        setupDemo()
    }

    fun setupDemo() {
        fragmentList = ArrayList<Fragment>()

        var args = Bundle()
        args.putInt("shoot_mode",shootMode)

        if (shootMode == 1){
            binding.tvHint.text = "Shoot the back side of the car"
        }

        var fragmentOne =  FragmentOneThreeSixtyShootDemo()
        fragmentOne.arguments = args
       fragmentList.add(fragmentOne)

        var fragmentTwo =  FragmentTwoThreeSixtyShootDemo()
        fragmentTwo.arguments = args

        fragmentList.add(fragmentTwo)

        demoCollectionAdapter = ThreeSixtyShootDemoAdapter(requireActivity(), fragmentList)

        binding.viewPager.adapter = demoCollectionAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab!!.position == 0) {
                    if (shootMode == 1){
                        binding.tvHint.text = "Shoot the back side of the car"
                    }else{
                        binding.tvHint.text = "Shoot the front side of the car"
                    }

                    binding.tvNext.text = "Next"
                } else {
                    binding.tvHint.text =
                        "Sit on the middle of the back seat, Place the phone in the centre & start moving your wrist"
                    binding.tvNext.text = "Begin Shoot"
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }
}