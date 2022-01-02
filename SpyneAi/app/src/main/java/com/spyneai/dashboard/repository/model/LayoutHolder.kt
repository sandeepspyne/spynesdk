package com.spyneai.dashboard.repository.model

import com.spyneai.dashboard.response.NewCategoriesResponse

class LayoutHolder {
    companion object{
        var data : List<NewCategoriesResponse.Category>? = null
        var categoryPosition = 0
    }
}