package com.dailycommit.plugin.git

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.ChangeListManager
import git4idea.GitUtil
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitLineHandler
import git4idea.history.GitHistoryUtils
import git4idea.repo.GitRepository
import java.time.LocalDate
import java.time.ZoneId

/**
 * Git 操作服务
 */
class GitService(private val project: Project) {

    private val git = Git.getInstance()

    /**
     * 获取项目的 Git 仓库
     */
    fun getRepository(): GitRepository? {
        return GitUtil.getRepositoryManager(project).repositories.firstOrNull()
    }

    /**
     * 检查是否是 Git 项目
     */
    fun isGitRepository(): Boolean {
        return getRepository() != null
    }

    /**
     * 获取当前 Git 用户名
     */
    fun getCurrentUser(): String {
        val repository = getRepository() ?: return "unknown"
        val handler = GitLineHandler(project, repository.root, GitCommand.CONFIG)
        handler.addParameters("user.name")
        val result = git.runCommand(handler)
        return result.outputAsJoinedString.trim().ifEmpty { "unknown" }
    }

    /**
     * 获取指定日期的提交列表
     */
    fun getCommitsForDate(date: LocalDate): List<String> {
        val repository = getRepository() ?: return emptyList()

        try {
            val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
            val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond()

            val handler = GitLineHandler(project, repository.root, GitCommand.LOG)
            handler.addParameters(
                "--pretty=format:%H|%s|%an|%at",
                "--since=$startOfDay",
                "--until=$endOfDay"
            )

            val result = git.runCommand(handler)
            if (result.success()) {
                return result.output.mapNotNull { line ->
                    val parts = line.split("|")
                    if (parts.size >= 2) parts[1] else null // 返回 commit message
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return emptyList()
    }

    /**
     * 获取指定日期范围的提交列表
     */
    fun getCommitsBetweenDates(startDate: LocalDate, endDate: LocalDate): List<String> {
        val repository = getRepository() ?: return emptyList()

        try {
            val startEpoch = startDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
            val endEpoch = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond()

            val handler = GitLineHandler(project, repository.root, GitCommand.LOG)
            handler.addParameters(
                "--pretty=format:%s",
                "--since=$startEpoch",
                "--until=$endEpoch"
            )

            val result = git.runCommand(handler)
            if (result.success()) {
                return result.output.filter { it.isNotBlank() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return emptyList()
    }

    /**
     * 获取未暂存的改动文件列表
     */
    fun getUnstagedChanges(): List<String> {
        val changeListManager = ChangeListManager.getInstance(project)
        return changeListManager.allChanges.map { it.virtualFile?.path ?: "" }.filter { it.isNotEmpty() }
    }

    /**
     * 获取未提交的改动的 diff
     */
    fun getUncommittedDiff(): String {
        val repository = getRepository() ?: return ""

        try {
            val handler = GitLineHandler(project, repository.root, GitCommand.DIFF)
            handler.addParameters("HEAD")

            val result = git.runCommand(handler)
            if (result.success()) {
                return result.output.joinToString("\n")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

    /**
     * 提交所有改动
     */
    fun commitAllChanges(message: String): Boolean {
        val repository = getRepository() ?: return false

        try {
            // 先 add 所有改动
            val addHandler = GitLineHandler(project, repository.root, GitCommand.ADD)
            addHandler.addParameters(".")
            val addResult = git.runCommand(addHandler)

            if (!addResult.success()) {
                return false
            }

            // 然后 commit
            val commitHandler = GitLineHandler(project, repository.root, GitCommand.COMMIT)
            commitHandler.addParameters("-m", message)
            val commitResult = git.runCommand(commitHandler)

            return commitResult.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * 获取最后一次提交的时间
     */
    fun getLastCommitDate(): LocalDate? {
        val repository = getRepository() ?: return null

        try {
            val handler = GitLineHandler(project, repository.root, GitCommand.LOG)
            handler.addParameters("--pretty=format:%at", "-n", "1")

            val result = git.runCommand(handler)
            if (result.success() && result.output.isNotEmpty()) {
                val timestamp = result.output[0].toLongOrNull() ?: return null
                return LocalDate.ofEpochDay(timestamp / 86400) // 86400 = seconds per day
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    /**
     * 检查是否有未提交的改动
     */
    fun hasUncommittedChanges(): Boolean {
        val changeListManager = ChangeListManager.getInstance(project)
        return changeListManager.allChanges.isNotEmpty()
    }
}
