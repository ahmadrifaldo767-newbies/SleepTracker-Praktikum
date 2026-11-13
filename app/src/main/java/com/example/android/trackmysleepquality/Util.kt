package com.example.android.trackmysleepquality

import android.content.res.Resources
import android.os.Build
import android.text.Html
import android.text.Spanned
import androidx.core.text.HtmlCompat
import com.example.android.trackmysleepquality.database.SleepNight
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

fun formatNights(nights: List<SleepNight>, resources: Resources): Spanned {
    val sb = StringBuilder()
    sb.apply {
        append("<b><h3>RIWAYAT DATA TIDUR ANDA</h3></b><br>")
        val indexedNights = nights.sortedBy { it.nightId }
        var stepCounter = 1

        for (night in indexedNights) {
            val durationMilli = night.endTimeMilli - night.startTimeMilli

            append("<br>")
            append("<b>--- Langkah / Percobaan ke-$stepCounter ---</b><br>")
            append("<b>Mulai:</b> " + convertLongToDateString(night.startTimeMilli) + "<br>")
            append("<b>Selesai:</b> " + convertLongToDateString(night.endTimeMilli) + "<br>")
            append("<b>Kualitas:</b> " + convertNumericQualityToString(night.sleepQuality) + "<br>")
            append("<b>Durasi Tidur:</b> " + formatDuration(durationMilli) + "<br>")
            stepCounter++
        }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        return Html.fromHtml(sb.toString(), Html.FROM_HTML_MODE_LEGACY)
    } else {
        @Suppress("DEPRECATION")
        return HtmlCompat.fromHtml(sb.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)
    }
}

// Fungsi untuk mengubah milidetik menjadi format Jam, Menit, Detik yang mudah dibaca
fun formatDuration(durationMilli: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(durationMilli)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMilli) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMilli) % 60
    return "$hours jam $minutes menit $seconds detik"
}

fun convertNumericQualityToString(quality: Int): String {
    return when (quality) {
        0 -> "Sangat Buruk (0)"
        1 -> "Buruk (1)"
        2 -> "Kurang Baik (2)"
        3 -> "Cukup Baik (3)"
        4 -> "Sangat Baik (4)"
        5 -> "Luar Biasa / Sempurna (5)"
        else -> "Belum Dinilai / --"
    }
}

fun convertLongToDateString(systemTime: Long): String {
    return SimpleDateFormat("EEEE, dd MMM yyyy HH:mm",
        Locale("id", "ID")).format(systemTime)
}