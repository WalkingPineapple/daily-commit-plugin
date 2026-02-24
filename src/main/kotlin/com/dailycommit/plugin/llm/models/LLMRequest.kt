package com.dailycommit.plugin.llm.models

import kotlinx.serialization.Serializable

/**
 * LLM 请求数据模型
 */
@Serializable
data class LLMRequest(
    val model: String,
    val messages: List<Message>,
    val temperature: Double = 0.7,
    val max_tokens: Int? = null,
    val stream: Boolean = false
)

@Serializable
data class Message(
    val role: String,
    val content: String
)
