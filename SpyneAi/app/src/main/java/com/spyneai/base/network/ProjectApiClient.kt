package com.spyneai.base.network

import com.spyneai.needs.AppConstants

class ProjectApiClient : BaseApiClient<ProjectApi>("http://35.240.170.119/api/", ProjectApi::class.java) {
}