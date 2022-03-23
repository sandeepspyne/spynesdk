package com.spyneai.sdk

import android.content.Context
import com.spyneai.base.network.Resource
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shoot.data.ShootRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RefreshAuthToken(
    val context: Context,
    val shootRepository: ShootRepository
) {

    fun refresh() {
        GlobalScope.launch(Dispatchers.Default) {
            val response = shootRepository.signupIntoSDK(
                Spyne.apiKey.toString(),
                Spyne.contactNo.toString(),
                Spyne.email.toString(),
                Spyne.userId.toString()
            )

            when (response) {
                is Resource.Success -> {
                    // auth save
                    Utilities.savePrefrence(
                        context,
                        AppConstants.AUTH_KEY,
                        response.value.data.secretKey
                    )
                }

                is Resource.Failure -> {
                    refresh()
                }
            }
        }

    }
}