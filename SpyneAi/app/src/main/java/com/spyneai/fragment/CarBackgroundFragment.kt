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
import com.spyneai.adapter.CarBackgroundAdapter
import com.spyneai.model.carreplace.CarBackgroundsResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.fragment_channel.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
class CarBackgroundFragment(context: Context,categoryId: String,
                            subCategoryId: String,categoryNames : String) : Fragment() {
    lateinit var carBackgroundsResponseList : List<CarBackgroundsResponse>
    lateinit var carBackgroundAdapter: CarBackgroundAdapter
    val contexts = context
    val catId = categoryId
    val subCatId = subCategoryId
    val categoryName = categoryNames
    private var pos: Int = 0

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
        carBackgroundsResponseList = ArrayList<CarBackgroundsResponse>()
        carBackgroundAdapter = CarBackgroundAdapter(context!!,
                carBackgroundsResponseList as ArrayList<CarBackgroundsResponse>,
                pos,
                object : CarBackgroundAdapter.BtnClickListener {
                    override fun onBtnClick(position: Int) {
                        Log.e("position preview", position.toString())
                        if (position == 0)
                        {
                            (activity as CameraPreviewActivity?)!!.setImageRaw()
                        }
                        else {
                            (activity as CameraPreviewActivity?)!!.showPreviewCar()
                        }
                        Utilities.savePrefrence(contexts, AppConstants.IMAGE_ID,
                                carBackgroundsResponseList[position].imageId.toString())
                        carBackgroundAdapter.notifyDataSetChanged()
                    }
                })
        val layoutManager: RecyclerView.LayoutManager =
                LinearLayoutManager(contexts,
                        LinearLayoutManager.HORIZONTAL, false)
        view.rvChannel.setLayoutManager(layoutManager)
        view.rvChannel.setAdapter(carBackgroundAdapter)
        fetchChannels(view)
    }

    private fun fetchChannels(view : View) {
        (carBackgroundsResponseList as ArrayList).clear()

        val carBackgroundList = CarBackgroundsResponse(0,
        "https://spyne-cliq.s3.ap-south-1.amazonaws.com/spyne-cliq/default/image+2.png",
        0,"")

        (carBackgroundsResponseList as ArrayList).add(carBackgroundList)

        (carBackgroundsResponseList as ArrayList).
        addAll(Utilities.getListBackgroundsCar(contexts,
                AppConstants.BACKGROUND_LIST_CARS)!!)

        if (carBackgroundsResponseList.size!! > 0) {
            view.rvChannel.visibility = View.VISIBLE
            view.tvMarketplace.visibility = View.GONE
        } else {
            view.rvChannel.visibility = View.GONE
            view.tvMarketplace.visibility = View.VISIBLE
        }
        carBackgroundAdapter.notifyDataSetChanged()
    }
}