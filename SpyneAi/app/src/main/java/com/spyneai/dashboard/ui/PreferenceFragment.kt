package com.spyneai.dashboard.ui

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Address
import android.location.Geocoder
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
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.dashboard.data.model.LocationsRes
import com.spyneai.databinding.FragmentPreferenceBinding
import com.spyneai.logout.LogoutDialog
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.ui.dialogs.RequiredPermissionDialog
import com.spyneai.shoot.utils.log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
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
import android.content.Context.LOCATION_SERVICE
import android.content.DialogInterface
import android.content.IntentSender
import android.provider.Settings
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.spyneai.R
import com.spyneai.shoot.data.ShootRepository
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.Toast

class PreferenceFragment : BaseFragment<DashboardViewModel, FragmentPreferenceBinding>() {
    val REQUEST_IMAGE_CAPTURE = 1
    var languageList = arrayListOf<String>()
    var locationList: ArrayList<String> = ArrayList()
    lateinit var spLanguageAdapter: ArrayAdapter<String>
    lateinit var spLocationAdapter: ArrayAdapter<String>
    lateinit var currentPhotoPath: String
    val location_data = JSONObject()
    var snackbar: Snackbar? = null
    var isActive = false
    var currentLat: Double? = 0.0
    var currentLong: Double? = 0.0
    var locationManager: LocationManager? = null
    val LOCATION_SETTING_REQUEST = 999


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getLocations()
        observeLocation()

        if (getString(R.string.app_name) == AppConstants.SPYNE_AI) {
            val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(30, 30, 30, 260)
            params.gravity = Gravity.CENTER_HORIZONTAL;
            binding.llLogout.layoutParams = params
        }

        if (Utilities.getPreference(
                requireContext(),
                AppConstants.ENTERPRISE_ID
            ) == AppConstants.SPYNE_ENTERPRISE_ID
        ) {
            binding.llAttendance.visibility = View.GONE
        } else {
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
                locationManager =
                    requireContext().getSystemService(LOCATION_SERVICE) as LocationManager
                if (!locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    onGPS()
                } else {
                    if (allPermissionsGranted()) {
                        onPermissionGranted()
                    } else {
                        permissionRequest.launch(permissions.toTypedArray())
                    }

                    if (location_data.has("latitude")) {
                        requireContext().captureEvent(
                            Events.GET_LOCATION_SUCCESS_CHECKIN,
                            HashMap<String, Any?>().apply {
                                put(
                                    "user_id",
                                    Utilities.getPreference(requireContext(), AppConstants.TOKEN_ID)
                                )
                                put("location_data", location_data)
                            })

                    } else {
                        requireContext().captureEvent(
                            Events.GET_LOCATION_FAIL_CHECKIN,
                            HashMap<String, Any?>().apply {
                                put(
                                    "user_id",
                                    Utilities.getPreference(requireContext(), AppConstants.TOKEN_ID)
                                )
                                put("location_data", location_data)
                                put("last_reboot_since", SystemClock.elapsedRealtime() / 60000)
                            })
                    }
                }
            }

            binding.btnClockOut.setOnClickListener {
                locationManager =
                    requireContext().getSystemService(LOCATION_SERVICE) as LocationManager
                if (!locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    onGPS()
                } else {

                    getLocationData("checkout")
                    viewModel.type = "checkout"
                    if (location_data.has("latitude")) {
                        requireContext().captureEvent(
                            Events.GET_LOCATION_SUCCESS_CHECKOUT,
                            HashMap<String, Any?>().apply {
                                put(
                                    "user_id",
                                    Utilities.getPreference(requireContext(), AppConstants.TOKEN_ID)
                                )
                                put("location_data", location_data)
                            })

                    } else {
                        requireContext().captureEvent(
                            Events.GET_LOCATION_FAIL_CHECKOUT,
                            HashMap<String, Any?>().apply {
                                put(
                                    "user_id",
                                    Utilities.getPreference(requireContext(), AppConstants.TOKEN_ID)
                                )
                                put("location_data", location_data)
                                put("last_reboot_since", SystemClock.elapsedRealtime() / 60000)
                            })

                    }
                }
            }

//            if (Utilities.getBool(requireContext(), AppConstants.CLOCKED_IN)) {
//                viewModel.siteImagePath =
//                    Utilities.getPreference(requireContext(), AppConstants.SITE_IMAGE_PATH)
//                        .toString()
//                setCheckOut(false)
//            } else {
//                setCheckIn(false)
//            }

            observeUrlResponse()
            observeClockInOut()
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

    private fun onGPS() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setMessage("To Continue, Turn on device location or GPS, which uses Google's location service.")
            .setCancelable(false).setPositiveButton("Turn On",
                DialogInterface.OnClickListener { dialog, which ->
                    startActivityForResult(
                        Intent(
                            Settings.ACTION_LOCATION_SOURCE_SETTINGS
                        ), LOCATION_SETTING_REQUEST
                    )
                })
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
        alertDialog.setCancelable(false)
    }

    private fun observeLocation() {
        viewModel.locationsResponse.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()

                    getAttendanceStatus()
                    observeStatusResponse()
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


    fun getLocationData(status: String) {
        val locationRequest = LocationRequest()
        locationRequest.interval = 500
        locationRequest.fastestInterval = 500
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        Utilities.showProgressDialog(requireContext())

        LocationServices.getFusedLocationProviderClient(requireContext())
            .requestLocationUpdates(locationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)

                    if (locationResult != null && locationResult.locations.size > 0) {
                        LocationServices.getFusedLocationProviderClient(requireContext().applicationContext)
                            .removeLocationUpdates(this)
                        val latestlocIndex = locationResult.locations.size - 1
                        currentLat = locationResult.locations[latestlocIndex].latitude
                        currentLong = locationResult.locations[latestlocIndex].longitude
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
                        Utilities.hideProgressDialog()

                        getDistanceFromLatLon(currentLat!!, currentLong!!, status)

                    } else {
                        val s = ""
                    }
                }
            }, Looper.getMainLooper())
    }


    // calculate distance bw lat lon
    fun getDistanceFromLatLon(lat1: Double, lon1: Double, type: String) {
        val selected = getSelectedItem()?.coordinates
        var R = 6371 // Radius of the earth in km
        var dLat = deg2rad(selected?.latitude!!?.minus(lat1))  // deg2rad below
        var dLon = deg2rad(selected.longitude - lon1)
        var a = sin(dLat / 2) * sin(dLat / 2) +
                cos(deg2rad(lat1)) * cos(deg2rad(selected?.latitude)) *
                sin(dLon / 2) * sin(dLon / 2)
        var c = 2 * atan2(sqrt(a), sqrt(1 - a))
        var d = R * c * 1000 // Distance in m

        if (d > getSelectedItem()?.thresholdDistanceInMeters!!) {
            if (type == "checkin")
                ShootSiteDialog().show(requireActivity().supportFragmentManager, "ShootSiteDialog")
            else {
                viewModel.type = "checkout"
                viewModel.fileUrl = ""
                checkInOut()
            }
//            if (lat1 == 0.0 || lon1 == 0.0) {
//                Toast.makeText(
//                    requireContext(),
//                    "Unable to detect your location, please try after some time!",
//                    Toast.LENGTH_LONG
//                ).show()
//            } else {
//                InvalidLocationDialog().show(
//                    requireActivity().supportFragmentManager,
//                    "invalidLocationDialog"
//                )
//            }
        } else {
            if (type == "checkin")
                ShootSiteDialog().show(requireActivity().supportFragmentManager, "ShootSiteDialog")
            else {
                viewModel.type = "checkout"
                viewModel.fileUrl = ""
                checkInOut()
            }
        }
    }

    private fun deg2rad(deg: Double): Double {
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
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> when (resultCode) {
                RESULT_OK -> {
                    GlobalScope.launch {
                        withContext(Dispatchers.Default) {
                            val file: File = File(currentPhotoPath)
                            val length = file.length()
                            val before: Long = length / 1024
                            val compressedImageFile = Compressor.compress(requireContext(), file)
                            val length2 = compressedImageFile.length()
                            val after: Long = length2 / 1024
                            Utilities.apply {
                                savePrefrence(
                                    requireContext(),
                                    AppConstants.SITE_IMAGE_PATH,
                                    compressedImageFile.path
                                )
                                savePrefrence(
                                    requireContext(),
                                    AppConstants.SITE_CITY_NAME,
                                    getSelectedItem()?.locationName
                                )
                                saveBool(requireContext(), AppConstants.CLOCKED_IN, true)
                                saveLong(
                                    requireContext(),
                                    AppConstants.CLOCKED_IN_TIME,
                                    System.currentTimeMillis()
                                )
                            }
                            viewModel.siteImagePath = compressedImageFile.path
                        }
                        withContext(Dispatchers.Main) {
                            setCheckOut(true)
                        }
                    }
                }
            }

            LOCATION_SETTING_REQUEST -> when (resultCode) {
                0 -> {
                    if (!locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        onGPS()
                    }
                }
                else -> {
                }
            }

        }

    }


    fun getSelectedItem(): LocationsRes.Data? {
        val locations = (viewModel.locationsResponse.value as Resource.Success).value.data

        return if (viewModel.type == "checkin") {
            locations.firstOrNull {
                it.locationName == binding.spSelectLocation.selectedItem.toString()
            }
        } else {
            locations.firstOrNull {
                it.locationName == binding.spLocationOut.selectedItem.toString()
            }
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

        isActive = false
        upDateTimer(time)

        binding.apply {
            cvClockIn.visibility = View.GONE
            cvClockOut.visibility = View.VISIBLE
            tvCityName.text =
                " at " + getLocationName(
                    Utilities.getPreference(requireContext(), AppConstants.SITE_CITY_NAME)
                        .toString()
                )
//            tvCityName.text =
//                " at " + Utilities.getPreference(requireContext(), AppConstants.SITE_CITY_NAME)
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

    fun getLocationName(locationId: String): String? {
        var name: String? = null

        viewModel.locationsResponse.observe(viewLifecycleOwner, {
            if (it is Resource.Success) {
                it.value.data.forEach {
                    if (it.locationId == locationId) {
                        name = it.locationName
                    }
                }
            }
        })

        return name
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
                    viewModel.preSignedUrl = it.value.data.presignedUrl
                    GlobalScope.launch {
                        withContext(Dispatchers.Main) {
                            imageUpload(
                                path = viewModel.siteImagePath,
                                preSignedUrl = it.value.data.presignedUrl,
                                fileUrl = it.value.data.fileUrl
                            )
                        }
                    }
                    viewModel._gcpUrlResponse.value = null
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) { getGcpUrl() }
                }
            }
        })
    }

    private suspend fun imageUpload(path: String, preSignedUrl: String, fileUrl: String) {
        val requestFile =
            File(path).asRequestBody("text/x-markdown; charset=utf-8".toMediaTypeOrNull())
        val shootRepository = ShootRepository()
        val uploadResponse = shootRepository.uploadImageToGcp(preSignedUrl, requestFile)
        when (uploadResponse) {
            is Resource.Failure -> {
                Utilities.hideProgressDialog()
                requireContext().captureEvent(
                    Events.SITEIMAGE_UPLOADED_FAIL,
                    HashMap<String, Any?>().apply {
                        put(
                            "user_id",
                            Utilities.getPreference(requireContext(), AppConstants.TOKEN_ID)
                        )
                        put("throwable", uploadResponse.throwable)
                    })
                showErrorSnackBar(path, preSignedUrl, fileUrl)
            }
            is Resource.Success -> {
                Utilities.hideProgressDialog()
                checkInOut()
                requireContext().captureEvent(
                    Events.SITEIMAGE_UPLOADED,
                    HashMap<String, Any?>().apply {
                        put(
                            "user_id",
                            Utilities.getPreference(requireContext(), AppConstants.TOKEN_ID)
                        )
                        put("fileUrl", viewModel.fileUrl)
                        put("response", uploadResponse)
                    })
            }
        }
    }

    private fun observeClockInOut() {
        viewModel.checkInOutRes.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    var eventName = ""
                    Utilities.hideProgressDialog()
                    if (it.value.data.checkout_time.isNullOrEmpty()) {
                        eventName = Events.CHECKIN_SUCCESS
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
                        eventName = Events.CHECKOUT_SUCCESS
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
                    requireContext()
                        .captureEvent(
                            eventName,
                            HashMap<String, Any?>().apply {
                                put(
                                    "email_id",
                                    Utilities.getPreference(requireContext(), AppConstants.EMAIL_ID)
                                )
                                put(
                                    "user_id",
                                    Utilities.getPreference(requireContext(), AppConstants.TOKEN_ID)
                                )
                                put("response", it.value)
                            })
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    val eventName =
                        if (viewModel.type == "checkin") Events.CHECKIN_FAILURE else Events.CHECKOUT_FAILURE
                    requireContext()
                        .captureEvent(
                            eventName,
                            HashMap<String, Any?>().apply {
                                put(
                                    "email_id",
                                    Utilities.getPreference(requireContext(), AppConstants.EMAIL_ID)
                                )
                                put(
                                    "user_id",
                                    Utilities.getPreference(requireContext(), AppConstants.TOKEN_ID)
                                )
                                put("response", it.errorMessage)
                            })
                    handleApiError(it) { checkInOut() }
                }
            }
        })
    }

    private fun checkInOut() {
        Utilities.showProgressDialog(requireContext())

        viewModel.captureCheckInOut(
            location_data,
            getSelectedItem()!!?.locationId,
            viewModel.fileUrl
        )
        val eventName =
            if (viewModel.type == "checkin") Events.CHECKIN_CALL_INTIATED else Events.CHECKOUT_CALL_INTIATED

        requireContext()
            .captureEvent(
                eventName,
                HashMap<String, Any?>().apply {
                    put("type", viewModel.type)
                    put(
                        "email_id",
                        Utilities.getPreference(requireContext(), AppConstants.EMAIL_ID)
                    )
                    put("user_id", Utilities.getPreference(requireContext(), AppConstants.TOKEN_ID))
                    put("location_data", location_data.toString())
                    put("location_id", getSelectedItem()!!?.locationId)
                    put("image_url", viewModel.fileUrl)
                    put("internet_connection", requireContext().isInternetActive())
                    put("gps", locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    put("battery_level", requireContext().getBatteryLevel())
                    put("last_reboot_since", SystemClock.elapsedRealtime() / 60000)
                    put("is_power_save_mode", requireContext().getPowerSaveMode())
                }
            )
    }

    private fun showErrorSnackBar(path: String, preSignedUrl: String, fileUrl: String) {
        snackbar = Snackbar.make(
            binding.root,
            "Failed to upload image",
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction("Retry") {
                GlobalScope.launch {
                    withContext(Dispatchers.Main) {
                        imageUpload(
                            path = viewModel.siteImagePath,
                            preSignedUrl = viewModel.preSignedUrl,
                            fileUrl = viewModel.fileUrl
                        )
                    }
                }
                requireContext().captureEvent(
                    Events.SITEIMAGE_UPLOAD_RETRY,
                    HashMap<String, Any?>().apply {
                        put(
                            "user_id",
                            Utilities.getPreference(requireContext(), AppConstants.TOKEN_ID)
                        )
                        put("fileUrl", viewModel.fileUrl)
                    })
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
        Utilities.showProgressDialog(requireContext())
        viewModel.getGCPUrl(File(viewModel.siteImagePath).name)
    }

    private fun getLocations() {
        Utilities.showProgressDialog(requireContext())
        viewModel.getLocations()
    }


    override fun onPause() {
        Log.e("DEBUG", "OnPause of loginFragment")
        super.onPause()
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
            getLocationData("checkin")
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

    private fun getAttendanceStatus() {
        Utilities.showProgressDialog(requireContext())
        viewModel.getAttendanceStatus()
    }


    private fun observeStatusResponse() {
        viewModel.attendanceStatusRes.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {

                    Utilities.hideProgressDialog()

                    when {
                        it.value.message == "Data not found" -> {
                            Utilities.saveBool(requireContext(), AppConstants.CLOCKED_IN, false)
                        }

                        it.value.data.checkin_time != null && it.value.data.location_out_id == null -> {
                            //user clocked in show checkout ui
                            val sdf =
                                SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH)
                            val checkin_time: Long = sdf.parse(it.value.data.checkin_time).time

                            Utilities.apply {
                                savePrefrence(
                                    requireContext(),
                                    AppConstants.SITE_IMAGE_PATH,
                                    it.value.data.checkin_image
                                )
                                savePrefrence(
                                    requireContext(),
                                    AppConstants.SITE_CITY_NAME,
                                    it.value.data.location_in_id
                                )
                                saveBool(requireContext(), AppConstants.CLOCKED_IN, true)
                                saveLong(
                                    requireContext(),
                                    AppConstants.CLOCKED_IN_TIME,
                                    checkin_time
                                )
                            }

                            viewModel.siteImagePath = it.value.data.checkin_image
                            setCheckOut(false)


//                            if (it.value.data.user_id == Utilities.getPreference(
//                                    requireContext(),
//                                    AppConstants.TOKEN_ID
//                                ) &&
//                                it.value.data.enterprise_id == Utilities.getPreference(
//                                    requireContext(),
//                                    AppConstants.ENTERPRISE_ID
//                                ) &&
//                                it.value.data.location_out_id.isNullOrEmpty()
//                            ) {
//
//                            } else {
//                                Utilities.saveLong(
//                                    requireContext(),
//                                    AppConstants.SHOOTS_SESSION,
//                                    checkout_time - checkin_time
//                                )
//
//                            }
                        }
                        else -> {
                            val sdf =
                                SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH)
                            val checkin_time: Long = sdf.parse(it.value.data.checkin_time).time
                            val checkout_time: Long = sdf.parse(it.value.data.checkout_time).time

                            Utilities.saveLong(
                                requireContext(),
                                AppConstants.SHOOTS_SESSION,
                                checkout_time - checkin_time
                            )
                            setCheckIn(true)
                            //user clocked out show check in ui
                        }
                    }

                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) { getAttendanceStatus() }
                }
            }
        })
    }
}

