package com.dailycommit.plugin.utils

import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project

/**
 * 通知工具类
 */
object NotificationUtils {

    private const val NOTIFICATION_GROUP_ID = "Daily Commit Manager"

    /**
     * 显示信息通知
     */
    fun showInfo(project: Project?, title: String, message: String) {
        showNotification(project, title, message, NotificationType.INFORMATION)
    }

    /**
     * 显示警告通知
     */
    fun showWarning(project: Project?, title: String, message: String) {
        showNotification(project, title, message, NotificationType.WARNING)
    }

    /**
     * 显示错误通知
     */
    fun showError(project: Project?, title: String, message: String) {
        showNotification(project, title, message, NotificationType.ERROR)
    }

    /**
     * 显示通知
     */
    private fun showNotification(
        project: Project?,
        title: String,
        message: String,
        type: NotificationType
    ) {
        try {
            val notification = NotificationGroupManager.getInstance()
                .getNotificationGroup(NOTIFICATION_GROUP_ID)
                .createNotification(title, message, type)

            Notifications.Bus.notify(notification, project)
        } catch (e: Exception) {
            // 如果通知组不存在，使用备用方式
            val notification = Notification(
                NOTIFICATION_GROUP_ID,
                title,
                message,
                type
            )
            Notifications.Bus.notify(notification, project)
        }
    }

    /**
     * 显示配置 API 的提示
     */
    fun showConfigureApiReminder(project: Project?) {
        showWarning(
            project,
            "需要配置 LLM API",
            "请先在 Settings -> Tools -> Daily Commit Manager 中配置 LLM API 才能使用。"
        )
    }

    /**
     * 显示提交成功的通知
     */
    fun showCommitSuccess(project: Project?, message: String) {
        showInfo(project, "提交成功", message)
    }

    /**
     * 显示日总结生成成功的通知
     */
    fun showDailySummaryGenerated(project: Project?, filePath: String) {
        showInfo(project, "日总结已生成", "今日工作总结已保存到：$filePath")
    }

    /**
     * 显示周总结生成成功的通知
     */
    fun showWeeklySummaryGenerated(project: Project?, filePath: String) {
        showInfo(project, "周总结已生成", "本周工作总结已保存到：$filePath")
    }

    /**
     * 显示 LLM API 调用失败的通知
     */
    fun showLLMError(project: Project?, errorMessage: String) {
        showError(project, "LLM API 调用失败", "错误信息：$errorMessage\n请检查 API 配置是否正确。")
    }

    /**
     * 显示昨日未提交的警告
     */
    fun showYesterdayNoCommitWarning(project: Project?) {
        showWarning(
            project,
            "昨日未提交代码",
            "检测到昨天没有提交代码，请先提交后才能继续工作。"
        )
    }
}
