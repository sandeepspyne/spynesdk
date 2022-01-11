package com.spyneai.base.network

import com.spyneai.needs.AppConstants

class ProjectApiClient : BaseApiClient<ProjectApi>("http://35.247.153.169:4567/api/", ProjectApi::class.java) {
}