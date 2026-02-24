package com.dailycommit.plugin.config

/**
 * LLM 提供商枚举
 */
enum class LLMProvider(val displayName: String, val defaultBaseUrl: String) {
    OPENAI("OpenAI", "https://api.openai.com/v1"),
    DEEPSEEK("DeepSeek", "https://api.deepseek.com/v1"),
    ZHIPU("智谱AI (GLM)", "https://open.bigmodel.cn/api/paas/v4"),
    QWEN("通义千问", "https://dashscope.aliyuncs.com/compatible-mode/v1"),
    ERNIE("文心一言", "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop"),
    CUSTOM("自定义", "");

    companion object {
        fun fromDisplayName(name: String): LLMProvider {
            return values().find { it.displayName == name } ?: CUSTOM
        }
    }
}
