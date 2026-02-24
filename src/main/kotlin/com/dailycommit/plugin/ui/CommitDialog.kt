package com.dailycommit.plugin.ui

import com.dailycommit.plugin.config.PluginSettings
import com.dailycommit.plugin.git.DiffAnalyzer
import com.dailycommit.plugin.git.GitService
import com.dailycommit.plugin.llm.OpenAICompatibleClient
import com.dailycommit.plugin.llm.PromptBuilder
import com.dailycommit.plugin.utils.NotificationUtils
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import kotlinx.coroutines.*
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*

/**
 * 普通提交对话框（带 AI 辅助）
 */
class CommitDialog(
    private val project: Project,
    private val onCommitSuccess: () -> Unit = {}
) : DialogWrapper(project, true) {

    private val gitService = GitService(project)
    private val diffAnalyzer = DiffAnalyzer(project)
    private val settings = PluginSettings.getInstance().state

    private val commitMessageArea = JBTextArea(10, 60)
    private val generateButton = JButton("AI 生成")
    private val loadingLabel = JBLabel("生成中...")

    init {
        title = "提交代码"
        init()
        setupUI()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout(10, 10))
        panel.preferredSize = Dimension(700, 450)

        // 顶部改动摘要
        val topPanel = JPanel(BorderLayout())
        topPanel.add(JBLabel("当前改动："), BorderLayout.NORTH)

        val changesSummary = diffAnalyzer.getUncommittedChangesSummary()
        val changesArea = JBTextArea(changesSummary, 6, 60)
        changesArea.isEditable = false
        topPanel.add(JBScrollPane(changesArea), BorderLayout.CENTER)

        panel.add(topPanel, BorderLayout.NORTH)

        // 中间 commit message 区域
        val middlePanel = JPanel(BorderLayout(5, 5))

        val headerPanel = JPanel(BorderLayout())
        headerPanel.add(JBLabel("Commit Message:"), BorderLayout.WEST)

        val buttonPanel = JPanel()
        loadingLabel.isVisible = false
        buttonPanel.add(generateButton)
        buttonPanel.add(loadingLabel)
        headerPanel.add(buttonPanel, BorderLayout.EAST)

        middlePanel.add(headerPanel, BorderLayout.NORTH)

        commitMessageArea.lineWrap = true
        commitMessageArea.wrapStyleWord = true
        middlePanel.add(JBScrollPane(commitMessageArea), BorderLayout.CENTER)

        panel.add(middlePanel, BorderLayout.CENTER)

        // 绑定按钮事件
        generateButton.addActionListener {
            generateCommitMessageWithAI()
        }

        return panel
    }

    private fun setupUI() {
        if (!settings.apiConfigured || settings.apiKey.isEmpty()) {
            generateButton.isEnabled = false
            generateButton.toolTipText = "请先在设置中配置 LLM API"
        }

        setOKButtonText("提交并生成日总结")
        setCancelButtonText("取消")
    }

    private fun generateCommitMessageWithAI() {
        generateButton.isEnabled = false
        loadingLabel.isVisible = true

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OpenAICompatibleClient(
                    baseUrl = settings.apiBaseUrl,
                    apiKey = settings.apiKey,
                    model = settings.modelName
                )

                val changesSummary = diffAnalyzer.getChangesDescription()
                val diff = diffAnalyzer.getDetailedDiff()
                val (systemMessage, userMessage) = PromptBuilder.buildCommitMessagePrompt(changesSummary, diff)

                val generatedMessage = client.generateText(systemMessage, userMessage)

                withContext(Dispatchers.Main) {
                    commitMessageArea.text = generatedMessage
                    loadingLabel.isVisible = false
                    generateButton.isEnabled = true
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadingLabel.isVisible = false
                    generateButton.isEnabled = true
                    NotificationUtils.showLLMError(project, e.message ?: "Unknown error")
                }
            }
        }
    }

    override fun doOKAction() {
        val message = commitMessageArea.text.trim()

        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(
                contentPane,
                "请输入 commit message！",
                "提示",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }

        val success = gitService.commitAllChanges(message)

        if (success) {
            NotificationUtils.showCommitSuccess(project, "代码已成功提交")
            onCommitSuccess()
            super.doOKAction()
        } else {
            JOptionPane.showMessageDialog(
                contentPane,
                "提交失败，请检查 Git 状态",
                "错误",
                JOptionPane.ERROR_MESSAGE
            )
        }
    }
}
