package com.spyneai.needs

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.preference.PreferenceManager
import android.util.Patterns
import android.view.LayoutInflater
import android.view.Window
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.spyneai.R
import com.spyneai.databinding.DialogProgressBinding
import com.spyneai.isValidGlideContext
import com.spyneai.shoot.utils.log
import kotlinx.android.synthetic.main.dialog_progress.*


object Utilities {


    private var dialog: Dialog? = null

    //Validating email id
    fun isValidEmail(email: String?): Boolean {
        val pattern = Patterns.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
    }


    fun savePrefrence(context: Context, key: String, value: String?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getPreference(context: Context?, key: String): String? {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(key, "")
    }

    fun saveBool(context: Context, key: String, value: Boolean){
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBool(context: Context?, key: String, default: Boolean = false): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getBoolean(key, default)
    }
    fun getLong(context: Context?, key: String): Long {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getLong(key,0)
    }
    fun saveLong(context: Context, key: String, value: Long){
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun showProgressDialog(context: Context) {
        log("show progress dialog")
        if (context.isValidGlideContext()) {
            dialog = Dialog(context!!)
            dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_progress,null)
            val binding = DialogProgressBinding.bind(view)
            dialog!!.setContentView(binding.root)
            dialog!!.setCancelable(false)
            if (getBool(context,AppConstants.FROM_SDK,false)){
                binding.ivAppLogo.isVisible = false
                binding.ivLoaders.isVisible = false

                binding.progressbar.isVisible = true
                binding.tvPleaseWait.isVisible = true
                dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.WHITE))
            }else {
                Glide.with(context).load(R.raw.loader).into(dialog!!.ivLoaders)
                dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
            dialog!!.show()
        }

    }

    fun hideProgressDialog() {
        if (dialog != null) dialog!!.dismiss()
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!.isConnected
    }



    fun <T> setList(context: Context?, key: String?, list: List<T>?) {
        val gson = Gson()
        val json = gson.toJson(list)
        set(context, key, json)
    }

    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor

    operator fun set(context: Context?, key: String?, value: String?) {
        sharedPreferences = context!!.getSharedPreferences(
            AppConstants.MY_LIST,
            Context.MODE_PRIVATE
        )
        editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.commit()
    }

}