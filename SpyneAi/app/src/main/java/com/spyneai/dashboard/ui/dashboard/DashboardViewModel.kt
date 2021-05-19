package com.spyneai.dashboard.ui.dashboard

import androidx.lifecycle.ViewModel
import com.spyneai.dashboard.data.repository.BaseRepository
import com.spyneai.dashboard.data.repository.DashboardRepository

class DashboardViewModel(
    private val repository: DashboardRepository
) : ViewModel() {
}