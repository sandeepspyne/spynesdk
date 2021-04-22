package com.spyneai.videorecording.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.spyneai.R
import com.spyneai.databinding.DialogCopyEmbeddedCodeBinding

class DialogEmbedCode : DialogFragment() {

    private lateinit var binding : DialogCopyEmbeddedCodeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.dialog_copy_embedded_code,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvCode.text = arguments?.getString("code")

//         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//             binding.tvCode.text = Html.fromHtml(arguments?.getString("code"), Html.FROM_HTML_MODE_COMPACT)
//        } else {
//             binding.tvCode.text = Html.fromHtml(arguments?.getString("code"))
//        }

        binding.tvCopy.setOnClickListener {
            val myClipboard: ClipboardManager = activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

            val clip: ClipData = ClipData.newPlainText("link", binding.tvCode.text)
            myClipboard.setPrimaryClip(clip)

            Toast.makeText(requireContext(),"Embedded code copied successfully", Toast.LENGTH_LONG).show()

            dismiss()
        }
    }
}