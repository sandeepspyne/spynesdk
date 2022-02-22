package com.spyneai.orders.data.paging

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R

class LoaderStateAdapter(private val retry: () -> Unit) :
    LoadStateAdapter<LoaderStateAdapter.LoaderViewHolder>() {

    override fun onBindViewHolder(holder: LoaderViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoaderViewHolder {
        return LoaderViewHolder.getInstance(parent, retry)
    }

    /**
     * view holder class for footer loader and error state handling
     */
    class LoaderViewHolder(view: View, retry: () -> Unit) : RecyclerView.ViewHolder(view) {

        companion object {
            //get instance of the DoggoImageViewHolder
            fun getInstance(parent: ViewGroup, retry: () -> Unit): LoaderViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val view = inflater.inflate(R.layout.item_loader, parent, false)
                return LoaderViewHolder(view, retry)
            }
        }

        private val progressBar: ProgressBar = view.findViewById(R.id.progress_bar)
        private val errorMsg: TextView = view.findViewById(R.id.error_msg)
        private val retryButton: Button = view.findViewById(R.id.retry_button)

        init {
            view.findViewById<Button>(R.id.retry_button).setOnClickListener {
                retry()
            }
        }

        fun bind(loadState: LoadState) {
            progressBar.isVisible = loadState is LoadState.Loading
            retryButton.isVisible = loadState is LoadState.Error
            errorMsg.isVisible =
                !(loadState as? LoadState.Error)?.error?.message.isNullOrBlank()
            errorMsg.text = (loadState as? LoadState.Error)?.error?.message
        }
    }
}