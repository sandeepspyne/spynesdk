package com.spyneai.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.activity.CameraPreviewActivity
import com.spyneai.adapter.ChannelAdapter
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.model.channel.ChannelsResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.fragment_channel.*
import kotlinx.android.synthetic.main.fragment_channel.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChannelFragment(context: Context,categoryId: String,
                      subCategoryId: String,categoryNames : String) : Fragment() {
    lateinit var channelList : List<ChannelsResponse>
    lateinit var channelAdapter: ChannelAdapter
    val contexts = context
    val catId = categoryId
    val subCatId = subCategoryId
    val categoryName = categoryNames

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_channel, container, false)
        setRecycler(view)
        return view
    }

    private fun setRecycler(view: View) {
        channelList = ArrayList<ChannelsResponse>()
        channelAdapter = ChannelAdapter(context!!, channelList as ArrayList<ChannelsResponse>,
                object : ChannelAdapter.BtnClickListener {
            override fun onBtnClick(position: Int) {
                Log.e("position preview", position.toString())
//                (CameraPreviewActivity()).showPreview(context!!)

                (activity as CameraPreviewActivity?)!!.showPreview()

            }
        })
        val layoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(contexts,
                LinearLayoutManager.HORIZONTAL, false)
        view.rvChannel.setLayoutManager(layoutManager)
        view.rvChannel.setAdapter(channelAdapter)
        fetchChannels(view)
    }

    private fun fetchChannels(view : View) {
        (channelList as ArrayList).clear()
        (channelList as ArrayList).addAll(Utilities.getList(contexts,AppConstants.CHANNEL_LIST)!!)

        if (channelList.size!! > 0) {
            view.rvChannel.visibility = View.VISIBLE
            view.tvMarketplace.visibility = View.GONE
        } else {
            view.rvChannel.visibility = View.GONE
            view.tvMarketplace.visibility = View.VISIBLE
        }
        channelAdapter.notifyDataSetChanged()
    }
/*
        //  Utilities.showProgressDialog(context)
        val request = RetrofitClients.buildService(APiService::class.java)
        val call = request.getChannelsList(categoryName)

        call?.enqueue(object : Callback<List<ChannelsResponse>> {
            override fun onResponse(call: Call<List<ChannelsResponse>>,
                                    response: Response<List<ChannelsResponse>>
            ) {
               // Utilities.hideProgressDialog()
                if (response.isSuccessful){
                    if (response.body()?.size!! > 0)
                    {
                        (channelList as ArrayList).clear()
                      //  (channelList as ArrayList).addAll(response.body() as ArrayList)
                        rvChannel.visibility = View.VISIBLE
                        tvMarketplace.visibility = View.GONE

                        Utilities.setList(contexts,AppConstants.CHANNEL_LIST,response.body() as ArrayList)

                    }
                    else{
                        rvChannel.visibility = View.GONE
                        tvMarketplace.visibility = View.VISIBLE
                    }
                    channelAdapter.notifyDataSetChanged()
                }
            }
            override fun onFailure(call: Call<List<ChannelsResponse>>, t: Throwable) {
               // Utilities.hideProgressDialog()
                Toast.makeText(context,
                    "Server not responding!!!",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }*/

}