package com.dailycommit.plugin.ui

import com.dailycommit.plugin.config.PluginSettings
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * 首次运行配置向导
 */
class FirstRunWizard(private val project: Project) : DialogWrapper(project, true) {

    init {
        title = "欢迎使用 Daily Commit Manager"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout(15, 15))
        panel.preferredSize = Dimension(500, 300)

        val welcomeText = """
            <html>
            <h2>欢迎使用 Daily Commit Manager！</h2>
            <br>
            <p>这是一个帮助您管理每日代码提交和生成工作总结的插件。</p>
            <br>
            <p><b>主要功能：</b></p>
            <ul>
                <li>每天强制提交代码（确保工作记录完整）</li>
                <li>使用 AI 自动生成 commit message</li>
                <li>自动生成每日工作总结</li>
                <li>每周四自动生成周工作总结</li>
            </ul>
            <br>
            <p><b>在开始使用前，请先配置 LLM API：</b></p>
            <ul>
                <li>支持 OpenAI、DeepSeek、通义千问、文心一言、智谱GLM 等</li>
                <li>需要提供 API Key、Base URL 和模型名称</li>
            </ul>
            <br>
            <p>点击下方按钮前往配置页面。</p>
            </html>
        """.trimIndent()

        val label = JBLabel(welcomeText)
        panel.add(label, BorderLayout.CENTER)

        val buttonPanel = JPanel()
        val configButton = JButton("前往配置")
        configButton.addActionListener {
            ShowSettingsUtil.getInstance().showSettingsDialog(
                project,
                "Daily Commit Manager"
            )
            close(OK_EXIT_CODE)
        }
        buttonPanel.add(configButton)
        panel.add(buttonPanel, BorderLayout.SOUTH)

        return panel
    }

    override fun createActions() = arrayOf(cancelAction)

    override fun getCancelAction() = super.getCancelAction().apply {
        putValue(NAME, "稍后配置")
    }

    companion object {
        /**
         * 检查并显示首次运行向导
         */
        fun showIfFirstRun(project: Project) {
            val settings = PluginSettings.getInstance().state
            if (settings.isFirstRun) {
                val wizard = FirstRunWizard(project)
                wizard.show()
                settings.isFirstRun = false
            }
        }
    }
}
