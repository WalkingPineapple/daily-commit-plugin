package com.dailycommit.plugin.summary

import com.dailycommit.plugin.config.PluginSettings
import com.dailycommit.plugin.utils.DateUtils
import com.dailycommit.plugin.utils.FileUtils
import com.intellij.openapi.project.Project
import java.io.File

/**
 * 总结存储管理器
 */
class SummaryStorage(private val project: Project) {

    private val settings = PluginSettings.getInstance().state

    /**
     * 保存日总结
     */
    fun saveDailySummary(date: String = DateUtils.getTodayString(), content: String): String {
        val fileName = "$date.txt"
        val filePath = FileUtils.getProjectFilePath(project, settings.dailySummaryPath, fileName)

        // 确保目录存在
        FileUtils.ensureProjectDirectoryExists(project, settings.dailySummaryPath)

        // 保存文件
        FileUtils.saveTextToFile(filePath, content)

        return filePath
    }

    /**
     * 保存周总结
     */
    fun saveWeeklySummary(weekNumber: String = DateUtils.getWeekNumber(), content: String): String {
        val fileName = "$weekNumber.txt"
        val filePath = FileUtils.getProjectFilePath(project, settings.weeklySummaryPath, fileName)

        // 确保目录存在
        FileUtils.ensureProjectDirectoryExists(project, settings.weeklySummaryPath)

        // 保存文件
        FileUtils.saveTextToFile(filePath, content)

        return filePath
    }

    /**
     * 读取日总结
     */
    fun readDailySummary(date: String = DateUtils.getTodayString()): String? {
        val fileName = "$date.txt"
        val filePath = FileUtils.getProjectFilePath(project, settings.dailySummaryPath, fileName)

        return if (FileUtils.fileExists(filePath)) {
            FileUtils.readFileContent(filePath)
        } else {
            null
        }
    }

    /**
     * 读取周总结
     */
    fun readWeeklySummary(weekNumber: String = DateUtils.getWeekNumber()): String? {
        val fileName = "$weekNumber.txt"
        val filePath = FileUtils.getProjectFilePath(project, settings.weeklySummaryPath, fileName)

        return if (FileUtils.fileExists(filePath)) {
            FileUtils.readFileContent(filePath)
        } else {
            null
        }
    }

    /**
     * 获取所有日总结文件
     */
    fun getAllDailySummaries(): List<File> {
        return FileUtils.listProjectFiles(project, settings.dailySummaryPath, "txt")
    }

    /**
     * 获取所有周总结文件
     */
    fun getAllWeeklySummaries(): List<File> {
        return FileUtils.listProjectFiles(project, settings.weeklySummaryPath, "txt")
    }

    /**
     * 检查今天是否已有日总结
     */
    fun hasTodaySummary(): Boolean {
        return readDailySummary() != null
    }

    /**
     * 检查本周是否已有周总结
     */
    fun hasThisWeekSummary(): Boolean {
        return readWeeklySummary() != null
    }
}
