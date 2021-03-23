package com.spyneai.fragment

import android.content.Context

import com.spyneai.adapter.MyOrdersSubmittedAdapter

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
import com.spyneai.adapter.MyOrdersOngoingAdapter
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClient
import com.spyneai.model.orders.Data
import com.spyneai.model.orders.MyOrdersResponse
import com.spyneai.model.orders.Ongoing
import com.spyneai.model.orders.Submitted
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.fragment_orders.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OrdersFragment(context: Context) : Fragment() {

    lateinit var myOrdersResponseList : List<Data>
    lateinit var myOrdersResponseListOngoing : List<Ongoing>
    lateinit var myOrdersResponseListSubmitted : List<Submitted>
    lateinit var myOrdersOngoingAdapter: MyOrdersOngoingAdapter
    lateinit var myOrdersSubmittedAdapter: MyOrdersSubmittedAdapter
    val contexts = context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_orders, container, false)
        setRecyclerOngoingOrders(view)
        setRecyclerSubmittedOrders(view)
        if (Utilities.isNetworkAvailable(contexts))
            fetchOrders(view)
        else
            Toast.makeText(contexts,
                "Please check your internet connection",
                Toast.LENGTH_SHORT).show()
        return view
    }

    private fun setRecyclerOngoingOrders(view: View) {
        myOrdersResponseListOngoing = ArrayList<Ongoing>()
        myOrdersOngoingAdapter = MyOrdersOngoingAdapter(contexts,
            myOrdersResponseListOngoing as ArrayList<Ongoing>,
                object : MyOrdersOngoingAdapter.BtnClickListener {
                    override fun onBtnClick(position: Int) {
                        Log.e("position preview", position.toString())
                    }
                })
        val layoutManager: RecyclerView.LayoutManager =
                LinearLayoutManager(contexts,
                        LinearLayoutManager.VERTICAL, false)
        view.rvMyOrdersOngoing.setLayoutManager(layoutManager)
        view.rvMyOrdersOngoing.setAdapter(myOrdersOngoingAdapter)
    }


    private fun setRecyclerSubmittedOrders(view: View) {
        myOrdersResponseListSubmitted = ArrayList<Submitted>()

        myOrdersSubmittedAdapter = MyOrdersSubmittedAdapter(contexts,
            myOrdersResponseListSubmitted as ArrayList<Submitted>,
                object : MyOrdersSubmittedAdapter.BtnClickListener {
                    override fun onBtnClick(position: Int) {
                        Log.e("position preview", position.toString())
                    }
                })
        val layoutManager: RecyclerView.LayoutManager =
                LinearLayoutManager(contexts,
                        LinearLayoutManager.VERTICAL, false)
        view.rvMyOrdersSubmitted.setLayoutManager(layoutManager)
        view.rvMyOrdersSubmitted.setAdapter(myOrdersSubmittedAdapter)

    }

    private fun fetchOrders(view: View) {
        Utilities.showProgressDialog(contexts)
        (myOrdersResponseListOngoing as ArrayList).clear()
        (myOrdersResponseListSubmitted as ArrayList).clear()

        val request = RetrofitClient.buildService(APiService::class.java)
        val call = request.getMyOrders(Utilities.getPreference(contexts, AppConstants.tokenId))

        call?.enqueue(object : Callback<MyOrdersResponse> {
            override fun onResponse(call: Call<MyOrdersResponse>,
                                    response: Response<MyOrdersResponse>
            ) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful){
                    if (response.body()?.payload!!.data.ongoing.size > 0)
                    {
                        (myOrdersResponseListOngoing as ArrayList).addAll(response.body()?.payload!!.data.ongoing)
                    }
                    if (response.body()?.payload!!.data.submitted.size > 0)
                    {
                        (myOrdersResponseListSubmitted as ArrayList).addAll(response.body()?.payload!!.data.submitted)
                    }

                    myOrdersOngoingAdapter.notifyDataSetChanged()
                    myOrdersSubmittedAdapter.notifyDataSetChanged()
                }
            }
            override fun onFailure(call: Call<MyOrdersResponse>, t: Throwable) {
                Utilities.hideProgressDialog()
                Toast.makeText(contexts,
                    "Server not responding!!!",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }

}