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
 * 阻止编辑器的强制提交对话框
 */
class BlockingDialog(
    private val project: Project
) : DialogWrapper(project, false) {

    private val gitService = GitService(project)
    private val diffAnalyzer = DiffAnalyzer(project)
    private val settings = PluginSettings.getInstance().state

    private val commitMessageArea = JBTextArea(8, 50)
    private val changesLabel = JBLabel()
    private val generateButton = JButton("AI 生成 Commit Message")
    private val loadingLabel = JBLabel("正在生成...")

    init {
        title = "强制提交 - 昨日未提交代码"
        init()
        setupUI()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout(10, 10))
        panel.preferredSize = Dimension(600, 400)

        // 顶部说明
        val topPanel = JPanel(BorderLayout())
        topPanel.add(
            JBLabel("<html><b>检测到昨天没有提交代码！</b><br>" +
                    "请先提交代码后才能继续工作。<br><br>" +
                    "当前改动：</html>"),
            BorderLayout.NORTH
        )

        // 改动摘要
        val changesSummary = diffAnalyzer.getUncommittedChangesSummary()
        changesLabel.text = "<html>${changesSummary.replace("\n", "<br>")}</html>"
        topPanel.add(JBScrollPane(changesLabel), BorderLayout.CENTER)

        panel.add(topPanel, BorderLayout.NORTH)

        // 中间 commit message 输入区
        val middlePanel = JPanel(BorderLayout(5, 5))
        middlePanel.add(JBLabel("Commit Message:"), BorderLayout.NORTH)

        commitMessageArea.lineWrap = true
        commitMessageArea.wrapStyleWord = true
        middlePanel.add(JBScrollPane(commitMessageArea), BorderLayout.CENTER)

        // AI 生成按钮
        val buttonPanel = JPanel()
        loadingLabel.isVisible = false
        buttonPanel.add(generateButton)
        buttonPanel.add(loadingLabel)
        middlePanel.add(buttonPanel, BorderLayout.SOUTH)

        panel.add(middlePanel, BorderLayout.CENTER)

        // 绑定 AI 生成按钮事件
        generateButton.addActionListener {
            generateCommitMessageWithAI()
        }

        // 底部提示
        val bottomPanel = JPanel()
        bottomPanel.add(JBLabel("<html><i>提示：您也可以手动输入 commit message</i></html>"))
        panel.add(bottomPanel, BorderLayout.SOUTH)

        return panel
    }

    /**
     * 设置 UI
     */
    private fun setupUI() {
        // 如果没有配置 API，禁用 AI 生成按钮
        if (!settings.apiConfigured || settings.apiKey.isEmpty()) {
            generateButton.isEnabled = false
            generateButton.text = "请先配置 LLM API"
        }

        // 设置确定按钮文本
        setOKButtonText("提交")
        setCancelButtonText("取消（无法继续工作）")

        // 取消按钮不关闭对话框
        myCancelAction.isEnabled = false
    }

    /**
     * 使用 AI 生成 commit message
     */
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

    /**
     * 点击确定按钮
     */
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

        // 执行提交
        val success = gitService.commitAllChanges(message)

        if (success) {
            NotificationUtils.showCommitSuccess(project, "代码已成功提交")
            super.doOKAction()
        } else {
            JOptionPane.showMessageDialog(
                contentPane,
                "提交失败，请检查 Git 配置",
                "错误",
                JOptionPane.ERROR_MESSAGE
            )
        }
    }
}
