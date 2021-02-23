package com.spyneai.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.spyneai.R
import com.spyneai.activity.CameraPreviewActivity
import com.spyneai.needs.AppConstants
import com.spyneai.needs.ImageFilePath
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_camera_preview.*
import kotlinx.android.synthetic.main.activity_order.*
import kotlinx.android.synthetic.main.fragment_logo.*
import java.io.File

class LogoFragment(
    context: Context,
    categoryId: String,
    subCategoryId: String,
    categoryNames: String
) : Fragment() {

    private val SELECT_PICTURE = 1
    val contexts = context
    val catId = categoryId
    val subCatId = subCategoryId
    val categoryName = categoryNames
    private lateinit var photoFilePath: File
    private var savedUri: Uri? = null
    private var selectedImagePath: String? = null
    private lateinit var photoFile: File

    lateinit var ivLogo : ImageView
    lateinit var ivCorners : ImageView
    var rotat : Int = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_logo, container, false)
        listeners(view)
        return view
    }

    private fun listeners(view: View) {
        val btnUploadLogo = view.findViewById<TextView>(R.id.btnUploadLogo)
        ivLogo = view.findViewById<ImageView>(R.id.ivLogos)
        ivCorners = view.findViewById<ImageView>(R.id.ivCorner)

        btnUploadLogo.setOnClickListener(View.OnClickListener {
            val checkSelfPermission = ContextCompat.checkSelfPermission(
                contexts!!,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
                //Requests permissions to be granted to this application at runtime
                ActivityCompat.requestPermissions(
                    contexts as Activity,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1
                )
            } else {
                defaultSet()
            }
        })

        ivCorners.setOnClickListener(View.OnClickListener {
            if (rotat == 0) {
                (activity as CameraPreviewActivity?)!!.changeLogoPosition(0)
                ivCorners.rotation = 0F
                rotat = 1
            }
            else if (rotat == 1) {
                (activity as CameraPreviewActivity?)!!.changeLogoPosition(1)
                ivCorners.rotation = 90F
                rotat = 2
            }
            else if (rotat == 2)
            {
                (activity as CameraPreviewActivity?)!!.changeLogoPosition(2)
                ivCorners.rotation = 180F
                rotat = 3
            }
            else if(rotat == 3){
                (activity as CameraPreviewActivity?)!!.changeLogoPosition(3)
                ivCorners.rotation = 270F
                rotat = 0
            }
        })

    }

    private fun defaultSet() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(
                intent,
                "Select Picture"
            ), SELECT_PICTURE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                val selectedImageUri: Uri = data!!.getData()!!
                selectedImagePath = ImageFilePath.getPath(context!!, data.getData()!!);
                photoFile = File(selectedImagePath)
                savedUri = Uri.fromFile(photoFile)

                //  photoFilePath = compressFileFromBitmap()

                val myBitmapLogo = BitmapFactory.decodeFile(photoFile.absolutePath)
                ivLogos.setImageBitmap(myBitmapLogo)

                cardLogos.visibility = View.VISIBLE
                ivCorner.visibility = View.VISIBLE

                Utilities.savePrefrence(
                    contexts,
                    AppConstants.LOGO_FILE,
                    photoFile.toString()
                )
                ivCorners.performClick()
            }
        }
    }
}