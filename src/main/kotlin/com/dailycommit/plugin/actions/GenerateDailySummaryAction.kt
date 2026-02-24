package com.dailycommit.plugin.actions

import com.dailycommit.plugin.summary.DailySummaryGenerator
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * 生成日总结 Action
 */
class GenerateDailySummaryAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val generator = DailySummaryGenerator(project)
        generator.generateDailySummary(showPreview = true)
    }

    override fun update(e: AnActionEvent) {
        // 只在有项目时启用
        e.presentation.isEnabled = e.project != null
    }
}
