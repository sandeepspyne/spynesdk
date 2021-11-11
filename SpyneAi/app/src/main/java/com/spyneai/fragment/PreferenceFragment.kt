package com.spyneai.fragment

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.spyneai.R
import com.spyneai.ShootSiteDialog
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.ClipperApi
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentPreferenceBinding
import com.spyneai.interfaces.GcpClient
import com.spyneai.logout.LogoutDialog
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shoot.ui.dialogs.RequiredPermissionDialog
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class PreferenceFragment : BaseFragment<DashboardViewModel, FragmentPreferenceBinding>(),
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    val REQUEST_IMAGE_CAPTURE = 1

//    var languageList = arrayOf("English","Germany","Italy")
    var languageList= arrayListOf<String>()
    lateinit var spLanguageAdapter: ArrayAdapter<String>
    lateinit var currentPhotoPath: String
    private var googleApiClient: GoogleApiClient? = null
    val location_data = JSONObject()
    var snackbar: Snackbar? = null
    var isActive = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // set llLogout Button Margin only for sypne app

        googleApiClient = GoogleApiClient.Builder(requireContext(), this, this).addApi(LocationServices.API).build()


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

        binding.spLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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

        binding.btnClockOut.setOnClickListener {
            clockInOut("checkout","")
            observeClockInOut("checkout","")
        }

        if (Utilities.getBool(requireContext(),AppConstants.CLOCKED_IN)){
            setCheckOut(Utilities.getPreference(requireContext(),AppConstants.SITE_IMAGE_PATH),false)
        }else {
           setCheckIn(false)
        }

    }

    private fun setCheckIn(hideClockOut : Boolean) {
        binding.llAttendance.setOnClickListener {
            when(binding.ivDropDown.rotation){
                0f -> {
                    binding.ivDropDown.rotation = 90f
                    binding.apply {
                        cvClockIn.visibility = View.VISIBLE
                        cvClockOut.visibility = View.GONE
                    }

                    setLastSession()
                }

                90f -> {
                    binding.ivDropDown.rotation = 0f
                    binding.apply {
                        cvClockIn.visibility = View.GONE
                    }
                }
            }
        }

        if (hideClockOut){
            binding.apply {
                cvClockIn.visibility = View.VISIBLE
                cvClockOut.visibility = View.GONE
            }

            setLastSession()
        }

        viewModel.isStartAttendance.observe(viewLifecycleOwner, {
            if (it){
                viewModel.isStartAttendance.value=false
                dispatchTakePictureIntent()
            }
        })
    }

    private fun setLastSession() {
        val millis = Utilities.getLong(requireContext(),AppConstants.SHOOTS_SESSION)

        if (millis == 0L)
            binding.tvSession.visibility = View.GONE
        else
            binding.tvSession.text = "Your last session was "+millisecondsToHours(millis)
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

    private fun setCheckOut(imagePath : String?,clockIn : Boolean) {
        val time = System.currentTimeMillis() - Utilities.getLong(requireContext(),AppConstants.CLOCKED_IN_TIME)

        isActive = true
        upDateTimer(time)

        binding.apply {
            cvClockIn.visibility = View.GONE
            cvClockOut.visibility = View.VISIBLE
            tvCityName.text = Utilities.getPreference(requireContext(),AppConstants.SITE_CITY_NAME)
            ivDropDown.rotation = 90f
            tvClockedTime.text = getString(R.string.clocked_in_for)+" "+millisecondsToTime(time)
            llAttendance.setOnClickListener(null)
        }

        imagePath?.let {
            Glide.with(requireContext())
                .load(it)
                .into(binding.ivSiteImage)

            if (clockIn){
                getGcpUrl(it)
                observeUrlResponse(it)
            }
        }
    }

    private fun upDateTimer(time: Long) {
        if (isActive){
            binding.tvClockedTime.text = getString(R.string.clocked_in_for)+" "+millisecondsToTime(time)

            Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
                override fun run() {
                    upDateTimer(time.plus(1000))
                }
            },1000)
        }
    }

    private fun observeUrlResponse(imagePath: String) {
        viewModel.gcpUrlResponse.observe(viewLifecycleOwner,{
            when(it){
                is Resource.Success -> {
                    //upload to gcp
                    uploadImageToGcpUrl(imagePath,it.value.data.presignedUrl,it.value.data.fileUrl)
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it){ getGcpUrl(imagePath) }
                }
            }
        })
    }

    private fun uploadImageToGcpUrl(path : String,preSignedUrl: String, fileUrl: String) {
        val requestFile =
            File(path).asRequestBody("text/x-markdown; charset=utf-8".toMediaTypeOrNull())

        //upload video with presigned url
        val request = GcpClient.buildService(ClipperApi::class.java)

        val call = request.uploadVideo(
            "application/octet-stream",
            preSignedUrl,
            requestFile
        )

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d("VideoUploader", "onResponse: "+response.code())
                Utilities.hideProgressDialog()
                if (response.isSuccessful){
                    //set clock in
                    clockInOut("checkin",fileUrl)
                    observeClockInOut("checkin",fileUrl)
                }else {
                    //retry gcp upload
                    showErrorSnackBar(path,preSignedUrl,fileUrl)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Utilities.hideProgressDialog()
                //retry gcp upload
                showErrorSnackBar(path,preSignedUrl,fileUrl)
            }

        })
    }

    private fun observeClockInOut(type: String, fileUrl: String) {
        viewModel.checkInOutRes.observe(viewLifecycleOwner,{
            when(it){
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    if (type == "checkin"){
                        Toast.makeText(requireContext(),"Clocked in successfully...",Toast.LENGTH_LONG).show()
                        isActive = true
                        upDateTimer(
                            System.currentTimeMillis() - Utilities.getLong(requireContext(),AppConstants.CLOCKED_IN_TIME)
                        )
                    }else{
                        Toast.makeText(requireContext(),"Clocked out successfully...",Toast.LENGTH_LONG).show()
                        //save session time
                        Utilities.apply {
                            saveLong(requireContext(),
                                AppConstants.SHOOTS_SESSION,
                                System.currentTimeMillis() - Utilities.getLong(requireContext(),AppConstants.CLOCKED_IN_TIME))
                            saveBool(requireContext(),AppConstants.CLOCKED_IN,false)
                        }

                        setCheckIn(true)
                    }
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it){ clockInOut(type,fileUrl)}
                }
            }
        })
    }

    private fun clockInOut(type : String, fileUrl: String) {
        Utilities.showProgressDialog(requireContext())
        viewModel.captureCheckInOut(
            type,
            location_data,
            fileUrl
        )
    }

    private fun showErrorSnackBar(path: String, preSignedUrl: String, fileUrl: String) {
        snackbar = Snackbar.make(
            binding.root,
            "Failed to upload image",
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction("Retry") {
                uploadImageToGcpUrl(path, preSignedUrl,fileUrl)
            }
            .setActionTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.primary
                )
            )

        snackbar?.show()
    }

    private fun getGcpUrl(imagePath : String) {
        Utilities.showProgressDialog(requireContext())
        viewModel.getGCPUrl(File(imagePath).name)
    }


    override fun onStart() {
        super.onStart()
        googleApiClient?.connect()
    }

    override fun onResume() {
        super.onResume()
        if (Utilities.getBool(requireContext(),AppConstants.CLOCKED_IN)){
            isActive = true
            upDateTimer(
                System.currentTimeMillis() - Utilities.getLong(requireContext(),AppConstants.CLOCKED_IN_TIME)
            )
        }
    }

    override fun onStop() {
        super.onStop()
        isActive = false
        googleApiClient?.disconnect()
    }

    private fun refreshTexts() {
        binding.apply {
            tvLang.text = getString(R.string.language)
            tvProjectName.text = getString(R.string.project_name)
            tvPassword.text = getString(R.string.change_password)
            tvAppVersionLabel.text = getString(R.string.app_version)
            tvLogout.text = getString(R.string.logout)
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
            Utilities.apply {
                savePrefrence(requireContext(),AppConstants.SITE_IMAGE_PATH,currentPhotoPath)
                savePrefrence(requireContext(),AppConstants.SITE_CITY_NAME,location_data.getString("city"))
                saveBool(requireContext(),AppConstants.CLOCKED_IN,true)
                saveLong(requireContext(),AppConstants.CLOCKED_IN_TIME,System.currentTimeMillis())
            }

            setCheckOut(currentPhotoPath,true)
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

    override fun onConnected(p0: Bundle?) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                val lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
                val lat: Double = lastLocation.latitude
                val lon: Double = lastLocation.longitude

                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val addresses: List<Address> = geocoder.getFromLocation(lat, lon, 1)
                val postalCode = addresses[0].postalCode
                val cityName = addresses[0].locality
                val countryName = addresses[0].countryName

                location_data.put("city", cityName)
                location_data.put("country", countryName)
                location_data.put("latitude", lat)
                location_data.put("longitude", lon)
                location_data.put("postalCode", postalCode)
            }catch (e : Exception){
                e.printStackTrace()
            }

        }
    }

    override fun onConnectionSuspended(p0: Int) {

    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }

    private fun millisecondsToHours(millis : Long): String? {
        return String.format(
            "%02d hours %02d min", TimeUnit.MILLISECONDS.toHours(millis),
            TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(
                TimeUnit.MILLISECONDS.toHours(
                    millis
                )
            )
        )
    }

    private fun millisecondsToTime(milliseconds: Long): String? {
        var hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        var minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.HOURS.toMinutes(
            TimeUnit.MILLISECONDS.toHours(
                milliseconds
            )
        )


        val seconds = milliseconds / 1000 % 60
        val secondsStr = seconds.toString()
        val secs = getTwoDigit(secondsStr)

        return if (hours > 0) "${getTwoDigit(hours.toString())}:${getTwoDigit(minutes.toString())}:$secs" else "${getTwoDigit(minutes.toString())}:$secs"
    }

    private fun getTwoDigit(value : String) : String {
        return if (value.length >= 2) {
            value.substring(0, 2)
        } else {
            "0$value"
        }
    }
}

