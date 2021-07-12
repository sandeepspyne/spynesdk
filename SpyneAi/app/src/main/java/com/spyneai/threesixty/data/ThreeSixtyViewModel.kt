package com.spyneai.threesixty.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ThreeSixtyViewModel : ViewModel() {

    val isDemoClicked: MutableLiveData<Boolean> = MutableLiveData()
}