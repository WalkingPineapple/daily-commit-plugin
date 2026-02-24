package com.dailycommit.plugin.utils

import com.intellij.openapi.project.Project
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

/**
 * 文件操作工具类
 */
object FileUtils {

    /**
     * 确保目录存在，不存在则创建
     */
    fun ensureDirectoryExists(path: String): File {
        val dir = File(path)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * 确保项目相对目录存在
     */
    fun ensureProjectDirectoryExists(project: Project, relativePath: String): File {
        val basePath = project.basePath ?: return File(relativePath)
        val fullPath = Paths.get(basePath, relativePath).toString()
        return ensureDirectoryExists(fullPath)
    }

    /**
     * 保存文本到文件
     */
    fun saveTextToFile(filePath: String, content: String) {
        val path = Paths.get(filePath)
        Files.createDirectories(path.parent)
        Files.write(
            path,
            content.toByteArray(),
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        )
    }

    /**
     * 保存文本到项目相对路径
     */
    fun saveTextToProjectFile(project: Project, relativePath: String, fileName: String, content: String) {
        val basePath = project.basePath ?: return
        val fullPath = Paths.get(basePath, relativePath, fileName).toString()
        saveTextToFile(fullPath, content)
    }

    /**
     * 读取文件内容
     */
    fun readFileContent(filePath: String): String {
        val file = File(filePath)
        return if (file.exists()) {
            file.readText()
        } else {
            ""
        }
    }

    /**
     * 检查文件是否存在
     */
    fun fileExists(filePath: String): Boolean {
        return File(filePath).exists()
    }

    /**
     * 获取项目文件的完整路径
     */
    fun getProjectFilePath(project: Project, relativePath: String, fileName: String): String {
        val basePath = project.basePath ?: return fileName
        return Paths.get(basePath, relativePath, fileName).toString()
    }

    /**
     * 列出目录下所有文件
     */
    fun listFiles(directoryPath: String, extension: String = ""): List<File> {
        val dir = File(directoryPath)
        if (!dir.exists() || !dir.isDirectory) {
            return emptyList()
        }

        return dir.listFiles()?.filter { file ->
            file.isFile && (extension.isEmpty() || file.extension == extension)
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    /**
     * 列出项目目录下的文件
     */
    fun listProjectFiles(project: Project, relativePath: String, extension: String = ""): List<File> {
        val basePath = project.basePath ?: return emptyList()
        val fullPath = Paths.get(basePath, relativePath).toString()
        return listFiles(fullPath, extension)
    }

    /**
     * 删除文件
     */
    fun deleteFile(filePath: String): Boolean {
        val file = File(filePath)
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }

    /**
     * 获取文件名（不含扩展名）
     */
    fun getFileNameWithoutExtension(file: File): String {
        return file.nameWithoutExtension
    }
}
