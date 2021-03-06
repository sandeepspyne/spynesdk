package com.spyneai.spyneaidemo.activity

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.activity.DashboardActivity
import com.spyneai.activity.TimerActivity
import com.spyneai.adapter.CarBackgroundAdapter
import com.spyneai.adapter.PhotosAdapter
import com.spyneai.aipack.*
import com.spyneai.model.carreplace.CarBackgroundsResponse
import com.spyneai.model.sku.Photos
import com.spyneai.model.skumap.UpdateSkuResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_edit_sku.*
import kotlinx.android.synthetic.main.activity_generate_gif.*
import kotlinx.android.synthetic.main.activity_order.*
import kotlinx.android.synthetic.main.activity_shoot_selection.*
import kotlinx.android.synthetic.main.activity_show_gif.*
import java.io.File

class GenerateGifDemoActivity : AppCompatActivity() {
    private lateinit var photsAdapter: PhotosAdapter
    private lateinit var photoList: List<Photos>

    lateinit var imageList : List<String>
    public lateinit var imageFileList : ArrayList<File>
    public lateinit var imageFileListFrames : ArrayList<Int>

    private var currentPOsition : Int = 0
    lateinit var carBackgroundList : ArrayList<CarBackgroundsResponse>
    lateinit var carbackgroundsAdapter: CarBackgroundAdapter
    var backgroundSelect : String = ""

    var totalImagesToUPload : Int = 0
    var totalImagesToUPloadIndex : Int = 0
    lateinit var gifList : ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_gif)

        setBackgroundsCar()
        listeners()

        imageFileList = ArrayList<File>()
        imageFileListFrames = ArrayList<Int>()

        //Get Intents

        imageFileList.addAll(intent.getParcelableArrayListExtra(AppConstants.ALL_IMAGE_LIST)!!)
        imageFileListFrames.addAll(intent.getIntegerArrayListExtra(AppConstants.ALL_FRAME_LIST)!!)
        totalImagesToUPload = imageFileList.size
    }

    private fun setBackgroundsCar() {
        carBackgroundList = ArrayList<CarBackgroundsResponse>()
        gifList = ArrayList<String>()
        gifList.addAll(intent.getParcelableArrayListExtra(AppConstants.GIF_LIST)!!)

        carbackgroundsAdapter = CarBackgroundAdapter(this,
            carBackgroundList as ArrayList<CarBackgroundsResponse>, 0,
            object : CarBackgroundAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    Log.e("position preview", position.toString())
                    //if (position<carBackgroundList.size)
                    backgroundSelect  = carBackgroundList[position].imageId.toString()
                    carbackgroundsAdapter.notifyDataSetChanged()

                    if (position>=2){
                        showBackgroundDialog()
                    }

                    Glide.with(this@GenerateGifDemoActivity) // replace with 'this' if it's in activity
                        .load(gifList[position])
                        .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                        .into(imageViewGif)

                    //showPreviewCar()
                }
            })
        val layoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false)
        rvBackgroundsCars.setLayoutManager(layoutManager)
        rvBackgroundsCars.setAdapter(carbackgroundsAdapter)

        fetchBackgrounds()
    }


    private fun fetchBackgrounds() {
        (carBackgroundList as ArrayList).clear()
        (carBackgroundList as ArrayList).addAll(
            Utilities.getListBackgroundsCar(
                this,AppConstants.BACKGROUND_LIST_CARS)!!)

        carbackgroundsAdapter.notifyDataSetChanged()

        backgroundSelect  = carBackgroundList[0].imageId.toString()

        Glide.with(this@GenerateGifDemoActivity) // replace with 'this' if it's in activity
            .load(gifList[0])
            .error(R.mipmap.defaults) // show error drawable if the image is not a gif
            .into(imageViewGif)

    }

    private fun listeners() {
        Log.e("Generate  SKU",
            Utilities.getPreference(this,
                AppConstants.SKU_NAME)!!)
        tvGenerateGif.setOnClickListener(View.OnClickListener {
            if (Utilities.isNetworkAvailable(this))
            {
                val intent = Intent(this@GenerateGifDemoActivity,
                    TimerDemoActivity::class.java)
                intent.putExtra(AppConstants.BG_ID,backgroundSelect)
                intent.putExtra(AppConstants.ALL_IMAGE_LIST, imageFileList)
                intent.putExtra(AppConstants.ALL_FRAME_LIST, imageFileListFrames)
                intent.putExtra(AppConstants.GIF_LIST, gifList)
                startActivity(intent)
                finish()
            }
            else{
                Toast.makeText(this,
                    "No internet Connection , Please Try Again! ",
                    Toast.LENGTH_LONG).show()
            }
        })

        ivBackGif.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })

    }


    override fun onBackPressed() {
        //  super.onBackPressed()
        showExitDialog()
    }

    private fun showBackgroundDialog(){

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_demo_exit)
        val dialogButtonYes: TextView = dialog.findViewById(R.id.btnExitDemo)
        val dialogButtonNo: TextView = dialog.findViewById(R.id.btnNo)

        dialogButtonYes.setOnClickListener(View.OnClickListener {
            Utilities.savePrefrence(this@GenerateGifDemoActivity, AppConstants.SHOOT_ID, "")
            Utilities.savePrefrence(this@GenerateGifDemoActivity, AppConstants.CATEGORY_ID, "")
            Utilities.savePrefrence(this@GenerateGifDemoActivity, AppConstants.PRODUCT_ID, "")
            Utilities.savePrefrence(this@GenerateGifDemoActivity, AppConstants.SKU_NAME, "")
            Utilities.savePrefrence(this@GenerateGifDemoActivity, AppConstants.SKU_ID, "")


            val updateSkuResponseList = ArrayList<UpdateSkuResponse>()
            updateSkuResponseList.clear()

            Utilities.setList(
                this@GenerateGifDemoActivity,
                AppConstants.FRAME_LIST, updateSkuResponseList
            )
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
            dialog.dismiss()

        })
        dialogButtonNo.setOnClickListener(View.OnClickListener { dialog.dismiss() })
        dialog.show()


    }

    fun showExitDialog( ) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_exit)
        val dialogButtonYes: TextView = dialog.findViewById(R.id.btnYes)
        val dialogButtonNo: TextView = dialog.findViewById(R.id.btnNo)

        dialogButtonYes.setOnClickListener(View.OnClickListener {
            Utilities.savePrefrence(this@GenerateGifDemoActivity, AppConstants.SHOOT_ID, "")
            Utilities.savePrefrence(this@GenerateGifDemoActivity, AppConstants.CATEGORY_ID, "")
            Utilities.savePrefrence(this@GenerateGifDemoActivity, AppConstants.PRODUCT_ID, "")
            Utilities.savePrefrence(this@GenerateGifDemoActivity, AppConstants.SKU_NAME, "")
            Utilities.savePrefrence(this@GenerateGifDemoActivity, AppConstants.SKU_ID, "")


            val updateSkuResponseList = ArrayList<UpdateSkuResponse>()
            updateSkuResponseList.clear()

            Utilities.setList(
                this@GenerateGifDemoActivity,
                AppConstants.FRAME_LIST, updateSkuResponseList
            )
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
            dialog.dismiss()

        })
        dialogButtonNo.setOnClickListener(View.OnClickListener { dialog.dismiss() })
        dialog.show()
    }

}