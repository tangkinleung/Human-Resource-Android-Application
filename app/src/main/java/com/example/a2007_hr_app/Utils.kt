package com.example.a2007_hr_app

import java.text.SimpleDateFormat


private const val SECOND_MILLIS = 1000
private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
private const val DAY_MILLIS = 24 * HOUR_MILLIS

class Utils {
    fun getTimeAgo(dateLong: Long): String? {
        var time = dateLong
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000
        }
        val now: Long = System.currentTimeMillis()
        if (time > now || time <= 0) {
            return null
        }

        // TODO: localize
        val diff = now - time
        return when {
            diff < MINUTE_MILLIS -> {
                "just now"
            }
            diff < 2 * MINUTE_MILLIS -> {
                "a minute ago"
            }
            diff < 50 * MINUTE_MILLIS -> {
                (diff/MINUTE_MILLIS).toString() + " minutes ago"
            }
            diff < 90 * MINUTE_MILLIS -> {
                "an hour ago"
            }
            diff < 24 * HOUR_MILLIS -> {
                (diff/HOUR_MILLIS).toString() + " hours ago"
            }
            diff < 48 * HOUR_MILLIS -> {
                "yesterday"
            }
            else -> {
                (diff/DAY_MILLIS).toString() + " days ago"
            }
        }
    }



    fun convertDateTimeToLong(date: String): Long {
        val df = SimpleDateFormat("dd-MM-yyyy-HH-mm-ss")
        return df.parse(date).time
    }

    fun convertDateToLong(date: String): Long {
        val df = SimpleDateFormat("dd/MM/yyyy")
        return df.parse(date).time
    }


}

