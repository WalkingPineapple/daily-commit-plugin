package com.dailycommit.plugin.llm.models

import kotlinx.serialization.Serializable

/**
 * LLM 响应数据模型
 */
@Serializable
data class LLMResponse(
    val id: String? = null,
    val choices: List<Choice>,
    val usage: Usage? = null
)

@Serializable
data class Choice(
    val index: Int = 0,
    val message: Message,
    val finish_reason: String? = null
)

@Serializable
data class Usage(
    val prompt_tokens: Int = 0,
    val completion_tokens: Int = 0,
    val total_tokens: Int = 0
)
