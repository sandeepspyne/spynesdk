package com.spyneai.base.network

import com.spyneai.needs.AppConstants

class ProjectApiClient : BaseApiClient<ProjectApi>(AppConstants.BASE_URL, ProjectApi::class.java) {
}