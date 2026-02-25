package com.dailycommit.plugin.llm

import com.dailycommit.plugin.llm.models.LLMRequest
import com.dailycommit.plugin.llm.models.LLMResponse
import com.dailycommit.plugin.llm.models.Message
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.SerializationException
import java.net.UnknownHostException
import java.net.ConnectException
import java.net.SocketTimeoutException

/**
 * OpenAI 兼容的 LLM 客户端实现
 * 支持 OpenAI、DeepSeek、智谱等使用 OpenAI 兼容 API 的模型
 */
class OpenAICompatibleClient(
    private val baseUrl: String,
    private val apiKey: String,
    private val model: String
) : LLMClient {

    private val client = HttpClient(Java) {
        // 禁用默认的响应验证，避免日志相关的类加载问题
        expectSuccess = false

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

            val response: HttpResponse = client.post(endpoint) {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            // 检查 HTTP 状态码
            when (response.status.value) {
                in 200..299 -> {
                    // 成功响应
                    try {
                        val llmResponse: LLMResponse = response.body()
                        return llmResponse.choices.firstOrNull()?.message?.content
                            ?: throw LLMException.ParseException("API 返回了空内容")
                    } catch (e: SerializationException) {
                        throw LLMException.ParseException("无法解析 API 响应数据", e)
                    }
                }
                401, 403 -> {
                    throw LLMException.AuthenticationException()
                }
                429 -> {
                    throw LLMException.QuotaExceededException()
                }
                in 400..499 -> {
                    val errorBody = response.bodyAsText()
                    throw LLMException.APIException(response.status.value, "请求参数错误: $errorBody")
                }
                in 500..599 -> {
                    throw LLMException.APIException(response.status.value, "服务器错误，请稍后重试")
                }
                else -> {
                    throw LLMException.APIException(response.status.value, "未知的 HTTP 状态码")
                }
            }

        } catch (e: LLMException) {
            // 直接抛出自定义异常
            throw e
        } catch (e: HttpRequestTimeoutException) {
            throw LLMException.RequestTimeoutException(cause = e)
        } catch (e: SocketTimeoutException) {
            // SocketTimeoutException 可能是连接超时或请求超时
            throw LLMException.ConnectionTimeoutException("连接或请求超时，API响应时间过长", e)
        } catch (e: UnknownHostException) {
            throw LLMException.NetworkException("无法解析 API 地址：${e.message}，请检查网络或 API Base URL 配置", e)
        } catch (e: ConnectException) {
            throw LLMException.NetworkException("无法连接到 API 服务器，请检查网络连接和 API 地址", e)
        } catch (e: Exception) {
            throw LLMException.UnknownException("发生未知错误: ${e.message}", e)
        }
    }

    override suspend fun testConnection(): Boolean {
        return try {
            val testPrompt = "Hello"
            generateText("You are a helpful assistant.", testPrompt)
            true
        } catch (e: LLMException) {
            // 将详细的异常信息传递给调用方
            throw e
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
