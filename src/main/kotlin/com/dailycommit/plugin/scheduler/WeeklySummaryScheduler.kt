package com.dailycommit.plugin.scheduler

import com.dailycommit.plugin.config.PluginSettings
import com.dailycommit.plugin.summary.WeeklySummaryGenerator
import com.dailycommit.plugin.utils.DateUtils
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.AppExecutorUtil
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * 周总结调度器
 * 每周四（或配置的其他日期）自动生成周总结
 */
class WeeklySummaryScheduler(private val project: Project) {

    private val settings = PluginSettings.getInstance().state
    private var scheduledTask: ScheduledFuture<*>? = null
    private var lastGeneratedWeek: String? = null

    /**
     * 启动调度器
     */
    fun start() {
        if (scheduledTask != null && !scheduledTask!!.isCancelled) {
            return // 已经在运行
        }

        val executor = AppExecutorUtil.getAppScheduledExecutorService()

        // 每小时检查一次
        scheduledTask = executor.scheduleWithFixedDelay({
            checkAndGenerateWeeklySummary()
        }, 0, 1, TimeUnit.HOURS)
    }

    /**
     * 停止调度器
     */
    fun stop() {
        scheduledTask?.cancel(false)
        scheduledTask = null
    }

    /**
     * 检查并生成周总结
     */
    private fun checkAndGenerateWeeklySummary() {
        try {
            val now = LocalDateTime.now()
            val currentDayOfWeek = now.dayOfWeek
            val currentHour = now.hour

            // 解析配置的生成日期和时间
            val targetDayOfWeek = DateUtils.parseDayOfWeek(settings.weeklyReportDay)
            val targetTime = settings.weeklyReportTime.split(":")
            val targetHour = targetTime.getOrNull(0)?.toIntOrNull() ?: 17

            // 检查是否是目标日期和时间
            if (currentDayOfWeek == targetDayOfWeek && currentHour == targetHour) {
                val currentWeek = DateUtils.getWeekNumber()

                // 检查本周是否已生成
                if (lastGeneratedWeek != currentWeek) {
                    ApplicationManager.getApplication().invokeLater {
                        generateWeeklySummary()
                    }
                    lastGeneratedWeek = currentWeek
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 生成周总结
     */
    private fun generateWeeklySummary() {
        val generator = WeeklySummaryGenerator(project)

        // 检查是否已经有本周的总结
        if (!generator.hasThisWeekSummary()) {
            generator.generateWeeklySummary(showPreview = true)
        }
    }

    companion object {
        private val instances = mutableMapOf<Project, WeeklySummaryScheduler>()

        /**
         * 获取或创建调度器实例
         */
        fun getInstance(project: Project): WeeklySummaryScheduler {
            return instances.getOrPut(project) {
                WeeklySummaryScheduler(project)
            }
        }
    }
}
