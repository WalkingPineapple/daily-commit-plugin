package com.dailycommit.plugin.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * 总结预览和编辑对话框
 */
class SummaryPreviewDialog(
    private val project: Project,
    private val title: String,
    private val summary: String,
    private val onSave: (String) -> Unit
) : DialogWrapper(project, true) {

    private val summaryArea = JBTextArea(summary, 20, 70)

    init {
        this.title = title
        init()
        setOKButtonText("保存")
        setCancelButtonText("取消")
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout(10, 10))
        panel.preferredSize = Dimension(800, 500)

        // 顶部提示
        val topLabel = JBLabel("<html><b>$title</b><br><i>您可以编辑以下内容后保存</i></html>")
        panel.add(topLabel, BorderLayout.NORTH)

        // 中间编辑区
        summaryArea.lineWrap = true
        summaryArea.wrapStyleWord = true
        panel.add(JBScrollPane(summaryArea), BorderLayout.CENTER)

        return panel
    }

    override fun doOKAction() {
        val editedSummary = summaryArea.text.trim()
        if (editedSummary.isNotEmpty()) {
            onSave(editedSummary)
        }
        super.doOKAction()
    }

    companion object {
        /**
         * 显示日总结预览对话框
         */
        fun showDailySummaryPreview(
            project: Project,
            summary: String,
            onSave: (String) -> Unit
        ) {
            val dialog = SummaryPreviewDialog(
                project,
                "日总结预览",
                summary,
                onSave
            )
            dialog.show()
        }

        /**
         * 显示周总结预览对话框
         */
        fun showWeeklySummaryPreview(
            project: Project,
            summary: String,
            onSave: (String) -> Unit
        ) {
            val dialog = SummaryPreviewDialog(
                project,
                "周总结预览",
                summary,
                onSave
            )
            dialog.show()
        }
    }
}
