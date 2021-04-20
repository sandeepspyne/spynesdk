package com.spyneai.videorecording

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.spyneai.R
import kotlinx.android.synthetic.main.fragment_one_three_sixty_shoot_demo.*

class FragmentOneThreeSixtyShootDemo : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_one_three_sixty_shoot_demo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        if(requireArguments().getInt("shoot_mode",0) == 1){
            iv.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.shoot_back_side))
        }
    }
}