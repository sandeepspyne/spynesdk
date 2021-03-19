/* 
Copyright (c) 2021 Kotlin Data Classes Generated from JSON powered by http://www.json2kotlin.com

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

For support, please feel free to contact me at https://www.linkedin.com/in/syedabsar */


package com.spyneai.model.orders

import com.google.gson.annotations.SerializedName
import com.spyneai.model.order.Sku

data class Submitted (

	@SerializedName("userId")
	val userId : String,
	@SerializedName("shootId")
	val shootId : String,
	@SerializedName("businessName")
	val businessName : String,
	@SerializedName("prodId")
	val prodId : String,
	@SerializedName("catId")
	val catId : String,
	@SerializedName("categoryName")
	val categoryName : String,
	@SerializedName("productName")
	val productName : String,
	@SerializedName("status")
	val status : String,
	@SerializedName("shootName")
	val shootName : String,
	@SerializedName("skuOneDisplayThumnail")
	val skuOneDisplayThumnail : String,
	@SerializedName("skuTwoDisplayThumnail")
	val skuTwoDisplayThumnail : String,
	@SerializedName("numberOfSkus")
	val numberOfSkus : Int,
	@SerializedName("shootAmount")
	val shootAmount : Int,
	@SerializedName("marketPlace")
	val marketPlace : List<MarketPlace>,
	@SerializedName("skus")
	val skus : List<Sku>,
	@SerializedName("expectedDate")
	val expectedDate : String,
	@SerializedName("submittedDate")
	val submittedDate : String,
	@SerializedName("creationDate")
	val creationDate : String
)