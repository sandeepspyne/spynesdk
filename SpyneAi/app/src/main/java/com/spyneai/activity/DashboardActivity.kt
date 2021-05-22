package com.spyneai.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.spyneai.extras.OnboardTwoActivity
import com.spyneai.R
import com.spyneai.adapter.CategoriesDashboardAdapter
import com.spyneai.extras.BeforeAfterActivity
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClient
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.model.categories.CategoriesResponse
import com.spyneai.model.categories.Data
import com.spyneai.model.credit.FreeCreditEligblityResponse
import com.spyneai.model.shoot.CreateCollectionRequest
import com.spyneai.model.shoot.CreateCollectionResponse
import com.spyneai.model.shoot.UpdateShootCategoryRequest
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.credits.WalletActivity
import com.spyneai.loginsignup.activity.LoginActivity
import com.spyneai.loginsignup.activity.SignUpActivity
import com.synnapps.carouselview.ViewListener
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.view_custom.view.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class DashboardActivity : AppCompatActivity() {
    lateinit var appUpdateManager: AppUpdateManager
    private val MY_REQUEST_CODE: Int = 1

    lateinit var categoriesResponseList: ArrayList<Data>
    lateinit var categoriesAdapter: CategoriesDashboardAdapter
    lateinit var rv_categories: RecyclerView
    lateinit var PACKAGE_NAME: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        PACKAGE_NAME = getApplicationContext().getPackageName();

        appUpdateManager = AppUpdateManagerFactory.create(this)
        if (PACKAGE_NAME.equals("com.spyneai.debug")){
            Utilities.savePrefrence(
                this,
                AppConstants.FRAME_SHOOOTS,
                ""
            )
            Utilities.savePrefrence(
                this,
                AppConstants.SKU_ID,
                ""
            )
            setRecycler()
            if (Utilities.isNetworkAvailable(this))
                fetchCategories()
            else
                Toast.makeText(
                    this,
                    "Please check your internet connection",
                    Toast.LENGTH_SHORT
                ).show()
            freeCreditEligiblityCheck()
        } else {
            autoUpdates()
        }
    }

    private fun autoUpdates() {

        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {

                // Request the update.
                appUpdateManager.startUpdateFlowForResult(
                    // Pass the intent that is returned by 'getAppUpdateInfo()'.
                    appUpdateInfo,
                    // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                    AppUpdateType.IMMEDIATE,
                    // The current activity making the update request.
                    this,
                    // Include a request code to later monitor this update request.
                    MY_REQUEST_CODE
                )
            } else {
                Utilities.savePrefrence(
                    this,
                    AppConstants.FRAME_SHOOOTS,
                    ""
                )
                Utilities.savePrefrence(
                    this,
                    AppConstants.SKU_ID,
                    ""
                )
                setRecycler()
                if (Utilities.isNetworkAvailable(this))
                    fetchCategories()
                else
                    Toast.makeText(
                        this,
                        "Please check your internet connection",
                        Toast.LENGTH_SHORT
                    ).show()
                freeCreditEligiblityCheck()
            }
        }

    }

    private fun setRecycler() {
        Log.e("Token Mine", Utilities.getPreference(this, AppConstants.tokenId).toString())
        categoriesResponseList = ArrayList<Data>()
        categoriesAdapter = CategoriesDashboardAdapter(this, categoriesResponseList,
            object : CategoriesDashboardAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    Log.e("position cat", position.toString())
                    if (position < 3) {
                        Utilities.savePrefrence(
                            this@DashboardActivity,
                            AppConstants.CATEGORY_NAME,
                            categoriesResponseList[position].displayName
                        )
                        setShoot(categoriesResponseList, position)
                    } else
                        Toast.makeText(
                            this@DashboardActivity,
                            "Coming Soon !",
                            Toast.LENGTH_SHORT
                        ).show()
                }
            })

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        rvCategoriesDashboard.setLayoutManager(layoutManager)
        rvCategoriesDashboard.setAdapter(categoriesAdapter)

    }

    private fun fetchCategories() {
        Utilities.showProgressDialog(this)
        categoriesResponseList.clear()

        val request = RetrofitClient.buildService(APiService::class.java)
        val call = request.getCategories(Utilities.getPreference(this, AppConstants.tokenId))

        call?.enqueue(object : Callback<CategoriesResponse> {
            override fun onResponse(
                call: Call<CategoriesResponse>,
                response: Response<CategoriesResponse>
            ) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful) {
                    if (response.body()?.payload?.data?.size!! > 0) {
                        categoriesResponseList.addAll(response.body()?.payload?.data!!)
                    }

                    categoriesAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<CategoriesResponse>, t: Throwable) {
                Log.e("ok", "no way")
                Utilities.hideProgressDialog()
                Toast.makeText(
                    this@DashboardActivity,
                    "Server not responding!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun setShoot(categoriesResponseList: ArrayList<Data>, position: Int) {
        Utilities.showProgressDialog(this)
        val createCollectionRequest = CreateCollectionRequest("Spyne Shoot");

        val request = RetrofitClient.buildService(APiService::class.java)
        val call = request.createCollection(
            Utilities.getPreference(this, AppConstants.tokenId),
            createCollectionRequest
        )

        call?.enqueue(object : Callback<CreateCollectionResponse> {
            override fun onResponse(
                call: Call<CreateCollectionResponse>,
                response: Response<CreateCollectionResponse>
            ) {
                if (response.isSuccessful) {
                    Log.e("ok", response.body()?.payload?.data?.shootId.toString())
                    Utilities.savePrefrence(
                        this@DashboardActivity,
                        AppConstants.SHOOT_ID,
                        response.body()?.payload?.data?.shootId.toString()
                    )
                    setCategoryMap(response.body()?.payload?.data?.shootId.toString(), position)
                }
            }

            override fun onFailure(call: Call<CreateCollectionResponse>, t: Throwable) {
                Log.e("ok", "no way")

            }
        })
    }

    private fun setCategoryMap(shootId: String, position: Int) {

        val updateShootCategoryRequest = UpdateShootCategoryRequest(
            shootId,
            categoriesResponseList[position].catId,
            categoriesResponseList[position].displayName
        )

        val request = RetrofitClient.buildService(APiService::class.java)
        val call = request.updateShootCategory(
            Utilities.getPreference(this, AppConstants.tokenId),
            updateShootCategoryRequest
        )

        call?.enqueue(object : Callback<CreateCollectionResponse> {
            override fun onResponse(
                call: Call<CreateCollectionResponse>,
                response: Response<CreateCollectionResponse>
            ) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful) {
                    val intent = Intent(this@DashboardActivity, BeforeAfterActivity::class.java)
                    intent.putExtra(
                        AppConstants.CATEGORY_ID,
                        categoriesResponseList[position].catId
                    )
                    intent.putExtra(
                        AppConstants.CATEGORY_NAME,
                        categoriesResponseList[position].displayName
                    )
                    intent.putExtra(
                        AppConstants.IMAGE_URL,
                        categoriesResponseList[position].displayThumbnail
                    )
                    intent.putExtra(
                        AppConstants.DESCRIPTION,
                        categoriesResponseList[position].description
                    )
                    intent.putExtra(AppConstants.COLOR, categoriesResponseList[position].colorCode)
                    startActivity(intent)
                    Log.e(
                        "Category map",
                        categoriesResponseList[position].catId + " " + response.body()!!.msgInfo.msgDescription
                    )
                }
            }

            override fun onFailure(call: Call<CreateCollectionResponse>, t: Throwable) {
                Utilities.hideProgressDialog()
                Log.e("ok", "no way")

            }
        })
    }

    private fun logoutDialog(){

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)

        var dialogView = LayoutInflater.from(this).inflate(R.layout.logout_dialog, null)

        dialog.setContentView(dialogView)

        dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        val llLogout: LinearLayout = dialog.findViewById(R.id.llLogout)
        var ivClose: ImageView = dialogView.findViewById(R.id.ivClose)


        ivClose.setOnClickListener(View.OnClickListener {

            dialog.dismiss()

        })

        dialog.show()


        llLogout.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
            Utilities.savePrefrence(this, AppConstants.tokenId, "")
            Utilities.savePrefrence(this, AppConstants.SHOOT_ID, "")
            Utilities.savePrefrence(this, AppConstants.SKU_ID, "")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()

        })
    }


    private fun freeCreditEligiblityCheck() {
        val userId = RequestBody.create(
            MultipartBody.FORM,
            Utilities.getPreference(this, AppConstants.tokenId)!!
        )

        val emaiId = RequestBody.create(
            MultipartBody.FORM,
            Utilities.getPreference(this, AppConstants.EMAIL_ID)!!
        )

        val request = RetrofitClients.buildService(APiService::class.java)
        val call = request.UserFreeCreditEligiblityCheck(userId, emaiId)

        call?.enqueue(object : Callback<FreeCreditEligblityResponse> {
            override fun onResponse(
                call: Call<FreeCreditEligblityResponse>,
                response: Response<FreeCreditEligblityResponse>
            ) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful) {
                    if (response.body()?.status == 200)
                        showFreeCreditDialog(response.body()!!.message)
                } else {
                    Toast.makeText(
                        this@DashboardActivity,
                        "Server not responding!!!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<FreeCreditEligblityResponse>, t: Throwable) {
                Toast.makeText(
                    this@DashboardActivity,
                    "Server not responding!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun showFreeCreditDialog(message: String) {

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)

        var dialogView = LayoutInflater.from(this).inflate(R.layout.free_credit_dialog, null)
        var tvMessage: TextView = dialogView.findViewById(R.id.tvSkuNameDialog)
        tvMessage.text = message

        dialog.setContentView(dialogView)

        dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        val llOk: LinearLayout = dialog.findViewById(R.id.llOk)


        llOk.setOnClickListener(View.OnClickListener {

            dialog.dismiss()

        })
        dialog.show()

    }

    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true);
        //System.exit(1);
    }


    override fun onResume() {
        super.onResume()

        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability()
                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                ) {
                    // If an in-app update is already running, resume the update.
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        this,
                        MY_REQUEST_CODE
                    )
                }
            }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode != AppCompatActivity.RESULT_OK) {
                this.finishAffinity();
                Toast.makeText(
                    this,
                    "Update flow failed!" + requestCode,
                    Toast.LENGTH_SHORT
                ).show()

                Log.e("MY_APP", "Update flow failed! Result code: $resultCode")
                // If the update is cancelled or fails,
                // you can request to start the update again.
            }
        }
    }

}