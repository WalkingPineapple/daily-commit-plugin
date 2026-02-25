package com.dailycommit.plugin.summary

import com.dailycommit.plugin.config.PluginSettings
import com.dailycommit.plugin.git.GitService
import com.dailycommit.plugin.llm.OpenAICompatibleClient
import com.dailycommit.plugin.llm.PromptBuilder
import com.dailycommit.plugin.ui.SummaryPreviewDialog
import com.dailycommit.plugin.utils.DateUtils
import com.dailycommit.plugin.utils.NotificationUtils
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import kotlinx.coroutines.runBlocking

/**
 * 周总结生成器
 */
class WeeklySummaryGenerator(private val project: Project) {

    private val gitService = GitService(project)
    private val summaryStorage = SummaryStorage(project)
    private val settings = PluginSettings.getInstance().state

    /**
     * 生成本周工作总结
     */
    fun generateWeeklySummary(showPreview: Boolean = true) {
        // 检查 API 配置
        if (!settings.apiConfigured || settings.apiKey.isEmpty()) {
            NotificationUtils.showConfigureApiReminder(project)
            return
        }

        // 获取本周的提交记录
        val weekStart = DateUtils.getThisWeekMonday()
        val weekEnd = DateUtils.getThisWeekSunday()
        val weekCommits = gitService.getCommitsBetweenDates(weekStart, weekEnd)

        if (weekCommits.isEmpty()) {
            NotificationUtils.showWarning(project, "无法生成周总结", "本周还没有任何提交记录")
            return
        }

        // 使用 IntelliJ 平台的后台任务 API
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "生成周总结...", true) {
            var generatedSummary: String? = null
            var error: Exception? = null

            override fun run(indicator: ProgressIndicator) {
                try {
                    indicator.text = "正在调用 LLM API..."

                    val client = OpenAICompatibleClient(
                        baseUrl = settings.apiBaseUrl,
                        apiKey = settings.apiKey,
                        model = settings.modelName
                    )

                    val weekRange = "${DateUtils.formatDate(weekStart)} 至 ${DateUtils.formatDate(weekEnd)}"
                    val (systemMessage, userMessage) = PromptBuilder.buildWeeklySummaryPrompt(
                        weekCommits,
                        weekRange
                    )

                    // 在后台线程中同步调用
                    generatedSummary = runBlocking {
                        client.generateText(systemMessage, userMessage)
                    }

                } catch (e: Exception) {
                    error = e
                }
            }

            override fun onSuccess() {
                val summary = generatedSummary
                if (summary != null) {
                    val weekRange = "${DateUtils.formatDate(weekStart)} 至 ${DateUtils.formatDate(weekEnd)}"
                    if (showPreview) {
                        // 显示预览对话框，允许用户编辑
                        ApplicationManager.getApplication().invokeLater {
                            SummaryPreviewDialog.showWeeklySummaryPreview(
                                project,
                                summary
                            ) { editedSummary ->
                                saveWeeklySummary(editedSummary, weekRange)
                            }
                        }
                    } else {
                        saveWeeklySummary(summary, weekRange)
                    }
                }
            }

            override fun onThrowable(throwable: Throwable) {
                val e = error ?: throwable
                NotificationUtils.showLLMError(project, e.message ?: "Unknown error")
            }
        })
    }

    /**
     * 保存周总结
     */
    private fun saveWeeklySummary(summary: String, weekRange: String) {
        val header = """
            ==========================================
            周报 - ${DateUtils.getWeekNumber()}
            时间范围：$weekRange
            ==========================================

        """.trimIndent()

        val fullContent = header + summary
        val filePath = summaryStorage.saveWeeklySummary(content = fullContent)
        NotificationUtils.showWeeklySummaryGenerated(project, filePath)
    }

    /**
     * 快速生成并保存（不显示预览）
     */
    fun generateAndSaveQuickly() {
        generateWeeklySummary(showPreview = false)
    }

    /**
     * 检查本周是否已生成周总结
     */
    fun hasThisWeekSummary(): Boolean {
        return summaryStorage.hasThisWeekSummary()
    }
}
