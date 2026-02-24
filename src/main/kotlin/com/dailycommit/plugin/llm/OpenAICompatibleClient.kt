package com.dailycommit.plugin.llm

import com.dailycommit.plugin.llm.models.LLMRequest
import com.dailycommit.plugin.llm.models.LLMResponse
import com.dailycommit.plugin.llm.models.Message
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * OpenAI 兼容的 LLM 客户端实现
 * 支持 OpenAI、DeepSeek、智谱等使用 OpenAI 兼容 API 的模型
 */
class OpenAICompatibleClient(
    private val baseUrl: String,
    private val apiKey: String,
    private val model: String
) : LLMClient {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 60000
            connectTimeoutMillis = 10000
            socketTimeoutMillis = 60000
        }
    }

    override suspend fun generateText(prompt: String): String {
        return generateText("You are a helpful assistant.", prompt)
    }

    override suspend fun generateText(systemMessage: String, userMessage: String): String {
        try {
            val endpoint = buildEndpoint()
            val request = LLMRequest(
                model = model,
                messages = listOf(
                    Message(role = "system", content = systemMessage),
                    Message(role = "user", content = userMessage)
                ),
                temperature = 0.7
            )

            val response: LLMResponse = client.post(endpoint) {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            return response.choices.firstOrNull()?.message?.content
                ?: throw Exception("Empty response from LLM")

        } catch (e: Exception) {
            throw Exception("LLM API 调用失败: ${e.message}", e)
        }
    }

    override suspend fun testConnection(): Boolean {
        return try {
            val testPrompt = "Hello"
            generateText("You are a helpful assistant.", testPrompt)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 构建 API endpoint
     */
    private fun buildEndpoint(): String {
        val normalizedBaseUrl = baseUrl.trimEnd('/')
        return if (normalizedBaseUrl.endsWith("/chat/completions")) {
            normalizedBaseUrl
        } else {
            "$normalizedBaseUrl/chat/completions"
        }
    }

    /**
     * 关闭客户端
     */
    fun close() {
        client.close()
    }
}
