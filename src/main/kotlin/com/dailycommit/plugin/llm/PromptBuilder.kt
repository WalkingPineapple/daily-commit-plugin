package com.dailycommit.plugin.llm

/**
 * Prompt 构建器 - 为不同场景构建提示词
 */
object PromptBuilder {

    /**
     * 生成 commit message 的系统提示
     */
    private const val COMMIT_MESSAGE_SYSTEM = """
你是一个专业的代码提交消息生成助手。
请根据代码改动，生成简洁、清晰的 Git commit message。
遵循以下规则：
1. 使用中文
2. 第一行是简短的摘要（不超过 50 字）
3. 如果需要，第二行空行，第三行开始详细描述
4. 使用动词开头（如：添加、修复、更新、重构等）
5. 说明改动的目的和影响，而不仅仅是改动内容
"""

    /**
     * 生成日总结的系统提示
     */
    private const val DAILY_SUMMARY_SYSTEM = """
你是一个专业的工作总结助手。
请根据今天的代码提交记录，生成一份简洁的工作总结。
要求：
1. 使用中文
2. 总结今天完成的主要工作（3-5 条）
3. 每条工作用一句话概括，突出重点
4. 按重要性排序
5. 格式简洁，易于阅读
"""

    /**
     * 生成周总结的系统提示
     */
    private const val WEEKLY_SUMMARY_SYSTEM = """
你是一个专业的工作总结助手。
请根据本周的代码提交记录，生成一份工作周报。
要求：
1. 使用中文
2. 分类总结本周工作（如：新功能、Bug 修复、优化等）
3. 每个分类列出 2-3 个主要成果
4. 突出本周的重点工作和成果
5. 格式清晰，适合向上汇报
"""

    /**
     * 构建生成 commit message 的 prompt
     */
    fun buildCommitMessagePrompt(changesSummary: String, diff: String): Pair<String, String> {
        val userMessage = """
请为以下代码改动生成 commit message：

改动摘要：
$changesSummary

详细 diff（仅供参考，内容较长时可忽略部分细节）：
${diff.take(2000)}${if (diff.length > 2000) "\n...(内容过长，已截断)" else ""}

请生成一个简洁清晰的 commit message。
"""
        return COMMIT_MESSAGE_SYSTEM to userMessage
    }

    /**
     * 构建生成日总结的 prompt
     */
    fun buildDailySummaryPrompt(commits: List<String>, changesSummary: String): Pair<String, String> {
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
        return DAILY_SUMMARY_SYSTEM to userMessage
    }

    /**
     * 构建生成周总结的 prompt
     */
    fun buildWeeklySummaryPrompt(commits: List<String>, weekRange: String): Pair<String, String> {
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
        return WEEKLY_SUMMARY_SYSTEM to userMessage
    }

    /**
     * 构建测试连接的简单 prompt
     */
    fun buildTestPrompt(): Pair<String, String> {
        return "You are a helpful assistant." to "Say 'Hello' in Chinese."
    }
}
