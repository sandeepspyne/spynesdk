package com.spyneai.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hbisoft.pickit.PickiT
import com.hbisoft.pickit.PickiTCallbacks
import com.spyneai.R
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
import java.net.URLEncoder
import com.google.android.material.shape.CornerFamily





class GenerateGifActivity : AppCompatActivity(), PickiTCallbacks {
    private lateinit var photsAdapter: PhotosAdapter
    private lateinit var photoList: List<Photos>

    lateinit var imageList : List<String>
    public lateinit var imageFileList : ArrayList<File>
    public lateinit var imageFileListFrames : ArrayList<Int>

    public lateinit var imageInteriorFileList : ArrayList<File>
    public lateinit var imageInteriorFileListFrames : ArrayList<Int>

    public lateinit var imageFocusedFileList: ArrayList<File>
    public lateinit var imageFocusedFileListFrames: ArrayList<Int>


    var cornerPosition: String = ""

    private var currentPOsition : Int = 0
    lateinit var carBackgroundList : ArrayList<CarBackgroundsResponse>
    lateinit var carbackgroundsAdapter: CarBackgroundAdapter
    var backgroundSelect : String = ""

    var totalImagesToUPload : Int = 0
    var totalImagesToUPloadIndex : Int = 0
    lateinit var gifList : ArrayList<String>
    var catName = ""

    var exposures : String = "false"
    lateinit var windows : String

    val PICK_IMAGE = 1
    var pickiT: PickiT? = null

    var dealershipLogo: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_gif)

        Utilities.savePrefrence(this, AppConstants.EXPOSURES, exposures)

        pickiT = PickiT(this, this, this)

        setBasics()
        if (intent.getStringExtra(AppConstants.CATEGORY_NAME) != null)
            catName = intent.getStringExtra(AppConstants.CATEGORY_NAME)!!
        else
            catName = Utilities.getPreference(this, AppConstants.CATEGORY_NAME)!!

        setBackgroundsCar()
        listeners()
    }

    private fun setBasics() {
        imageFileList = ArrayList<File>()
        imageFileListFrames = ArrayList<Int>()

        imageInteriorFileList = ArrayList<File>()
        imageInteriorFileListFrames = ArrayList<Int>()

        imageFocusedFileList = ArrayList<File>()
        imageFocusedFileListFrames = ArrayList<Int>()

        //Get Intents

        imageFileList.addAll(intent.getParcelableArrayListExtra(AppConstants.ALL_IMAGE_LIST)!!)
        imageFileListFrames.addAll(intent.getIntegerArrayListExtra(AppConstants.ALL_FRAME_LIST)!!)

        if (Utilities.getPreference(this, AppConstants.CATEGORY_NAME).equals("Automobiles"))
        {
            imageInteriorFileList.addAll(intent.getParcelableArrayListExtra(AppConstants.ALL_INTERIOR_IMAGE_LIST)!!)
            imageInteriorFileListFrames.addAll(intent.getIntegerArrayListExtra(AppConstants.ALL_INTERIOR_FRAME_LIST)!!)

            imageFocusedFileList.addAll(intent.getParcelableArrayListExtra(AppConstants.ALL_FOCUSED_IMAGE_LIST)!!)
            imageFocusedFileListFrames.addAll(intent.getIntegerArrayListExtra(AppConstants.ALL_FOCUSED_FRAME_LIST)!!)
        }

        totalImagesToUPload = imageFileList.size
        windows = "inner"
        Utilities.savePrefrence(this, AppConstants.WINDOWS, windows)
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
                    backgroundSelect = carBackgroundList[position].imageId.toString()
                    carbackgroundsAdapter.notifyDataSetChanged()

                    if (position <= gifList.size - 1) {
                        Glide.with(this@GenerateGifActivity) // replace with 'this' if it's in activity
                            .load(gifList[position])
                            .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                            .into(imageViewGif)
                    } else {
                        Glide.with(this@GenerateGifActivity) // replace with 'this' if it's in activity
                            .load(R.drawable.no_sample_image)
                            .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                            .into(imageViewGif)
                    }

                    //showPreviewCar()
                }
            })
        val layoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL, false
            )
        rvBackgroundsCars.setLayoutManager(layoutManager)
        rvBackgroundsCars.setAdapter(carbackgroundsAdapter)

        fetchBackgrounds()
    }


    private fun fetchBackgrounds() {
        (carBackgroundList as ArrayList).clear()
        (carBackgroundList as ArrayList).addAll(
            Utilities.getListBackgroundsCar(
                this, AppConstants.BACKGROUND_LIST_CARS
            )!!
        )

        carbackgroundsAdapter.notifyDataSetChanged()

        backgroundSelect  = carBackgroundList[0].imageId.toString()


        if (gifList != null && gifList.size > 0) {
            if (gifList[0] != null) {
                Glide.with(this@GenerateGifActivity) // replace with 'this' if it's in activity
                    .load(gifList[0])
                    .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                    .into(imageViewGif)
            }
        }

    }

    private fun listeners() {



        tvGenerateGif.setOnClickListener(View.OnClickListener {
            if (Utilities.isNetworkAvailable(this)) {
                val intent = Intent(
                    this@GenerateGifActivity,
                    TimerActivity::class.java
                )
                intent.putExtra(AppConstants.BG_ID, backgroundSelect)
                intent.putExtra(AppConstants.ALL_IMAGE_LIST, imageFileList)
                intent.putExtra(AppConstants.ALL_FRAME_LIST, imageFileListFrames)
                intent.putExtra(AppConstants.ALL_INTERIOR_IMAGE_LIST, imageInteriorFileList)
                intent.putExtra(AppConstants.ALL_INTERIOR_FRAME_LIST, imageInteriorFileListFrames)
                intent.putExtra(AppConstants.ALL_FOCUSED_IMAGE_LIST, imageFocusedFileList)
                intent.putExtra(AppConstants.ALL_FOCUSED_FRAME_LIST, imageFocusedFileListFrames)
                intent.putExtra(AppConstants.DEALERSHIP_LOGO, dealershipLogo)
                intent.putExtra(AppConstants.CORNER_POSITION, cornerPosition)
                intent.putExtra(AppConstants.CATEGORY_NAME, catName)
//                intent.putExtra(AppConstants.GIF_LIST, gifList)
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

        ivBackGif.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })


//        toggle.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
//            if (isChecked)
//                exposures = "true"
//            else
//                exposures = "false"
//
//            Log.e("Exposure",exposures)
//            Utilities.savePrefrence(this,AppConstants.EXPOSURES,exposures)
//            // do something, the isChecked will be
//            // true if the switch is in the On position
//        })

        windows = "outer"
        Utilities.savePrefrence(this, AppConstants.WINDOWS, windows)



        /*  llTransparent.setOnClickListener(View.OnClickListener {
              llTransparent.setBackgroundResource(R.drawable.bg_selected)
              llOriginal.setBackgroundResource(R.drawable.bg_channel)
              windows = "inner"
              Utilities.savePrefrence(this,AppConstants.WINDOWS,windows)

          })

          llOriginal.setOnClickListener(View.OnClickListener {
              llOriginal.setBackgroundResource(R.drawable.bg_selected)
              llTransparent.setBackgroundResource(R.drawable.bg_channel)
              windows = "outer"
              Utilities.savePrefrence(this,AppConstants.WINDOWS,windows)

          })*/

        tvUpoadLogo.setOnClickListener {
            val getIntent = Intent(Intent.ACTION_GET_CONTENT)
            getIntent.type = "image/*"
            val pickIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickIntent.type = "image/*"
            val chooserIntent = Intent.createChooser(getIntent, "Select Logo")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))
            startActivityForResult(chooserIntent, PICK_IMAGE)
        }
        ivRemoveLogo.setOnClickListener {
            tvUpoadLogo.visibility = View.VISIBLE
            rlDealershipLogo.visibility = View.GONE
            llSelectCorner.visibility = View.GONE
            ivDealershipLogo.setImageDrawable(null);
        }

        cbTopLeft.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                if (isChecked) {
                    cornerPosition = "leftTop"
                    cbBottomLeft.setChecked(false)
                    cbTopRight.setChecked(false)
                    cbBottomRight.setChecked(false)
                }
            }
        })
        cbBottomLeft.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                if (isChecked) {
                    cornerPosition = "leftBottom"
                    cbTopLeft.setChecked(false)
                    cbTopRight.setChecked(false)
                    cbBottomRight.setChecked(false)
                }
            }
        })
        cbTopRight.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                if (isChecked) {
                    cornerPosition = "rightTop"
                    cbTopLeft.setChecked(false)
                    cbBottomLeft.setChecked(false)
                    cbBottomRight.setChecked(false)
                }
            }
        })
        cbBottomRight.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                if (isChecked) {
                    cornerPosition = "rightBottom"
                    cbTopLeft.setChecked(false)
                    cbBottomLeft.setChecked(false)
                    cbTopRight.setChecked(false)
                }
            }
        })

        ivShowPopup.setOnClickListener {
            showDealershipDialog()
        }

    }


    override fun onBackPressed() {
        //  super.onBackPressed()
        showExitDialog()
    }

    fun showExitDialog( ) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_exit)
        val dialogButtonYes: TextView = dialog.findViewById(R.id.btnYes)
        val dialogButtonNo: TextView = dialog.findViewById(R.id.btnNo)

        dialogButtonYes.setOnClickListener(View.OnClickListener {
            Utilities.savePrefrence(this@GenerateGifActivity, AppConstants.SHOOT_ID, "")
            Utilities.savePrefrence(this@GenerateGifActivity, AppConstants.CATEGORY_ID, "")
            Utilities.savePrefrence(this@GenerateGifActivity, AppConstants.PRODUCT_ID, "")
            Utilities.savePrefrence(this@GenerateGifActivity, AppConstants.SKU_NAME, "")
            Utilities.savePrefrence(this@GenerateGifActivity, AppConstants.SKU_ID, "")


            val updateSkuResponseList = ArrayList<UpdateSkuResponse>()
            updateSkuResponseList.clear()

            Utilities.setList(
                this@GenerateGifActivity,
                AppConstants.FRAME_LIST, updateSkuResponseList
            )

            val intent = Intent(this, DashboardActivity::class.java)
            dialog.dismiss()

            startActivity(intent)
            finish()

        })
        dialogButtonNo.setOnClickListener(View.OnClickListener { dialog.dismiss() })
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && data != null) {
                val selectedLogoUri: Uri = data?.getData()!!
                Log.e("logoPath", selectedLogoUri.toString())
                showLogo(selectedLogoUri)
            try {
                var file = selectedLogoUri?.toFile()
                dealershipLogo = file?.path!!.toString()
            }catch (ex: IllegalArgumentException){
                pickiT?.getPath(selectedLogoUri, Build.VERSION.SDK_INT)
            }

        }
    }

    fun showLogo(selectedLogoUri: Uri) {

        Glide.with(this).load(selectedLogoUri.toString()).into(ivDealershipLogo)
        tvUpoadLogo.visibility = View.GONE
        rlDealershipLogo.visibility = View.VISIBLE
        llSelectCorner.visibility = View.VISIBLE
    }

    override fun PickiTonUriReturned() {

    }

    override fun PickiTonStartListener() {
    }

    override fun PickiTonProgressUpdate(progress: Int) {
    }

    override fun PickiTonCompleteListener(
        path: String?,
        wasDriveFile: Boolean,
        wasUnknownProvider: Boolean,
        wasSuccessful: Boolean,
        Reason: String?
    ) {
        dealershipLogo = path.toString()
    }

    private fun showDealershipDialog(){

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dealership_dialog)
        dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        val ivClose: ImageView = dialog.findViewById(R.id.ivClose)


        ivClose.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
        })
        dialog.show()

    }
}