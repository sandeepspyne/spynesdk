package com.spyneai.fragment

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.ShootSiteDialog
import com.spyneai.base.BaseFragment
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.databinding.FragmentPreferenceBinding
import com.spyneai.databinding.HomeDashboardFragmentBinding
import com.spyneai.logout.LogoutDialog
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shoot.ui.dialogs.RequiredPermissionDialog
import kotlinx.android.synthetic.main.fragment_preference.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class PreferenceFragment : BaseFragment<DashboardViewModel, FragmentPreferenceBinding>(){

    val REQUEST_IMAGE_CAPTURE = 1

//    var languageList = arrayOf("English","Germany","Italy")
    var languageList= arrayListOf<String>()
    lateinit var spLanguageAdapter: ArrayAdapter<String>
    lateinit var currentPhotoPath: String


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // set llLogout Button Margin only for sypne app

        if (getString(R.string.app_name) == AppConstants.SPYNE_AI){
            val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(30, 30, 30, 260)
            params.gravity= Gravity.CENTER_HORIZONTAL;
            binding.llLogout.setLayoutParams(params)
//            Toast.makeText(requireContext(), "running", Toast.LENGTH_SHORT).show()
        }

        languageList.clear()

        when(getString(R.string.app_name)) {
                AppConstants.AUTO_FOTO -> {
                    languageList.add("English")
                    languageList.add("German")
                    languageList.add("Italy")
                }
            else ->languageList.add("English")
          }

        spLanguageAdapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            languageList)
        binding.spLanguage.adapter = spLanguageAdapter


        if (getString(R.string.app_name) == AppConstants.AUTO_FOTO){
            when(Utilities.getPreference(requireContext(),AppConstants.LOCALE)){
                "en" -> binding.spLanguage.setSelection(0)
                "de" -> binding.spLanguage.setSelection(1)
                "IT" -> binding.spLanguage.setSelection(2)
            }
        }

        //Project Name Switch Visibility According to App Name

        when(getString(R.string.app_name)) {
            AppConstants.FLIPKART,
            AppConstants.UDAAN,
            AppConstants.LAL_10,
            AppConstants.AMAZON,
            AppConstants.SWIGGY -> {
                binding.llProjectNameSwitch.visibility=View.VISIBLE
                statusSwitch()
            }
            else ->
                binding.llProjectNameSwitch.visibility=View.GONE
        }

        if (Utilities.getPreference(requireContext(), AppConstants.USER_EMAIL).toString() != ""){

            binding.tvUserName.setText(Utilities.getPreference(requireContext(), AppConstants.USER_NAME))
//            binding.tvLoginAs.setTextText(Utilities.getPreference(requireContext(), AppConstants.USER_NAME))
            binding.tvEmail.setText(Utilities.getPreference(requireContext(), AppConstants.USER_EMAIL))
            binding.tvAppVersion.setText(Utilities.getPreference(requireContext(), AppConstants.APP_VERSION))
        }

        spLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position==0){
                    //if (Utilities.getPreference(requireContext(),AppConstants.LOCALE) == "en")
                        onLanguageSelected("en")
                }else if(position==1){
                    onLanguageSelected( "de")
                }else {
                    onLanguageSelected("IT")
                }

                refreshTexts()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // another interface callback
            }
        }

        binding.llLogout.setOnClickListener {
            LogoutDialog().show(requireActivity().supportFragmentManager,"LogoutDialog")

        }
        binding.btClockIn.setOnClickListener {
            if (allPermissionsGranted())
                onPermissionGranted()
            else
                permissionRequest.launch(permissions.toTypedArray())
        }

        if (Utilities.getBool(requireContext(),AppConstants.CLOCKED_IN)){
            setCheckOut(Utilities.getPreference(requireContext(),AppConstants.SITE_IMAGE_PATH))
        }else {
           setCheckIn()
        }

    }

    private fun setCheckIn() {
        binding.apply {
            cvClockIn.visibility = View.VISIBLE
            cvClockOut.visibility = View.GONE
        }
        viewModel.isStartAttendance.observe(viewLifecycleOwner, {
            if (it){
                binding.btClockIn.visibility=View.GONE
                binding.btClockOut.visibility=View.VISIBLE
                viewModel.isStartAttendance.value=false
                dispatchTakePictureIntent()
            }
        })
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        requireContext(),
                        "com.spyneai.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

        return File.createTempFile(
            Utilities.getPreference(requireContext(),AppConstants.TOKEN_ID)+"_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun setCheckOut(imagePath : String?) {
        binding.apply {
            cvClockIn.visibility = View.GONE
            cvClockOut.visibility = View.VISIBLE
        }

        imagePath?.let {
            Glide.with(requireContext())
                .load(it)
                .into(binding.ivSiteImage)
        }

    }

    private fun refreshTexts() {
        binding.apply {
            tvLang.text = getString(R.string.language)
            tvProjectName.text = getString(R.string.project_name)
            tvPassword.text = getString(R.string.change_password)
            tvAppVersionLabel.text = getString(R.string.app_version)
            tvLogout.text = getString(R.string.logout)
//            tvLogin.text = getString(R.string.you_are_logged_in_as)
        }
    }

    private fun onLanguageSelected(locale: String) {
        Utilities.savePrefrence(requireContext(), AppConstants.LOCALE,locale)

        val locale = Locale(locale)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun statusSwitch(){
        binding.switchProjectName.isChecked =
            Utilities.getPreference(requireContext(), AppConstants.STATUS_PROJECT_NAME).toString() =="true"

        binding.switchProjectName.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Utilities.savePrefrence(requireContext(), AppConstants.STATUS_PROJECT_NAME, "true")
            }
            else Utilities.savePrefrence(requireContext(), AppConstants.STATUS_PROJECT_NAME, "false")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            setCheckOut(currentPhotoPath)
            Utilities.savePrefrence(requireContext(),AppConstants.SITE_IMAGE_PATH,currentPhotoPath)
            Utilities.saveBool(requireContext(),AppConstants.CLOCKED_IN,true)
        }
    }

    protected fun allPermissionsGranted() = permissions.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private val permissions = mutableListOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add(Manifest.permission.ACCESS_MEDIA_LOCATION)
        }
    }

    private val permissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

            if (permissions.all {
                    it.value
                }) {
                onPermissionGranted()
            } else {
                RequiredPermissionDialog().show(requireActivity().supportFragmentManager, "RequiredPermissionDialog")
            }

        }

    open fun onPermissionGranted() {
        ShootSiteDialog().show(requireActivity().supportFragmentManager,"ShootSiteDialog")
    }

    override fun getViewModel() = DashboardViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentPreferenceBinding.inflate(inflater, container, false)
}

