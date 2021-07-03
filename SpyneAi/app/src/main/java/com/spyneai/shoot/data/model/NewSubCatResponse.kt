package com.spyneai.dashboard.response

data class NewSubCatResponse(
    val `data`: List<Data>,
    val interior: List<Interior>,
    val message: String,
    var miscellaneous: List<Miscellaneous>,
    val status: Int
) {
    data class Data(
        val active: Int,
        val created_at: String,
        val display_thumbnail: String,
        val enterprise_id: String,
        val id: Int,
        val priority: Int,
        val prod_cat_id: String,
        val prod_sub_cat_id: String,
        val sub_cat_name: String,
        val updated_at: String
    )

    data class Interior(
        val display_name: String,
        val display_thumbnail: String,
        var isSelected : Boolean = false
    )

    data class Miscellaneous(
        val display_name: String,
        val display_thumbnail: String,
        val prod_cat_id: String,
        val prod_sub_cat_id: String,
        var isSelected : Boolean = false
    )
}