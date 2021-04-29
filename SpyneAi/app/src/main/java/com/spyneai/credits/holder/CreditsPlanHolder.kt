package com.spyneai.credits.holder

import android.view.View
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R

class CreditsPlanHolder(view: View) : RecyclerView.ViewHolder(view)  {
    val rb : RadioButton = view.findViewById(R.id.rb)
    val tvCredits : TextView = view.findViewById(R.id.tv_credits)
    val tvPrice : TextView = view.findViewById(R.id.tv_price)
    val tvPricePerImage : TextView = view.findViewById(R.id.tv_price_per_image)
}