package com.spyneai.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.activity.CameraPreviewActivity
import com.spyneai.adapter.BackgroundsAdapter
import com.spyneai.model.channel.BackgroundsResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.fragment_background.*
import kotlinx.android.synthetic.main.fragment_background.view.*
import kotlinx.android.synthetic.main.fragment_channel.rvChannel
import kotlinx.android.synthetic.main.fragment_channel.view.*
import kotlinx.android.synthetic.main.fragment_channel.view.rvChannel

class BackgroundFragment(context: Context,categoryId: String,
                         subCategoryId: String,categoryNames : String) : Fragment(){

    lateinit var backgroundList : List<BackgroundsResponse>
    lateinit var backgroundsAdapter: BackgroundsAdapter
    val contexts = context
    val catId = categoryId
    val subCatId = subCategoryId
    val categoryName = categoryNames

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_background, container, false)
        setRecycler(view)
        return view
    }

    private fun setRecycler(view: View) {
        backgroundList = ArrayList<BackgroundsResponse>()
        backgroundsAdapter = BackgroundsAdapter(requireContext(),
            backgroundList as ArrayList<BackgroundsResponse>,
                object : BackgroundsAdapter.BtnClickListener {
                    override fun onBtnClick(position: Int) {
                        Log.e("position preview", position.toString())
                        (activity as CameraPreviewActivity?)!!.showPreview()
                    }
                })
        val layoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(contexts,
                LinearLayoutManager.HORIZONTAL, false)
        view.rvChannel.setLayoutManager(layoutManager)
        view.rvChannel.setAdapter(backgroundsAdapter)

        fetchBackgrounds(view)
    }

    private fun fetchBackgrounds(view: View) {
        (backgroundList as ArrayList).clear()
        (backgroundList as ArrayList).addAll(Utilities.getListBackgrounds(contexts,AppConstants.BACKGROUND_LIST)!!)

        if (backgroundList.size!! > 0)
        {
            view.rvChannel.visibility = View.VISIBLE
            view.tvBackground.visibility = View.GONE
        }
        else{
            view.rvChannel.visibility = View.GONE
            view.tvBackground.visibility = View.VISIBLE
        }
        backgroundsAdapter.notifyDataSetChanged()
    }
}