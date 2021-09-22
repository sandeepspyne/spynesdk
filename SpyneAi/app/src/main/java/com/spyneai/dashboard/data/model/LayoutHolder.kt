package com.spyneai.dashboard.data.model

import com.google.gson.annotations.SerializedName
import com.spyneai.dashboard.response.NewCategoriesResponse

class LayoutHolder {
    companion object{
        var data : List<NewCategoriesResponse.Data>? = null
    }
}