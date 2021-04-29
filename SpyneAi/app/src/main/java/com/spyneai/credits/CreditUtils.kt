package com.spyneai.credits

import java.text.DecimalFormat
import java.text.NumberFormat

class CreditUtils {
    companion object {
        fun getFormattedNumber(myNumber : Int) : String {
            val formatter: NumberFormat = DecimalFormat("#,###")
            return formatter.format(myNumber)
        }
    }
}