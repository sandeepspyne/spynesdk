package com.spyneai.credits.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.adapter.AddChannelAdapter
import com.spyneai.credits.holder.CreditsPlanHolder
import com.spyneai.credits.model.CreditPlansResItem

class CreditsPlandAdapter(var context : Context, var plansList : ArrayList<CreditPlansResItem>)
    : RecyclerView.Adapter<CreditsPlanHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreditsPlanHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_credit_plans, parent, false)

        return CreditsPlanHolder(view)
    }

    override fun onBindViewHolder(holder: CreditsPlanHolder, position: Int) {

    }

    override fun getItemCount(): Int {
        return if (plansList == null) 0 else plansList.size
    }
}