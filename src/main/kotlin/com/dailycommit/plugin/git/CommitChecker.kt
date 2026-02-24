package com.dailycommit.plugin.git

import com.dailycommit.plugin.config.PluginSettings
import com.dailycommit.plugin.utils.DateUtils
import com.intellij.openapi.project.Project
import java.time.LocalDate

/**
 * 提交检查器
 */
class CommitChecker(private val project: Project) {

    private val gitService = GitService(project)
    private val settings = PluginSettings.getInstance().state

    /**
     * 检查昨天是否有提交
     */
    fun hasCommitYesterday(): Boolean {
        if (!gitService.isGitRepository()) {
            return true // 不是 Git 项目，不检查
        }

        val yesterday = DateUtils.getYesterday()

        // 如果配置了仅工作日检查，且昨天不是工作日，则跳过
        if (settings.checkWorkdaysOnly && !DateUtils.isWorkday(yesterday)) {
            return true
        }

        val commits = gitService.getCommitsForDate(yesterday)
        return commits.isNotEmpty()
    }

    /**
     * 检查今天是否有提交
     */
    fun hasCommitToday(): Boolean {
        if (!gitService.isGitRepository()) {
            return true
        }

        val today = LocalDate.now()
        val commits = gitService.getCommitsForDate(today)
        return commits.isNotEmpty()
    }

    /**
     * 检查是否需要强制提交
     * 规则：
     * 1. 如果未启用每日检查，返回 false
     * 2. 如果不是 Git 项目，返回 false
     * 3. 如果昨天有提交，返回 false
     * 4. 如果配置了仅工作日检查，且今天不是工作日，返回 false
     * 5. 其他情况返回 true
     */
    fun shouldForceCommit(): Boolean {
        if (!settings.enableDailyCommitCheck) {
            return false
        }

        if (!gitService.isGitRepository()) {
            return false
        }

        // 如果配置了仅工作日检查，且今天不是工作日，则不强制
        if (settings.checkWorkdaysOnly && !DateUtils.isWorkday()) {
            return false
        }

        // 检查昨天是否有提交
        return !hasCommitYesterday()
    }

    /**
     * 检查是否有未提交的改动
     */
    fun hasUncommittedChanges(): Boolean {
        return gitService.hasUncommittedChanges()
    }

    /**
     * 获取最后一次提交的日期
     */
    fun getLastCommitDate(): LocalDate? {
        return gitService.getLastCommitDate()
    }

    /**
     * 获取昨天的提交列表
     */
    fun getYesterdayCommits(): List<String> {
        val yesterday = DateUtils.getYesterday()
        return gitService.getCommitsForDate(yesterday)
    }

    /**
     * 获取今天的提交列表
     */
    fun getTodayCommits(): List<String> {
        val today = LocalDate.now()
        return gitService.getCommitsForDate(today)
    }
}
