package com.dailycommit.plugin.config

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import java.awt.BorderLayout
import javax.swing.*

/**
 * 插件配置界面
 */
class PluginConfigurable : Configurable {

    private val settings = PluginSettings.getInstance().state

    // UI 组件
    private lateinit var apiKeyField: JBPasswordField
    private lateinit var apiBaseUrlField: JBTextField
    private lateinit var modelNameField: JBTextField
    private lateinit var llmProviderCombo: ComboBox<String>

    private lateinit var enableDailyCheckBox: JBCheckBox
    private lateinit var checkWorkdaysOnlyBox: JBCheckBox
    private lateinit var commitCheckTimeField: JBTextField

    private lateinit var dailySummaryPathField: JBTextField
    private lateinit var weeklySummaryPathField: JBTextField

    private lateinit var weeklyReportDayCombo: ComboBox<String>
    private lateinit var weeklyReportTimeField: JBTextField

    private lateinit var mainPanel: JPanel

    override fun getDisplayName(): String = "Daily Commit Manager"

    override fun createComponent(): JComponent {
        // 初始化组件
        apiKeyField = JBPasswordField()
        apiBaseUrlField = JBTextField()
        modelNameField = JBTextField()
        llmProviderCombo = ComboBox(LLMProvider.values().map { it.displayName }.toTypedArray())

        enableDailyCheckBox = JBCheckBox("启用每日提交检查")
        checkWorkdaysOnlyBox = JBCheckBox("仅工作日检查")
        commitCheckTimeField = JBTextField()

        dailySummaryPathField = JBTextField()
        weeklySummaryPathField = JBTextField()

        weeklyReportDayCombo = ComboBox(arrayOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"))
        weeklyReportTimeField = JBTextField()

        // LLM 提供商变更监听
        llmProviderCombo.addActionListener {
            val selected = llmProviderCombo.selectedItem as? String
            val provider = LLMProvider.fromDisplayName(selected ?: "")
            apiBaseUrlField.text = provider.defaultBaseUrl
        }

        // 构建表单
        mainPanel = JPanel(BorderLayout())
        val formPanel = FormBuilder.createFormBuilder()
            .addComponent(JBLabel("<html><h2>LLM API 配置</h2></html>"))
            .addLabeledComponent("LLM 提供商:", llmProviderCombo)
            .addLabeledComponent("API Key:", apiKeyField)
            .addLabeledComponent("API Base URL:", apiBaseUrlField)
            .addLabeledComponent("模型名称:", modelNameField)
            .addVerticalGap(15)

            .addComponent(JBLabel("<html><h2>每日提交配置</h2></html>"))
            .addComponent(enableDailyCheckBox)
            .addComponent(checkWorkdaysOnlyBox)
            .addLabeledComponent("检查时间 (HH:mm):", commitCheckTimeField)
            .addVerticalGap(15)

            .addComponent(JBLabel("<html><h2>总结存储配置</h2></html>"))
            .addLabeledComponent("日总结路径:", dailySummaryPathField)
            .addLabeledComponent("周总结路径:", weeklySummaryPathField)
            .addVerticalGap(15)

            .addComponent(JBLabel("<html><h2>周总结配置</h2></html>"))
            .addLabeledComponent("生成日期:", weeklyReportDayCombo)
            .addLabeledComponent("生成时间 (HH:mm):", weeklyReportTimeField)
            .addVerticalGap(15)

            .addComponent(JBLabel("<html><p style='color:gray;'>提示：首次使用请先配置 API Key，支持国内主流 LLM。</p></html>"))
            .addComponentFillVertically(JPanel(), 0)
            .panel

        mainPanel.add(formPanel, BorderLayout.CENTER)
        reset()
        return mainPanel
    }

    override fun isModified(): Boolean {
        return apiKeyField.password.concatToString() != settings.apiKey ||
                apiBaseUrlField.text != settings.apiBaseUrl ||
                modelNameField.text != settings.modelName ||
                llmProviderCombo.selectedItem != LLMProvider.valueOf(settings.llmProvider).displayName ||
                enableDailyCheckBox.isSelected != settings.enableDailyCommitCheck ||
                checkWorkdaysOnlyBox.isSelected != settings.checkWorkdaysOnly ||
                commitCheckTimeField.text != settings.commitCheckTime ||
                dailySummaryPathField.text != settings.dailySummaryPath ||
                weeklySummaryPathField.text != settings.weeklySummaryPath ||
                weeklyReportDayCombo.selectedItem != settings.weeklyReportDay ||
                weeklyReportTimeField.text != settings.weeklyReportTime
    }

    override fun apply() {
        settings.apiKey = apiKeyField.password.concatToString()
        settings.apiBaseUrl = apiBaseUrlField.text
        settings.modelName = modelNameField.text
        val selectedProvider = llmProviderCombo.selectedItem as? String
        settings.llmProvider = LLMProvider.fromDisplayName(selectedProvider ?: "").name

        settings.enableDailyCommitCheck = enableDailyCheckBox.isSelected
        settings.checkWorkdaysOnly = checkWorkdaysOnlyBox.isSelected
        settings.commitCheckTime = commitCheckTimeField.text

        settings.dailySummaryPath = dailySummaryPathField.text
        settings.weeklySummaryPath = weeklySummaryPathField.text

        settings.weeklyReportDay = weeklyReportDayCombo.selectedItem as? String ?: "THURSDAY"
        settings.weeklyReportTime = weeklyReportTimeField.text

        // 标记 API 已配置
        settings.apiConfigured = settings.apiKey.isNotEmpty() &&
                                 settings.apiBaseUrl.isNotEmpty() &&
                                 settings.modelName.isNotEmpty()
    }

    override fun reset() {
        apiKeyField.text = settings.apiKey
        apiBaseUrlField.text = settings.apiBaseUrl
        modelNameField.text = settings.modelName
        llmProviderCombo.selectedItem = LLMProvider.valueOf(settings.llmProvider).displayName

        enableDailyCheckBox.isSelected = settings.enableDailyCommitCheck
        checkWorkdaysOnlyBox.isSelected = settings.checkWorkdaysOnly
        commitCheckTimeField.text = settings.commitCheckTime

        dailySummaryPathField.text = settings.dailySummaryPath
        weeklySummaryPathField.text = settings.weeklySummaryPath

        weeklyReportDayCombo.selectedItem = settings.weeklyReportDay
        weeklyReportTimeField.text = settings.weeklyReportTime
    }
}
