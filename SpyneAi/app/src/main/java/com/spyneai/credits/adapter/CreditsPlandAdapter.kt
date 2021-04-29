package com.spyneai.credits.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.OrientationEventListener
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.credits.holder.CreditsPlanHolder
import com.spyneai.credits.model.CreditPlansResItem
import com.spyneai.imagesdowloading.DownloadTask


class CreditsPlandAdapter(var context: Context, var plansList: ArrayList<CreditPlansResItem>, var listener: Listener)
    : RecyclerView.Adapter<CreditsPlanHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreditsPlanHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_credit_plans, parent, false)

        return CreditsPlanHolder(view)
    }

    override fun onBindViewHolder(holder: CreditsPlanHolder, position: Int) {
        var item = plansList.get(position)

        holder.tvCredits.text = item.credits.toString()+" credits"
        holder.tvPrice.text = "₹ "+item.price
        //holder.tvPricePerImage.text = "₹ "+item.pricePerImage+" /image"

        if (item.isSelected){
            holder.rb.isChecked = true
            holder.clMain.setBackgroundColor(ContextCompat.getColor(context,R.color.credit_plan_selected))
        }else{
            holder.rb.isChecked = false
            holder.clMain.setBackgroundColor(ContextCompat.getColor(context,R.color.white))
        }

        holder.clMain.setOnClickListener {
            listener.onSelected(plansList.get(position))
        }



    }

    override fun getItemCount(): Int {
        return if (plansList == null) 0 else plansList.size
    }

    interface Listener {
        fun onSelected(item: CreditPlansResItem)

    }
}