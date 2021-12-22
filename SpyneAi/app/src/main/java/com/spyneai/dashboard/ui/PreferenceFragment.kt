package com.spyneai.dashboard.ui

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.spyneai.*
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.ClipperApi
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.dashboard.data.model.LocationsRes
import com.spyneai.databinding.FragmentPreferenceBinding
import com.spyneai.interfaces.GcpClient
import com.spyneai.logout.LogoutDialog
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.ui.dialogs.RequiredPermissionDialog
import com.spyneai.shoot.utils.log
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
import kotlin.collections.ArrayList
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import android.location.LocationManager

import androidx.core.app.ActivityCompat

import com.spyneai.extras.MainActivity
import android.content.Context.LOCATION_SERVICE
import android.content.Context.LOCATION_SERVICE
import android.location.Criteria
import android.content.Context.LOCATION_SERVICE

import com.iceteck.silicompressorr.videocompression.MediaController.mContext
import android.content.Context.LOCATION_SERVICE
import android.content.DialogInterface
import android.location.LocationListener
import android.provider.Settings
import android.widget.Toast as Toast
import android.widget.TextView
import com.google.android.gms.location.*
import com.spyneai.R


class PreferenceFragment : BaseFragment<DashboardViewModel, FragmentPreferenceBinding>() {

    val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    //    var languageList = arrayOf("English","Germany","Italy")
    var languageList = arrayListOf<String>()
    var locationList: ArrayList<String> = ArrayList()
    lateinit var spLanguageAdapter: ArrayAdapter<String>
    lateinit var spLocationAdapter: ArrayAdapter<String>
    lateinit var currentPhotoPath: String
    val location_data = JSONObject()
    var snackbar: Snackbar? = null
    var isActive = false
    var selectedLat: Double? = 0.0
    var selectdLong: Double? = 0.0
    var currentLat: Double? = 0.0
    var currentLong: Double? = 0.0
    var location_accessed_from: String? =""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val locationManager = requireContext().getSystemService(LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            OnGPS()
        } else {
            getLocationData()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if (getString(R.string.app_name) == AppConstants.SPYNE_AI) {
            val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(30, 30, 30, 260)
            params.gravity = Gravity.CENTER_HORIZONTAL;
            binding.llLogout.setLayoutParams(params)
        }

        //

        if (Utilities.getPreference(requireContext(),AppConstants.ENTERPRISE_ID) == AppConstants.SPYNE_ENTERPRISE_ID){
            binding.llAttendance.visibility = View.GONE
        }else {
            locationList.add("Select Location")
            spLocationAdapter = ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                locationList
            )

            //clockin Adapter
            binding.spSelectLocation.adapter = spLocationAdapter
            binding.spSelectLocation.setTitle("")

            binding.spSelectLocation.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position == 0) {
                        binding.btClockIn.enable(false)
                    } else {
                        binding.btClockIn.enable(true)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

            //clockout Adapter
            binding.spLocationOut.adapter = spLocationAdapter
            binding.spLocationOut.setTitle("")

            binding.spLocationOut.setOnItemSelectedListener(object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    if (position == 0) {
                        binding.btnClockOut.enable(false)
                    } else {
                        binding.btnClockOut.enable(true)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            })

            binding.btClockIn.setOnClickListener {
                if (allPermissionsGranted())
                    onPermissionGranted()
                else
                    permissionRequest.launch(permissions.toTypedArray())

            }



            binding.btnClockOut.setOnClickListener {
                viewModel.type = "checkout"
                getLocationData()
                getDistanceFromLatLon(currentLat!!, currentLong!!, "checkout")
            }
            if (Utilities.getBool(requireContext(), AppConstants.CLOCKED_IN)) {
                viewModel.siteImagePath =
                    Utilities.getPreference(requireContext(), AppConstants.SITE_IMAGE_PATH).toString()
                setCheckOut(false)
            } else {
                setCheckIn(false)
            }

            observeUrlResponse()
            observeClockInOut()

            getLocations()
            observeLocation()
        }

        languageList.clear()

        when (getString(R.string.app_name)) {
            AppConstants.AUTO_FOTO -> {
                languageList.add("English")
                languageList.add("German")
                languageList.add("Italy")
            }
            else -> languageList.add("English")
        }

        spLanguageAdapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            languageList
        )
        binding.spLanguage.adapter = spLanguageAdapter


        if (getString(R.string.app_name) == AppConstants.AUTO_FOTO) {
            when (Utilities.getPreference(requireContext(), AppConstants.LOCALE)) {
                "en" -> binding.spLanguage.setSelection(0)
                "de" -> binding.spLanguage.setSelection(1)
                "IT" -> binding.spLanguage.setSelection(2)
            }
        }

        //Project Name Switch Visibility According to App Name

        when (getString(R.string.app_name)) {
            AppConstants.FLIPKART,
            AppConstants.UDAAN,
            AppConstants.LAL_10,
            AppConstants.AMAZON,
            AppConstants.SWIGGY -> {
                binding.llProjectNameSwitch.visibility = View.VISIBLE
                statusSwitch()
            }
            else ->
                binding.llProjectNameSwitch.visibility = View.GONE
        }

        if (Utilities.getPreference(requireContext(), AppConstants.USER_EMAIL).toString() != "") {

            binding.tvUserName.setText(
                Utilities.getPreference(
                    requireContext(),
                    AppConstants.USER_NAME
                )
            )
//            binding.tvLoginAs.setTextText(Utilities.getPreference(requireContext(), AppConstants.USER_NAME))
            binding.tvEmail.setText(
                Utilities.getPreference(
                    requireContext(),
                    AppConstants.USER_EMAIL
                )
            )
            binding.tvAppVersion.setText(
                Utilities.getPreference(
                    requireContext(),
                    AppConstants.APP_VERSION
                )
            )
        }

        binding.spLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) {
                    //if (Utilities.getPreference(requireContext(),AppConstants.LOCALE) == "en")
                    onLanguageSelected("en")
                } else if (position == 1) {
                    onLanguageSelected("de")
                } else {
                    onLanguageSelected("IT")
                }

                refreshTexts()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // another interface callback
            }
        }

        binding.llLogout.setOnClickListener {
            LogoutDialog().show(requireActivity().supportFragmentManager, "LogoutDialog")

        }


    }

    private fun OnGPS() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes",
            DialogInterface.OnClickListener { dialog, which -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) })
            .setNegativeButton("No",
                DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    private fun observeLocation() {
        viewModel.locationsResponse.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    val locationNameList = it.value.data.map { it -> it.locationName }
                        .toMutableList() as ArrayList<String>

                    locationList.apply {
                        locationList.clear()
                        locationList.add("Select Location")
                        locationList.addAll(locationNameList)
                    }
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    log("get Manual Location fail")
                    requireContext().captureFailureEvent(
                        Events.GET_LOCATIONS_FAILED, HashMap<String, Any?>(),
                        it.errorMessage!!
                    )

                    Utilities.hideProgressDialog()
                    handleApiError(it) { getLocations() }
                }
            }
        })
    }

    fun getLocationData(){
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            try {
                locationRequest = LocationRequest().apply {
                    interval = TimeUnit.SECONDS.toMillis(0)
                    fastestInterval = TimeUnit.SECONDS.toMillis(0)
                    maxWaitTime = TimeUnit.MINUTES.toMillis(1)
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                }
                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        super.onLocationResult(locationResult)
                        locationResult?.lastLocation?.let {
                           // currentLocation = locationByGps
                            currentLat = it?.latitude
                            currentLat = it?.longitude
                        }
                    }
                }

                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            currentLat = location.latitude
                            currentLong = location.longitude
                        }
                    }
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val addresses: List<Address> =
                    geocoder.getFromLocation(currentLat!!, currentLong!!, 1)
                val postalCode = addresses[0].postalCode
                val cityName = addresses[0].locality
                val countryName = addresses[0].countryName
                location_data.put("city", cityName)
                location_data.put("country", countryName)
                location_data.put("latitude", currentLat)
                location_data.put("longitude", currentLong)
                location_data.put("postalCode", postalCode)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }



    // calculate distance bw lat lon
    fun getDistanceFromLatLon(lat1: Double, lon1: Double, type: String) {
        val selected = getSelectedItem()?.coordinates
        var R = 6371 // Radius of the earth in km
        var dLat = deg2rad(selected?.latitude!!?.minus(lat1))  // deg2rad below
        var dLon = deg2rad(selected.longitude - lon1);
        var a = sin(dLat / 2) * sin(dLat / 2) +
                cos(deg2rad(lat1)) * cos(deg2rad(selected?.latitude)) *
                sin(dLon / 2) * sin(dLon / 2)
        var c = 2 * atan2(sqrt(a), sqrt(1 - a));
        var d = R * c * 1000 // Distance in m
        val s = ""
        Toast.makeText(requireContext(), "$lat1   $lon1  $d",Toast.LENGTH_SHORT).show()
        if (d > getSelectedItem()?.thresholdDistanceInMeters!!) {
            InvalidLocationDialog().show(
                requireActivity().supportFragmentManager,
                "invalidLocationDialog"
            )
        } else {
            if (type == "checkin")
                ShootSiteDialog().show(requireActivity().supportFragmentManager, "ShootSiteDialog")
            else {
                viewModel.type = "checkout"
                viewModel.fileUrl = ""
                clockInOut()
            }

        }
    }

    fun deg2rad(deg: Double): Double {
        return deg * (Math.PI / 180)
    }


    private fun setCheckIn(hideClockOut: Boolean) {
        viewModel.type = "checkin"
        binding.llAttendance.setOnClickListener {
            when (binding.ivDropDown.rotation) {
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

        if (hideClockOut) {
            binding.apply {
                cvClockIn.visibility = View.VISIBLE
                cvClockOut.visibility = View.GONE
            }

            setLastSession()
        }

        viewModel.isStartAttendance.observe(viewLifecycleOwner, {
            if (it) {
                viewModel.isStartAttendance.value = false
                dispatchTakePictureIntent()
            }
        })
    }

    private fun setLastSession() {
        val millis = Utilities.getLong(requireContext(), AppConstants.SHOOTS_SESSION)

        binding.tvSession.text = "Your last session was " + millisecondsToHours(millis)
    }

    private fun dispatchTakePictureIntent() {
        try {
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
                            "${BuildConfig.APPLICATION_ID}.fileprovider",
                            it
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        takePictureIntent.putExtra(
                            "android.intent.extras.CAMERA_FACING",
                            android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT
                        )
                        takePictureIntent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
                        takePictureIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                        //resultLauncher.launch(takePictureIntent)
                    }
                }
            }
        } catch (e: Exception) {
            val s = ""
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.resultCode = resultCode
        val s = ""
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Utilities.apply {
                savePrefrence(requireContext(), AppConstants.SITE_IMAGE_PATH, currentPhotoPath)
                savePrefrence(
                    requireContext(),
                    AppConstants.SITE_CITY_NAME,
                    getSelectedItem()?.locationName
                )
                saveBool(requireContext(), AppConstants.CLOCKED_IN, true)
                saveLong(requireContext(), AppConstants.CLOCKED_IN_TIME, System.currentTimeMillis())
            }

            viewModel.siteImagePath = currentPhotoPath
            setCheckOut(true)
        } else {
            val s = ""
        }
    }



    fun getSelectedItem(): LocationsRes.Data?
    {
        val locations = (viewModel.locationsResponse.value as Resource.Success).value.data

        return if (viewModel.type == "checkin"){
            locations.firstOrNull {
                it.locationName == binding.spSelectLocation.selectedItem.toString()
            }
        }else{
            locations.firstOrNull {
                it.locationName == binding.spLocationOut.selectedItem.toString()
            }
        }
    }

    val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val s = ""
            if (result.resultCode == RESULT_OK) {
                Utilities.apply {
                    savePrefrence(requireContext(), AppConstants.SITE_IMAGE_PATH, currentPhotoPath)
                    savePrefrence(
                        requireContext(),
                        AppConstants.SITE_CITY_NAME,
                        location_data.getString("city")
                    )
                    saveBool(requireContext(), AppConstants.CLOCKED_IN, true)
                    saveLong(
                        requireContext(),
                        AppConstants.CLOCKED_IN_TIME,
                        System.currentTimeMillis()
                    )
                }

                viewModel.siteImagePath = currentPhotoPath
                setCheckOut(true)
            } else {
                val s = ""
            }

        }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File =
            requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

        return File.createTempFile(
            Utilities.getPreference(
                requireContext(),
                AppConstants.TOKEN_ID
            ) + "_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun setCheckOut(clockIn: Boolean) {
        val time = System.currentTimeMillis() - Utilities.getLong(
            requireContext(),
            AppConstants.CLOCKED_IN_TIME
        )

        isActive = true
        upDateTimer(time)

        binding.apply {
            cvClockIn.visibility = View.GONE
            cvClockOut.visibility = View.VISIBLE
            tvCityName.text =
                " at " + Utilities.getPreference(requireContext(), AppConstants.SITE_CITY_NAME)
            ivDropDown.rotation = 90f
            tvClockedTime.text = getString(R.string.clocked_in_for) + " " + millisecondsToTime(time)
            llAttendance.setOnClickListener(null)
        }

        Glide.with(requireContext())
            .load(viewModel.siteImagePath)
            .into(binding.ivSiteImage)

        if (clockIn) {
            getGcpUrl()
        }
    }

    private fun upDateTimer(time: Long) {
        if (isActive) {
            binding.tvClockedTime.text =
                getString(R.string.clocked_in_for) + " " + millisecondsToTime(time)

            Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
                override fun run() {
                    upDateTimer(time.plus(1000))
                }
            }, 1000)
        }
    }

    private fun observeUrlResponse() {
        viewModel.gcpUrlResponse.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    //upload to gcp
                    viewModel.fileUrl = it.value.data.fileUrl
                    uploadImageToGcpUrl(
                        viewModel.siteImagePath,
                        it.value.data.presignedUrl,
                        it.value.data.fileUrl
                    )
                    viewModel._gcpUrlResponse.value = null
                }

                is Resource.Failure -> {
                    binding.progressBar.visibility = View.GONE
                    handleApiError(it) { getGcpUrl() }
                }
            }
        })
    }

    private fun uploadImageToGcpUrl(path: String, preSignedUrl: String, fileUrl: String) {
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
                Log.d("VideoUploader", "onResponse: " + response.code())
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    //set clock in
                    clockInOut()
                } else {
                    //retry gcp upload
                    showErrorSnackBar(path, preSignedUrl, fileUrl)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                //retry gcp upload
                showErrorSnackBar(path, preSignedUrl, fileUrl)
            }

        })
    }

    private fun observeClockInOut() {
        viewModel.checkInOutRes.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    if (viewModel.type == "checkin") {
                        Toast.makeText(
                            requireContext(),
                            "Clocked in successfully...",
                            Toast.LENGTH_LONG
                        ).show()
                        isActive = true
                        upDateTimer(
                            System.currentTimeMillis() - Utilities.getLong(
                                requireContext(),
                                AppConstants.CLOCKED_IN_TIME
                            )
                        )
                    } else {

                        val tempList = ArrayList<String>()
                        tempList.addAll(locationList)

                        locationList.apply {
                            clear()
                            addAll(tempList)
                        }
                        spLocationAdapter.notifyDataSetChanged()

                        Toast.makeText(
                            requireContext(),
                            "Clocked out successfully...",
                            Toast.LENGTH_LONG
                        ).show()
                        //save session time
                        Utilities.apply {
                            saveLong(
                                requireContext(),
                                AppConstants.SHOOTS_SESSION,
                                System.currentTimeMillis() - Utilities.getLong(
                                    requireContext(),
                                    AppConstants.CLOCKED_IN_TIME
                                )
                            )
                            saveBool(requireContext(), AppConstants.CLOCKED_IN, false)
                        }

                        setCheckIn(true)
                    }

                    viewModel._checkInOutRes.value = null
                }

                is Resource.Failure -> {
                    binding.progressBar.visibility = View.GONE
                    handleApiError(it) { clockInOut() }
                }
            }
        })
    }

    private fun clockInOut() {
        binding.progressBar.visibility = View.VISIBLE

        viewModel.captureCheckInOut(
            viewModel.type,
            location_data,
            getSelectedItem()!!?.locationId,
            viewModel.fileUrl
        )
    }

    private fun showErrorSnackBar(path: String, preSignedUrl: String, fileUrl: String) {
        snackbar = Snackbar.make(
            binding.root,
            "Failed to upload image",
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction("Retry") {
                uploadImageToGcpUrl(path, preSignedUrl, fileUrl)
            }
            .setActionTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.primary
                )
            )

        snackbar?.show()
    }

    private fun getGcpUrl() {
        binding.progressBar.visibility = View.VISIBLE
        viewModel.getGCPUrl(File(viewModel.siteImagePath).name)
    }

    private fun getLocations() {
        Utilities.showProgressDialog(requireContext())
        viewModel.getLocations()
    }


    override fun onPause() {
        Log.e("DEBUG", "OnPause of loginFragment")
        super.onPause()
        val removeTask = fusedLocationClient.removeLocationUpdates(locationCallback)
        removeTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Location Callback removed.")
            } else {
                Log.d(TAG, "Failed to remove Location Callback.")
            }
        }
    }



    override fun onResume() {
        super.onResume()

        if (Utilities.getBool(requireContext(), AppConstants.CLOCKED_IN)) {
            isActive = true
            upDateTimer(
                System.currentTimeMillis() - Utilities.getLong(
                    requireContext(),
                    AppConstants.CLOCKED_IN_TIME
                )
            )
        }
    }

    override fun onStop() {
        super.onStop()
        isActive = false
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
        Utilities.savePrefrence(requireContext(), AppConstants.LOCALE, locale)

        val locale = Locale(locale)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun statusSwitch() {
        binding.switchProjectName.isChecked =
            Utilities.getPreference(requireContext(), AppConstants.STATUS_PROJECT_NAME)
                .toString() == "true"

        binding.switchProjectName.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Utilities.savePrefrence(requireContext(), AppConstants.STATUS_PROJECT_NAME, "true")
            } else Utilities.savePrefrence(
                requireContext(),
                AppConstants.STATUS_PROJECT_NAME,
                "false"
            )
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
        Manifest.permission.ACCESS_FINE_LOCATION
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
                RequiredPermissionDialog().show(
                    requireActivity().supportFragmentManager,
                    "RequiredPermissionDialog"
                )
            }

        }

    open fun onPermissionGranted() {
        if (getSelectedItem() == null) {
            Toast.makeText(requireContext(), "Please Select Location", Toast.LENGTH_LONG).show()
        } else {
            getLocationData()
            getDistanceFromLatLon(currentLat!!, currentLong!!, "checkin")
        }
    }

    override fun getViewModel() = DashboardViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentPreferenceBinding.inflate(inflater, container, false)

    private fun millisecondsToHours(millis: Long): String? {
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

        return if (hours > 0) "${getTwoDigit(hours.toString())}:${getTwoDigit(minutes.toString())}:$secs" else "${
            getTwoDigit(
                minutes.toString()
            )
        }:$secs"
    }

    private fun getTwoDigit(value: String): String {
        return if (value.length >= 2) {
            value.substring(0, 2)
        } else {
            "0$value"
        }
    }
}

