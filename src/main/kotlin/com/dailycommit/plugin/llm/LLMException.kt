package com.dailycommit.plugin.llm

/**
 * LLM 异常类型
 */
sealed class LLMException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /**
     * 连接超时异常
     */
    class ConnectionTimeoutException(message: String = "连接超时，请检查网络连接或API地址是否正确", cause: Throwable? = null) :
        LLMException(message, cause)

    /**
     * 请求超时异常
     */
    class RequestTimeoutException(message: String = "请求超时，API响应时间过长", cause: Throwable? = null) :
        LLMException(message, cause)

    /**
     * 认证失败异常
     */
    class AuthenticationException(message: String = "API Key 无效或已过期，请检查配置", cause: Throwable? = null) :
        LLMException(message, cause)

    /**
     * API 错误异常
     */
    class APIException(val statusCode: Int, message: String, cause: Throwable? = null) :
        LLMException("API 错误 (${statusCode}): $message", cause)

    /**
     * 网络连接异常
     */
    class NetworkException(message: String = "网络连接失败，请检查网络设置或API地址", cause: Throwable? = null) :
        LLMException(message, cause)

    /**
     * 响应解析异常
     */
    class ParseException(message: String = "响应数据解析失败", cause: Throwable? = null) :
        LLMException(message, cause)

    /**
     * 配额超限异常
     */
    class QuotaExceededException(message: String = "API 配额已用完，请检查账户余额或升级套餐", cause: Throwable? = null) :
        LLMException(message, cause)

    /**
     * 未知错误
     */
    class UnknownException(message: String, cause: Throwable? = null) :
        LLMException(message, cause)
}
