package com.halibiram.tomato.core.common.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// DateUtils
object DateUtils {
    fun formatDate(date: Date, format: String = "yyyy-MM-dd HH:mm:ss"): String {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        return sdf.format(date)
    }
}
