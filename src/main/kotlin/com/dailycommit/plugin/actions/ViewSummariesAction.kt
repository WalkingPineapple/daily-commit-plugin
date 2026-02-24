package com.dailycommit.plugin.actions

import com.dailycommit.plugin.summary.SummaryStorage
import com.dailycommit.plugin.utils.FileUtils
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

/**
 * 查看总结历史 Action
 */
class ViewSummariesAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val storage = SummaryStorage(project)

        val dailySummaries = storage.getAllDailySummaries()
        val weeklySummaries = storage.getAllWeeklySummaries()

        if (dailySummaries.isEmpty() && weeklySummaries.isEmpty()) {
            Messages.showInfoMessage(
                project,
                "还没有生成任何总结。\n请先提交代码并生成日总结或周总结。",
                "无总结文件"
            )
            return
        }

        // 构建选择列表
        val options = mutableListOf<String>()
        val fileMap = mutableMapOf<String, File>()

        if (dailySummaries.isNotEmpty()) {
            options.add("=== 日总结 ===")
            dailySummaries.take(10).forEach { file ->
                val fileName = FileUtils.getFileNameWithoutExtension(file)
                val displayName = "日总结: $fileName"
                options.add(displayName)
                fileMap[displayName] = file
            }
        }

        if (weeklySummaries.isNotEmpty()) {
            options.add("")
            options.add("=== 周总结 ===")
            weeklySummaries.take(10).forEach { file ->
                val fileName = FileUtils.getFileNameWithoutExtension(file)
                val displayName = "周总结: $fileName"
                options.add(displayName)
                fileMap[displayName] = file
            }
        }

        // 显示选择对话框
        val choice = Messages.showChooseDialog(
            project,
            "选择要查看的总结文件：",
            "查看工作总结",
            null,
            options.toTypedArray(),
            options[0]
        )

        if (choice >= 0 && choice < options.size) {
            val selectedOption = options[choice]
            val selectedFile = fileMap[selectedOption]

            selectedFile?.let { file ->
                openFileInEditor(project, file)
            }
        }
    }

    /**
     * 在编辑器中打开文件
     */
    private fun openFileInEditor(project: com.intellij.openapi.project.Project, file: File) {
        val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(file.absolutePath)
        virtualFile?.let {
            FileEditorManager.getInstance(project).openFile(it, true)
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }
}
