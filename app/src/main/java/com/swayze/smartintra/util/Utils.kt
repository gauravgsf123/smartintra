package com.swayze.smartintra.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresApi
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Date

object Utils {

    fun hasInternetConnection(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        }

        return false
    }

    @SuppressLint("SimpleDateFormat")
    fun getDate(time:Long):String {
        val date = Date(time); // *1000 is to convert seconds to milliseconds
        val sdf  = SimpleDateFormat("dd/MM/yyyy HH:mm"); // the format of your date
        return sdf.format(date);
    }

    @SuppressLint("SimpleDateFormat")
    fun getDate(time:Long, format: String):String {
        val date = Date(time); // *1000 is to convert seconds to milliseconds
        val sdf  = SimpleDateFormat(format); // the format of your date
        return sdf.format(date);
    }

    @SuppressLint("SimpleDateFormat")
    fun getDate(format: String): String {
        val sdf = SimpleDateFormat(format)
        return sdf.format(Date());
    }

    fun milliseconds(date: String?): Long {
        //String date_ = date;
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        try {
            val mDate = sdf.parse(date)
            val timeInMilliseconds = mDate.time
            println("Date in milli :: $timeInMilliseconds")
            return timeInMilliseconds
        } catch (e: ParseException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
        return 0
    }

    fun checkDate(date:String,calenderDate:String):Boolean{
        var enteredDate: Date? = null
        var currentDate: Date? = null
        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy")
            enteredDate = sdf.parse(date)
            currentDate = sdf.parse(calenderDate)
        } catch (ex: Exception) {
            // enteredDate will be null if date="287686";
        }

        return enteredDate?.before(currentDate) == true //|| enteredDate?.equals(currentDate)== true
    }


    fun getDayAndDate(): String{
        /*val dateTime = java.time.LocalDate.now()
        val result = "${dateTime.dayOfWeek.getDisplayName(
            java.time.format.TextStyle.FULL,
            java.util.Locale.getDefault()
        )}, ${dateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}"
        return result*/
        val now = Date()
        val dayName = SimpleDateFormat("EEEE", java.util.Locale.getDefault()).format(now)
        val dateStr = SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(now)
        return "$dayName, $dateStr"
    }
}