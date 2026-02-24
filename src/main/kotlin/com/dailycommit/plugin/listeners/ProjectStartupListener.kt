package com.dailycommit.plugin.listeners

import com.dailycommit.plugin.git.CommitChecker
import com.dailycommit.plugin.scheduler.WeeklySummaryScheduler
import com.dailycommit.plugin.summary.DailySummaryGenerator
import com.dailycommit.plugin.ui.BlockingDialog
import com.dailycommit.plugin.ui.FirstRunWizard
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

/**
 * 项目启动监听器
 * 在项目启动时执行以下检查：
 * 1. 首次运行向导
 * 2. 检查昨天是否有提交
 * 3. 如果没有，显示强制提交对话框
 * 4. 启动周总结调度器
 */
class ProjectStartupListener : ProjectActivity {

    override suspend fun execute(project: Project) {
        // 延迟执行，等待项目完全加载
        ApplicationManager.getApplication().invokeLater {
            // 1. 显示首次运行向导
            FirstRunWizard.showIfFirstRun(project)

            // 2. 检查是否需要强制提交
            val commitChecker = CommitChecker(project)
            if (commitChecker.shouldForceCommit()) {
                showBlockingDialog(project)
            }

            // 3. 启动周总结调度器
            val scheduler = WeeklySummaryScheduler(project)
            scheduler.start()
        }
    }

    /**
     * 显示阻止编辑的强制提交对话框
     */
    private fun showBlockingDialog(project: Project) {
        ApplicationManager.getApplication().invokeLater {
            val dialog = BlockingDialog(project)
            val result = dialog.showAndGet()

            if (result) {
                // 提交成功后，生成日总结
                val dailySummaryGenerator = DailySummaryGenerator(project)
                dailySummaryGenerator.generateDailySummary(showPreview = true)
            }
        }
    }
}
