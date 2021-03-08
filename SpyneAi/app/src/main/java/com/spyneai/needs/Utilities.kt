package com.spyneai.needs

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.preference.PreferenceManager
import android.util.Patterns
import android.view.Gravity
import android.view.Window
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.spyneai.R
import com.spyneai.model.carreplace.CarBackgroundsResponse
import com.spyneai.model.channel.BackgroundsResponse
import com.spyneai.model.channel.ChannelsResponse
import com.spyneai.model.skumap.UpdateSkuResponse
import kotlinx.android.synthetic.main.dialog_progress.*
import kotlinx.android.synthetic.main.dialog_progress_preview.*
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList


object Utilities {


    private var dialog: Dialog? = null

    //Validating email id
    fun isValidEmail(email: String?): Boolean {
        val pattern = Patterns.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
    }

    //Validation of mobile number
    fun isValidMobileNumber(s: String): Boolean {

        // The given argument to compile() method
        // is regular expression. With the help of
        // regular expression we can validate mobile
        // number.
        // 1) Then contains 6 or 7 or 8 or 9.
        // 2) Then contains 9 digits
        // Pattern p = Pattern.compile("(0/91)?[6-9][0-9]{9}");
        val p = Pattern.compile("^[6-9]\\d{9}$")

        // Pattern class contains matcher() method
        // to find matching between given number
        // and regular expression
        val m = p.matcher(s)
        return m.find() && m.group() == s
    }

    fun checkInternetConnection(context: Context): Boolean {
        val mgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var netInfo: NetworkInfo? = null
        if (mgr != null) {
            netInfo = mgr.activeNetworkInfo
        }
        return netInfo?.isConnected ?: false
    }

    public fun savePrefrence(context: Context, key: String?, value: String?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    public fun getPreference(context: Context?, key: String?): String? {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(key, "")
    }

    fun showProgressDialog(context: Context?) {
        dialog = Dialog(context!!)
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setContentView(R.layout.dialog_progress)
        dialog!!.setCancelable(false)
        Glide.with(context).load(R.raw.logo).into(dialog!!.ivLoaders);
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog!!.show()
    }


    fun showProgressDialogPreview(context: Context?) {
        dialog = Dialog(context!!)

        val window: Window = dialog!!.getWindow()!!
        val wlp = window.attributes
        wlp.gravity = Gravity.TOP;
        window.setAttributes(wlp);
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setContentView(R.layout.dialog_progress_preview)
        dialog!!.setCancelable(false)
        Glide.with(context).load(R.raw.loaders).into(dialog!!.ivLoaderPreview);
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog!!.show()
    }

    fun hideProgressDialog() {
        if (dialog != null) dialog!!.dismiss()
    }

    //Check for network connection
    private fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!.isConnected
    }

    fun getcurdate(): String? {
        return SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
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

    fun getList(context: Context, key: String): List<ChannelsResponse>? {
        sharedPreferences = context!!.getSharedPreferences(
            AppConstants.MY_LIST,
            Context.MODE_PRIVATE
        )
        var arrayItems = ArrayList<ChannelsResponse>()
        val serializedObject: String? = sharedPreferences.getString(key, null)
        if (serializedObject != null) {
            val gson = Gson()
            val type: Type = object : TypeToken<List<ChannelsResponse?>?>() {}.type
            arrayItems = gson.fromJson<ArrayList<ChannelsResponse>>(serializedObject, type)
        }

        return arrayItems
    }


    fun getListBackgrounds(context: Context, key: String): List<BackgroundsResponse>? {
        sharedPreferences = context!!.getSharedPreferences(
            AppConstants.MY_LIST,
            Context.MODE_PRIVATE
        )

        var arrayItems = ArrayList<BackgroundsResponse>()
        val serializedObject: String? = sharedPreferences.getString(key, null)
        if (serializedObject != null) {
            val gson = Gson()
            val type: Type = object : TypeToken<List<BackgroundsResponse?>?>() {}.type
            arrayItems = gson.fromJson<ArrayList<BackgroundsResponse>>(serializedObject, type)
        }

        return arrayItems
    }

    fun getListBackgroundsCar(context: Context, key: String): List<CarBackgroundsResponse>? {
        sharedPreferences = context!!.getSharedPreferences(
            AppConstants.MY_LIST,
            Context.MODE_PRIVATE
        )

        var arrayItems = ArrayList<CarBackgroundsResponse>()
        val serializedObject: String? = sharedPreferences.getString(key, null)
        if (serializedObject != null) {
            val gson = Gson()
            val type: Type = object : TypeToken<List<CarBackgroundsResponse?>?>() {}.type
            arrayItems = gson.fromJson<ArrayList<CarBackgroundsResponse>>(serializedObject, type)
        }

        return arrayItems
    }

    fun getGifsList(context: Context, key: String): List<String>? {
        sharedPreferences = context!!.getSharedPreferences(
            AppConstants.MY_LIST,
            Context.MODE_PRIVATE
        )

        var arrayItems = ArrayList<String>()
        val serializedObject: String? = sharedPreferences.getString(key, null)
        if (serializedObject != null) {
            val gson = Gson()
            val type: Type = object : TypeToken<List<String?>?>() {}.type
            arrayItems = gson.fromJson<ArrayList<String>>(serializedObject, type)
        }

        return arrayItems
    }


    fun getFrameLists(context: Context, key: String): List<UpdateSkuResponse>? {
        sharedPreferences = context!!.getSharedPreferences(
            AppConstants.MY_LISTS,
            Context.MODE_PRIVATE
        )

        var arrayItems = ArrayList<UpdateSkuResponse>()
        val serializedObject: String? = sharedPreferences.getString(key, null)
        if (serializedObject != null) {
            val gson = Gson()
            val type: Type = object : TypeToken<List<UpdateSkuResponse?>?>() {}.type
            arrayItems = gson.fromJson<ArrayList<UpdateSkuResponse>>(serializedObject, type)
        }

        return arrayItems
    }
}