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
