package com.spyneai.base.network

import com.spyneai.needs.AppConstants

class ClipperApiClient : BaseApiClient<ClipperApi>(AppConstants.BASE_URL, ClipperApi::class.java) {
}