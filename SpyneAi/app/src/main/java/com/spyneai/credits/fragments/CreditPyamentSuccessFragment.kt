package com.spyneai.credits.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.databinding.FragmentCreditSuccessBinding

class CreditPyamentSuccessFragment : Fragment() {

    private lateinit var binding : FragmentCreditSuccessBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_credit_success,container,false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //load gif
        Glide.with(this).asGif().load(R.raw.payment_success_gif)
            .into(binding.ivWalletGif)

        binding.tvAmount.text = arguments?.getInt("amount").toString()+" credits has been added to "+"\n"+"your wallet"
    }
}