package com.dailycommit.plugin.config

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.FormBuilder
import com.dailycommit.plugin.llm.OpenAICompatibleClient
import com.intellij.openapi.application.ApplicationManager
import kotlinx.coroutines.runBlocking
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.Dimension
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
    private lateinit var testApiButton: JButton
    private lateinit var testStatusLabel: JBLabel

    private lateinit var commitMessagePromptArea: JBTextArea
    private lateinit var dailySummaryPromptArea: JBTextArea
    private lateinit var weeklySummaryPromptArea: JBTextArea

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

        // 测试API按钮和状态标签
        testApiButton = JButton("测试 API 连接")
        testStatusLabel = JBLabel("")

        // 提示词编辑区域
        commitMessagePromptArea = JBTextArea(10, 60)
        commitMessagePromptArea.lineWrap = true
        commitMessagePromptArea.wrapStyleWord = true

        dailySummaryPromptArea = JBTextArea(10, 60)
        dailySummaryPromptArea.lineWrap = true
        dailySummaryPromptArea.wrapStyleWord = true

        weeklySummaryPromptArea = JBTextArea(10, 60)
        weeklySummaryPromptArea.lineWrap = true
        weeklySummaryPromptArea.wrapStyleWord = true

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

        // 测试API按钮点击事件
        testApiButton.addActionListener {
            testApiConnection()
        }

        // 构建表单
        mainPanel = JPanel(BorderLayout())

        // 创建测试按钮面板
        val testPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        testPanel.add(testApiButton)
        testPanel.add(testStatusLabel)

        val formPanel = FormBuilder.createFormBuilder()
            .addComponent(JBLabel("<html><h2>LLM API 配置</h2></html>"))
            .addLabeledComponent("LLM 提供商:", llmProviderCombo)
            .addLabeledComponent("API Key:", apiKeyField)
            .addLabeledComponent("API Base URL:", apiBaseUrlField)
            .addLabeledComponent("模型名称:", modelNameField)
            .addComponent(testPanel)
            .addVerticalGap(15)

            .addComponent(JBLabel("<html><h2>提示词配置</h2></html>"))
            .addComponent(JBLabel("<html><p style='color:gray;'>可以自定义 AI 生成的提示词，用于生成更符合您需求的内容</p></html>"))
            .addVerticalGap(10)
            .addComponent(JBLabel("Commit Message 提示词:"))
            .addComponent(createScrollPane(commitMessagePromptArea, 150))
            .addVerticalGap(10)
            .addComponent(JBLabel("日总结提示词:"))
            .addComponent(createScrollPane(dailySummaryPromptArea, 150))
            .addVerticalGap(10)
            .addComponent(JBLabel("周总结提示词:"))
            .addComponent(createScrollPane(weeklySummaryPromptArea, 150))
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
                commitMessagePromptArea.text != settings.commitMessagePrompt ||
                dailySummaryPromptArea.text != settings.dailySummaryPrompt ||
                weeklySummaryPromptArea.text != settings.weeklySummaryPrompt ||
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

        settings.commitMessagePrompt = commitMessagePromptArea.text
        settings.dailySummaryPrompt = dailySummaryPromptArea.text
        settings.weeklySummaryPrompt = weeklySummaryPromptArea.text

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

        commitMessagePromptArea.text = settings.commitMessagePrompt
        dailySummaryPromptArea.text = settings.dailySummaryPrompt
        weeklySummaryPromptArea.text = settings.weeklySummaryPrompt

        enableDailyCheckBox.isSelected = settings.enableDailyCommitCheck
        checkWorkdaysOnlyBox.isSelected = settings.checkWorkdaysOnly
        commitCheckTimeField.text = settings.commitCheckTime

        dailySummaryPathField.text = settings.dailySummaryPath
        weeklySummaryPathField.text = settings.weeklySummaryPath

        weeklyReportDayCombo.selectedItem = settings.weeklyReportDay
        weeklyReportTimeField.text = settings.weeklyReportTime
    }

    /**
     * 测试 API 连接
     */
    private fun testApiConnection() {
        val apiKey = apiKeyField.password.concatToString()
        val apiBaseUrl = apiBaseUrlField.text
        val modelName = modelNameField.text

        if (apiKey.isEmpty() || apiBaseUrl.isEmpty() || modelName.isEmpty()) {
            testStatusLabel.text = "<html><span style='color:red;'>⚠ 请填写完整的 API 配置</span></html>"
            return
        }

        testApiButton.isEnabled = false
        testStatusLabel.text = "<html><span style='color:blue;'>⏳ 测试中...</span></html>"

        // 在后台线程执行测试
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                val client = OpenAICompatibleClient(
                    baseUrl = apiBaseUrl,
                    apiKey = apiKey,
                    model = modelName
                )

                // 使用 runBlocking 调用 suspend 函数
                runBlocking {
                    client.testConnection()
                }

                // 切换到 UI 线程更新界面
                SwingUtilities.invokeLater {
                    testStatusLabel.text = "<html><span style='color:green;'>✓ API 连接成功！</span></html>"
                    testApiButton.isEnabled = true
                }
            } catch (e: Exception) {
                SwingUtilities.invokeLater {
                    val errorMsg = when {
                        e.message != null -> e.message!!
                        else -> "连接失败"
                    }
                    testStatusLabel.text = "<html><span style='color:red;'>✗ $errorMsg</span></html>"
                    testApiButton.isEnabled = true
                }
            }
        }
    }

    /**
     * 创建带滚动条的文本区域
     */
    private fun createScrollPane(textArea: JBTextArea, height: Int): JBScrollPane {
        val scrollPane = JBScrollPane(textArea)
        scrollPane.preferredSize = Dimension(600, height)
        return scrollPane
    }
}
