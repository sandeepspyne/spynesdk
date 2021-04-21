package com.spyneai.videorecording.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.spyneai.R

class DialogRecordVideoHint : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        getDialog()?.setCancelable(false);
        return inflater.inflate(R.layout.dialog_record_video_top_hint, container, false)
    }

}