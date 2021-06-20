package com.spyneai.credits.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.credits.CreditPlansActivity
import com.spyneai.dashboard.ui.MainDashboardActivity
import com.spyneai.databinding.FragmentCreditPaymentFailedBinding

class CreditPaymentFailedFragment : Fragment() {

    private var _binding: FragmentCreditPaymentFailedBinding? = null
    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreditPaymentFailedBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //load gif
        Glide.with(this).asGif().load(R.raw.payment_failed)
            .into(binding.ivWalletGif)

        binding.tvGoToHome.setOnClickListener {
            var dashboardIntent = Intent(requireContext(), MainDashboardActivity::class.java)
            dashboardIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(dashboardIntent)
        }

        binding.tvRetry.setOnClickListener {
            if (requireActivity() != null){
                var activty = requireActivity() as CreditPlansActivity
                activty.prepareCheckOut()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}