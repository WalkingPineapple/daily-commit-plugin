package com.dailycommit.plugin.llm

import com.dailycommit.plugin.config.PluginSettings

/**
 * Prompt 构建器 - 为不同场景构建提示词
 */
object PromptBuilder {

    private val settings = PluginSettings.getInstance().state

    /**
     * 构建生成 commit message 的 prompt
     */
    fun buildCommitMessagePrompt(changesSummary: String, diff: String): Pair<String, String> {
        val systemMessage = settings.commitMessagePrompt

        val userMessage = """
请为以下代码改动生成 commit message：

改动摘要：
$changesSummary

详细 diff（仅供参考，内容较长时可忽略部分细节）：
${diff.take(2000)}${if (diff.length > 2000) "\n...(内容过长，已截断)" else ""}

请生成一个简洁清晰的 commit message。
"""
        return systemMessage to userMessage
    }

    /**
     * 构建生成日总结的 prompt
     */
    fun buildDailySummaryPrompt(commits: List<String>, changesSummary: String): Pair<String, String> {
        val systemMessage = settings.dailySummaryPrompt

        val commitsText = if (commits.isEmpty()) {
            "今天没有提交记录。"
        } else {
            commits.mapIndexed { index, commit ->
                "${index + 1}. $commit"
            }.joinToString("\n")
        }

        val userMessage = """
请根据今天的工作记录生成日总结。

今日提交记录：
$commitsText

今日主要改动：
$changesSummary

请生成一份简洁的工作日报，总结今天完成的主要工作。
"""
        return systemMessage to userMessage
    }

    /**
     * 构建生成周总结的 prompt
     */
    fun buildWeeklySummaryPrompt(commits: List<String>, weekRange: String): Pair<String, String> {
        val systemMessage = settings.weeklySummaryPrompt

        val commitsText = if (commits.isEmpty()) {
            "本周没有提交记录。"
        } else {
            commits.mapIndexed { index, commit ->
                "${index + 1}. $commit"
            }.joinToString("\n")
        }

        val userMessage = """
请根据本周（$weekRange）的工作记录生成周总结。

本周提交记录（共 ${commits.size} 条）：
$commitsText

请生成一份工作周报，分类总结本周的主要工作和成果。
"""
        return systemMessage to userMessage
    }

    /**
     * 构建测试连接的简单 prompt
     */
    fun buildTestPrompt(): Pair<String, String> {
        return "You are a helpful assistant." to "Say 'Hello' in Chinese."
    }
}
