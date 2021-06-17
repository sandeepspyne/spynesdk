package com.spyneai.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import com.spyneai.R
import com.spyneai.adapter.ShowPreviewImagesAdapter
import com.spyneai.dashboard.ui.MainDashboardActivity
import com.spyneai.model.skumap.UpdateSkuResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_preview_order.*
import java.io.File
import java.io.IOException

class PreviewOrderActivity : AppCompatActivity() {

    private lateinit var showPreviewImagesAdapter: ShowPreviewImagesAdapter
    var catName: String = "Category"
    var skuName: String = "SKU"
    lateinit var imageFileList: ArrayList<File>
    lateinit var imageFileListFrames: ArrayList<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview_order)

        fetchIntents()
        listeners()

        setRecycler()
    }

    private fun fetchIntents() {
        if (intent.getStringExtra(AppConstants.CATEGORY_NAME) != null)
            catName = intent.getStringExtra(AppConstants.CATEGORY_NAME)!!
        else
            catName = Utilities.getPreference(this, AppConstants.CATEGORY_NAME)!!

        if (intent.getStringExtra(AppConstants.SKU_NAME) != null)
            skuName = intent.getStringExtra(AppConstants.SKU_NAME)!!
        else
            skuName = Utilities.getPreference(this, AppConstants.SKU_NAME)!!

        imageFileList = ArrayList<File>()
        imageFileListFrames = ArrayList<Int>()

        imageFileList.addAll(intent.getParcelableArrayListExtra(AppConstants.ALL_IMAGE_LIST)!!)
        imageFileListFrames.addAll(intent.getIntegerArrayListExtra(AppConstants.ALL_FRAME_LIST)!!)

    }

    private fun listeners() {

        tvPlaceOrder.setOnClickListener(View.OnClickListener {
            if (Utilities.isNetworkAvailable(this)) {
                val intent = Intent(this, TimerActivity::class.java)
                intent.putExtra(AppConstants.BG_ID, "")
                intent.putExtra(AppConstants.MARKETPLACE_ID, "mark_0Q32nR")
                intent.putExtra(AppConstants.CATEGORY_NAME, catName)
                intent.putExtra(
                    AppConstants.BACKGROUND_COLOUR,
                    "https://storage.googleapis.com/spyne/AI/raw/e904fad2-727e-467a-aef5-89b3e1f06c99.jpg"
                )
                intent.putExtra(AppConstants.ALL_IMAGE_LIST, imageFileList)
                intent.putExtra(AppConstants.ALL_FRAME_LIST, imageFileListFrames)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(
                    this,
                    "No internet Connection , Please Try Again! ",
                    Toast.LENGTH_LONG
                ).show()
            }
        })

        ivBackPreviewOrder.setOnClickListener(View.OnClickListener {
            showExitDialog()
        })

    }


    private fun setRecycler() {

        showPreviewImagesAdapter = ShowPreviewImagesAdapter(this, imageFileList,
            object : ShowPreviewImagesAdapter.BtnClickListener {
                @RequiresApi(Build.VERSION_CODES.M)
                override fun onBtnClick(position: Int) {
                    //   showImagesDialog(position)
                    showImage(imageFileList[position])
                    Log.e("position preview", position.toString())
                }
            })

        rvPreview.setLayoutManager(
            GridLayoutManager(
                this,
                2)
        )
        rvPreview.setAdapter(showPreviewImagesAdapter)
        showPreviewImagesAdapter.notifyDataSetChanged()

    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun showImage(file: File) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_image)
        val window: Window = dialog.getWindow()!!
        window.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val ivFullImage: ImageView = dialog.findViewById(R.id.ivFullImage)

        ivFullImage.setImageBitmap(setImageRaw(file))

        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun setImageRaw(file: File): Bitmap? {
        val myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath())
        var ei: ExifInterface? = null
        try {
            ei = ExifInterface(file.getAbsolutePath())
        } catch (e: IOException) {
            e.printStackTrace()
        }
        assert(ei != null)
        val orientation = ei!!.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )
        val rotatedBitmap: Bitmap?
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotatedBitmap = rotateImage(myBitmap!!, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotatedBitmap = rotateImage(myBitmap!!, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotatedBitmap = rotateImage(myBitmap!!, 270f)
            ExifInterface.ORIENTATION_NORMAL -> rotatedBitmap = myBitmap
            else -> rotatedBitmap = myBitmap
        }

        return rotatedBitmap
    }

    public fun rotateImage(source: Bitmap, angle: Float): Bitmap? {
        val matrix = Matrix()

        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }

    override fun onBackPressed() {
        showExitDialog()
    }

    fun showExitDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_exit_preview)
        val dialogButtonYes: TextView = dialog.findViewById(R.id.btnYes)
        val dialogButtonNo: TextView = dialog.findViewById(R.id.btnNo)

        dialogButtonYes.setOnClickListener(View.OnClickListener {
            Utilities.savePrefrence(this@PreviewOrderActivity, AppConstants.SHOOT_ID, "")
            Utilities.savePrefrence(this@PreviewOrderActivity, AppConstants.CATEGORY_ID, "")
            Utilities.savePrefrence(this@PreviewOrderActivity, AppConstants.PRODUCT_ID, "")
            Utilities.savePrefrence(this@PreviewOrderActivity, AppConstants.SKU_NAME, "")
            Utilities.savePrefrence(this@PreviewOrderActivity, AppConstants.SKU_ID, "")
            val intent = Intent(this, MainDashboardActivity::class.java)
            startActivity(intent)
            finish()

            val updateSkuResponseList = ArrayList<UpdateSkuResponse>()
            updateSkuResponseList.clear()

            Utilities.setList(
                this@PreviewOrderActivity,
                AppConstants.FRAME_LIST, updateSkuResponseList
            )
            dialog.dismiss()

        })
        dialogButtonNo.setOnClickListener(View.OnClickListener { dialog.dismiss() })
        dialog.show()
    }
}