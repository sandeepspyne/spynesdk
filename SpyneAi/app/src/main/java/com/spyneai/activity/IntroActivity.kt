package com.spyneai.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.appintro.*
import com.spyneai.R

class IntroActivity : AppIntro2()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addSlide(
            AppIntroCustomLayoutFragment.newInstance(R.layout.activity_onboard_one)
        )
        addSlide(
            AppIntroCustomLayoutFragment.newInstance(R.layout.activity_onboard_two)
        )
        addSlide(
            AppIntroCustomLayoutFragment.newInstance(R.layout.activity_onboard_three)
        )

    }
}