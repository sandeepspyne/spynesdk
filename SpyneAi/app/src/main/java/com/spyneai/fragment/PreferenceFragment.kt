package com.spyneai.fragment

import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.isVisible
import com.spyneai.R
import com.spyneai.Shoot_Site_Dialog
import com.spyneai.base.BaseFragment
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.databinding.FragmentPreferenceBinding
import com.spyneai.logout.LogoutDialog
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shoot.ui.dialogs.ThreeSixtyInteriorHintDialog
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.fragment_preference.*
import java.util.*


class PreferenceFragment : BaseFragment<DashboardViewModel, FragmentPreferenceBinding>()  {

    val REQUEST_IMAGE_CAPTURE = 1

//    var languageList = arrayOf("English","Germany","Italy")
    var languageList= arrayListOf<String>()
    lateinit var spLanguageAdapter: ArrayAdapter<String>


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

        viewModel.isStartAttendance.observe(viewLifecycleOwner, {
            if (it){
                binding.btClockIn.visibility=View.GONE
                binding.btClockOut.visibility=View.VISIBLE
                viewModel.isStartAttendance.value=false
                try {
                    startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_IMAGE_CAPTURE)
                } catch (e: ActivityNotFoundException) {
                    // display error state to the user
                }

            }
        })







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

        if(Utilities.getPreference(requireContext(), AppConstants.STATUS_PROJECT_NAME).toString() =="true"){
            binding.switchProjectName.isChecked=true

        } else binding.switchProjectName.isChecked=false


        binding.switchProjectName.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Utilities.savePrefrence(requireContext(), AppConstants.STATUS_PROJECT_NAME, "true")
            }
            else Utilities.savePrefrence(requireContext(), AppConstants.STATUS_PROJECT_NAME, "false")
        }
    }





    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            binding.ivSiteImage.setImageBitmap(imageBitmap)
        }
    }





    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.llLogout.setOnClickListener {
            LogoutDialog().show(requireActivity().supportFragmentManager,"LogoutDialog")

        }
        binding.btClockIn.setOnClickListener {
            Shoot_Site_Dialog().show(requireActivity().supportFragmentManager,"Shoot_site_dialog")

        }

    }







    override fun getViewModel() = DashboardViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentPreferenceBinding.inflate(inflater, container, false)
}

