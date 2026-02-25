package com.dailycommit.plugin.config

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * 插件配置数据类
 */
data class PluginSettingsState(
    // LLM 配置
    var apiKey: String = "",
    var apiBaseUrl: String = "https://api.openai.com/v1",
    var modelName: String = "gpt-3.5-turbo",
    var llmProvider: String = LLMProvider.OPENAI.name,

    // 提示词配置
    var commitMessagePrompt: String = """你是一个专业的代码提交消息生成助手。
请根据代码改动，生成简洁、清晰的 Git commit message。
遵循以下规则：
1. 使用中文
2. 第一行是简短的摘要（不超过 50 字）
3. 如果需要，第二行空行，第三行开始详细描述
4. 使用动词开头（如：添加、修复、更新、重构等）
5. 说明改动的目的和影响，而不仅仅是改动内容""",

    var dailySummaryPrompt: String = """你是一个专业的工作总结助手。
请根据今天的代码提交记录，生成一份简洁的工作总结。
要求：
1. 使用中文
2. 总结今天完成的主要工作（3-5 条）
3. 每条工作用一句话概括，突出重点
4. 按重要性排序
5. 格式简洁，易于阅读""",

    var weeklySummaryPrompt: String = """你是一个专业的工作总结助手。
请根据本周的代码提交记录，生成一份工作周报。
要求：
1. 使用中文
2. 分类总结本周工作（如：新功能、Bug 修复、优化等）
3. 每个分类列出 2-3 个主要成果
4. 突出本周的重点工作和成果
5. 格式清晰，适合向上汇报""",

    // 强制提交配置
    var enableDailyCommitCheck: Boolean = true,
    var checkWorkdaysOnly: Boolean = true,
    var commitCheckTime: String = "17:00", // 每天检查时间

    // 总结存储配置
    var dailySummaryPath: String = ".idea/daily-summaries",
    var weeklySummaryPath: String = ".idea/weekly-summaries",

    // 周总结配置
    var weeklyReportDay: String = "THURSDAY", // 周几生成
    var weeklyReportTime: String = "17:00",

    // 首次运行标记
    var isFirstRun: Boolean = true,

    // API 配置验证标记
    var apiConfigured: Boolean = false
)

/**
 * 插件设置持久化服务
 */
@State(
    name = "DailyCommitPluginSettings",
    storages = [Storage("DailyCommitPlugin.xml")]
)
class PluginSettings : PersistentStateComponent<PluginSettingsState> {

    private var state = PluginSettingsState()

    override fun getState(): PluginSettingsState {
        return state
    }

    override fun loadState(state: PluginSettingsState) {
        XmlSerializerUtil.copyBean(state, this.state)
    }

    companion object {
        fun getInstance(): PluginSettings {
            return ApplicationManager.getApplication().getService(PluginSettings::class.java)
        }
    }
}
