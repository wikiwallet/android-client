package io.golos.cyber_android.utils

import android.content.Context
import io.golos.cyber_android.R
import io.golos.cyber_android.ui.common.formatters.time_estimation.TimeEstimationFormatter
import io.golos.cyber_android.utils.DateUtils.ESTIMATE_TIME_FORMAT
import io.golos.cyber_android.utils.DateUtils.MMMM_DD_YYYY_FORMAT
import io.golos.cyber_android.utils.DateUtils.hour
import io.golos.cyber_android.utils.DateUtils.minute
import io.golos.cyber_android.utils.DateUtils.week
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun createTimeLabel(
        fromTimeStamp: Long,
        elapsedMinutes: Int,
        elapsedHours: Int,
        elapsedDays: Int,
        context: Context
    ): String {
        val mSdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        return when {
            elapsedMinutes == 0 -> context.getString(io.golos.cyber_android.R.string.now_happened)
            elapsedMinutes < 60 -> "${context.resources.getQuantityString(
                io.golos.cyber_android.R.plurals.minutes,
                elapsedMinutes,
                elapsedMinutes
            )}  ${context.resources.getString(io.golos.cyber_android.R.string.ago)}"

            elapsedHours < 24 -> "${context.resources.getQuantityString(
                io.golos.cyber_android.R.plurals.hours,
                elapsedHours,
                elapsedHours
            )}  ${context.resources.getString(io.golos.cyber_android.R.string.ago)}"
            else -> {
                if (elapsedDays <= 7)
                    "${context.resources.getQuantityString(
                        io.golos.cyber_android.R.plurals.days,
                        elapsedDays,
                        elapsedDays
                    )} ${context.resources.getString(io.golos.cyber_android.R.string.ago)}"
                else {
                    val timeStamp = fromTimeStamp + TimeZone.getDefault().getOffset(fromTimeStamp)
                    Calendar.getInstance(TimeZone.getDefault()).apply { timeInMillis = timeStamp }.let {
                        mSdf.format(it.time)
                    }
                }
            }
        }
    }

    const val MMMM_DD_YYYY_FORMAT = "MMMM dd, yyyy"
    const val ESTIMATE_TIME_FORMAT = "MM.dd.yyyy"

    const val second = 1000L
    const val minute = second * 60
    const val hour = minute * 60
    const val day = hour * 24
    const val week = day * 7
    const val month = day * 30     // :) - was allowed by Vlad
}

fun Date.toMMMM_DD_YYYY_Format(): String {
    return SimpleDateFormat(MMMM_DD_YYYY_FORMAT, Locale.US).format(this)
}

fun Date.toTimeEstimateFormat(context: Context): String{

    val now = Date().time
    val estimated = this.time
    val resources = context.resources
    return when {
        estimated >= now ->
            resources.getString(R.string.date_time_format_just_now)  // Somewhere in a future

        now - estimated < DateUtils.minute ->
            resources.getString(R.string.date_time_format_just_now)

        now - estimated < DateUtils.hour ->
            resources.getFormattedString(R.string.date_time_format_minutes_ago, (now - estimated)/ DateUtils.minute)

        now - estimated < DateUtils.day ->
            resources.getFormattedString(R.string.date_time_format_hours_ago, (now - estimated)/ DateUtils.hour)

        now - estimated < DateUtils.week ->
            resources.getFormattedString(R.string.date_time_format_days_ago, (now - estimated)/ DateUtils.day)

        now - estimated < DateUtils.month ->
            resources.getFormattedString(R.string.date_time_format_weeks_ago, (now - estimated)/ DateUtils.week)

        else -> SimpleDateFormat(ESTIMATE_TIME_FORMAT, Locale.getDefault()).format(this)
    }
}