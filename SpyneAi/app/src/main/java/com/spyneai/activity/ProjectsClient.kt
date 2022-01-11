package com.spyneai.activity

import com.spyneai.base.network.BaseApiClient
import com.spyneai.base.network.ClipperApi
import com.spyneai.base.network.ProjectApi
import com.spyneai.needs.AppConstants

class ProjectsClient : BaseApiClient<ProjectApi>("http://35.247.153.169:4567/api/", ProjectApi::class.java) {
}