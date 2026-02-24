package com.dailycommit.plugin.summary

import com.dailycommit.plugin.config.PluginSettings
import com.dailycommit.plugin.git.CommitChecker
import com.dailycommit.plugin.git.DiffAnalyzer
import com.dailycommit.plugin.llm.OpenAICompatibleClient
import com.dailycommit.plugin.llm.PromptBuilder
import com.dailycommit.plugin.ui.SummaryPreviewDialog
import com.dailycommit.plugin.utils.DateUtils
import com.dailycommit.plugin.utils.NotificationUtils
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 日总结生成器
 */
class DailySummaryGenerator(private val project: Project) {

    private val commitChecker = CommitChecker(project)
    private val diffAnalyzer = DiffAnalyzer(project)
    private val summaryStorage = SummaryStorage(project)
    private val settings = PluginSettings.getInstance().state

    /**
     * 生成今日工作总结
     */
    fun generateDailySummary(showPreview: Boolean = true) {
        // 检查 API 配置
        if (!settings.apiConfigured || settings.apiKey.isEmpty()) {
            NotificationUtils.showConfigureApiReminder(project)
            return
        }

        // 获取今天的提交记录
        val todayCommits = commitChecker.getTodayCommits()

        if (todayCommits.isEmpty()) {
            NotificationUtils.showWarning(project, "无法生成日总结", "今天还没有任何提交记录")
            return
        }

        // 异步生成总结
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OpenAICompatibleClient(
                    baseUrl = settings.apiBaseUrl,
                    apiKey = settings.apiKey,
                    model = settings.modelName
                )

                val changesSummary = diffAnalyzer.getChangesDescription()
                val (systemMessage, userMessage) = PromptBuilder.buildDailySummaryPrompt(
                    todayCommits,
                    changesSummary
                )

                val generatedSummary = client.generateText(systemMessage, userMessage)

                withContext(Dispatchers.Main) {
                    if (showPreview) {
                        // 显示预览对话框，允许用户编辑
                        ApplicationManager.getApplication().invokeLater {
                            SummaryPreviewDialog.showDailySummaryPreview(
                                project,
                                generatedSummary
                            ) { editedSummary ->
                                saveDailySummary(editedSummary)
                            }
                        }
                    } else {
                        saveDailySummary(generatedSummary)
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    NotificationUtils.showLLMError(project, e.message ?: "Unknown error")
                }
            }
        }
    }

    /**
     * 保存日总结
     */
    private fun saveDailySummary(summary: String) {
        val header = """
            ==========================================
            日期：${DateUtils.getTodayString()}
            工作日报
            ==========================================

        """.trimIndent()

        val fullContent = header + summary
        val filePath = summaryStorage.saveDailySummary(content = fullContent)
        NotificationUtils.showDailySummaryGenerated(project, filePath)
    }

    /**
     * 快速生成并保存（不显示预览）
     */
    fun generateAndSaveQuickly() {
        generateDailySummary(showPreview = false)
    }
}
