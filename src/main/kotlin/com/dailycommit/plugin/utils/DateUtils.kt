package com.dailycommit.plugin.utils

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.time.temporal.TemporalAdjusters

/**
 * 日期时间工具类
 */
object DateUtils {

    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")

    /**
     * 获取今天的日期字符串 (yyyy-MM-dd)
     */
    fun getTodayString(): String {
        return LocalDate.now().format(DATE_FORMATTER)
    }

    /**
     * 获取昨天的日期
     */
    fun getYesterday(): LocalDate {
        return LocalDate.now().minusDays(1)
    }

    /**
     * 获取昨天的日期字符串
     */
    fun getYesterdayString(): String {
        return getYesterday().format(DATE_FORMATTER)
    }

    /**
     * 检查今天是否是工作日
     */
    fun isWorkday(): Boolean {
        val today = LocalDate.now()
        val dayOfWeek = today.dayOfWeek
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY
    }

    /**
     * 检查指定日期是否是工作日
     */
    fun isWorkday(date: LocalDate): Boolean {
        val dayOfWeek = date.dayOfWeek
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY
    }

    /**
     * 获取本周的周一
     */
    fun getThisWeekMonday(): LocalDate {
        return LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }

    /**
     * 获取本周的周日
     */
    fun getThisWeekSunday(): LocalDate {
        return LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    }

    /**
     * 获取本周编号 (yyyy-Wxx)
     */
    fun getWeekNumber(): String {
        val now = LocalDate.now()
        val weekOfYear = now.get(ChronoField.ALIGNED_WEEK_OF_YEAR)
        return "${now.year}-W${weekOfYear.toString().padStart(2, '0')}"
    }

    /**
     * 格式化日期时间
     */
    fun formatDateTime(dateTime: LocalDateTime): String {
        return dateTime.format(DATETIME_FORMATTER)
    }

    /**
     * 格式化日期
     */
    fun formatDate(date: LocalDate): String {
        return date.format(DATE_FORMATTER)
    }

    /**
     * 格式化时间
     */
    fun formatTime(time: String): String {
        return try {
            val parts = time.split(":")
            if (parts.size == 2) {
                "${parts[0].padStart(2, '0')}:${parts[1].padStart(2, '0')}"
            } else {
                "17:00"
            }
        } catch (e: Exception) {
            "17:00"
        }
    }

    /**
     * 检查今天是否是指定的星期几
     */
    fun isTodayDayOfWeek(dayOfWeek: DayOfWeek): Boolean {
        return LocalDate.now().dayOfWeek == dayOfWeek
    }

    /**
     * 将字符串转换为 DayOfWeek
     */
    fun parseDayOfWeek(day: String): DayOfWeek {
        return try {
            DayOfWeek.valueOf(day.uppercase())
        } catch (e: Exception) {
            DayOfWeek.THURSDAY
        }
    }
}
