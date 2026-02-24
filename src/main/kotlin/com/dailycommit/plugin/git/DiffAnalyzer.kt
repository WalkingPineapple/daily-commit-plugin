package com.dailycommit.plugin.git

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ChangeListManager

/**
 * 差异分析器 - 分析代码改动
 */
class DiffAnalyzer(private val project: Project) {

    private val gitService = GitService(project)

    /**
     * 获取当前未提交的改动摘要
     */
    fun getUncommittedChangesSummary(): String {
        val changeListManager = ChangeListManager.getInstance(project)
        val changes = changeListManager.allChanges

        if (changes.isEmpty()) {
            return "没有未提交的改动。"
        }

        val summary = StringBuilder()
        summary.append("当前改动文件：\n")

        val groupedChanges = changes.groupBy { getChangeType(it) }

        groupedChanges.forEach { (type, changeList) ->
            summary.append("\n$type:\n")
            changeList.forEach { change ->
                val filePath = change.virtualFile?.path ?: "未知文件"
                val fileName = filePath.substringAfterLast("/")
                summary.append("  - $fileName\n")
            }
        }

        return summary.toString()
    }

    /**
     * 获取改动的详细 diff
     */
    fun getDetailedDiff(): String {
        return gitService.getUncommittedDiff()
    }

    /**
     * 获取改动的简短描述（用于生成 commit message）
     */
    fun getChangesDescription(): String {
        val changeListManager = ChangeListManager.getInstance(project)
        val changes = changeListManager.allChanges

        if (changes.isEmpty()) {
            return "No changes"
        }

        val summary = StringBuilder()
        val groupedChanges = changes.groupBy { getChangeType(it) }

        val descriptions = mutableListOf<String>()

        groupedChanges.forEach { (type, changeList) ->
            val fileNames = changeList.mapNotNull { it.virtualFile?.name }.take(3)
            if (fileNames.isNotEmpty()) {
                val filesList = fileNames.joinToString(", ")
                descriptions.add("$type: $filesList" + if (changeList.size > 3) " and ${changeList.size - 3} more" else "")
            }
        }

        return descriptions.joinToString("; ")
    }

    /**
     * 获取改动文件列表
     */
    fun getChangedFiles(): List<String> {
        val changeListManager = ChangeListManager.getInstance(project)
        return changeListManager.allChanges.mapNotNull { it.virtualFile?.path }
    }

    /**
     * 统计改动信息
     */
    fun getChangeStatistics(): Map<String, Int> {
        val changeListManager = ChangeListManager.getInstance(project)
        val changes = changeListManager.allChanges

        val stats = mutableMapOf(
            "新增文件" to 0,
            "修改文件" to 0,
            "删除文件" to 0,
            "总计" to changes.size
        )

        changes.forEach { change ->
            when (getChangeType(change)) {
                "新增" -> stats["新增文件"] = stats["新增文件"]!! + 1
                "修改" -> stats["修改文件"] = stats["修改文件"]!! + 1
                "删除" -> stats["删除文件"] = stats["删除文件"]!! + 1
            }
        }

        return stats
    }

    /**
     * 获取改动类型
     */
    private fun getChangeType(change: Change): String {
        return when (change.type) {
            Change.Type.NEW -> "新增"
            Change.Type.DELETED -> "删除"
            Change.Type.MODIFICATION -> "修改"
            Change.Type.MOVED -> "移动"
            else -> "其他"
        }
    }

    /**
     * 判断是否有实质性改动（排除空白文件等）
     */
    fun hasSubstantialChanges(): Boolean {
        val diff = getDetailedDiff()
        // 简单判断：diff 内容长度大于 100 字符
        return diff.length > 100
    }
}
