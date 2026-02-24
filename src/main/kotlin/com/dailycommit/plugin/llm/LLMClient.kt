package com.dailycommit.plugin.llm

/**
 * LLM 客户端接口
 */
interface LLMClient {
    /**
     * 生成文本
     * @param prompt 提示词
     * @return 生成的文本
     */
    suspend fun generateText(prompt: String): String

    /**
     * 生成文本（带系统消息）
     * @param systemMessage 系统消息
     * @param userMessage 用户消息
     * @return 生成的文本
     */
    suspend fun generateText(systemMessage: String, userMessage: String): String

    /**
     * 测试 API 连接
     * @return 是否连接成功
     */
    suspend fun testConnection(): Boolean
}
