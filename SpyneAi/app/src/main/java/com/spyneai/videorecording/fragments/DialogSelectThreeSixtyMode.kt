package com.spyneai.videorecording.fragments

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.spyneai.R


class DialogSelectThreeSixtyMode : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        getDialog()?.setCancelable(false);
        return inflater.inflate(R.layout.dialog_select_three_sixty_mode, container, false)
    }

    override fun onStart() {
        super.onStart()

        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT

        dialog?.getWindow()?.setLayout(width, height)
        dialog?.getWindow()?.setLayout(width, height)
    }




}