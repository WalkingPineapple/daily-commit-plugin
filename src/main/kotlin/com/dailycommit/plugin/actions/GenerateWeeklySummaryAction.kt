package com.dailycommit.plugin.actions

import com.dailycommit.plugin.summary.WeeklySummaryGenerator
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * 生成周总结 Action
 */
class GenerateWeeklySummaryAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val generator = WeeklySummaryGenerator(project)
        generator.generateWeeklySummary(showPreview = true)
    }

    override fun update(e: AnActionEvent) {
        // 只在有项目时启用
        e.presentation.isEnabled = e.project != null
    }
}
