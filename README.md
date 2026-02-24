# Daily Commit Manager - IDEA 插件

每日代码提交管理和工作总结自动生成工具

## 功能特性

### 核心功能

1. **每日强制提交**
   - 每天启动 IDE 时检查昨天是否有提交
   - 如果昨天未提交，阻止编辑器使用，强制用户提交代码
   - 可配置是否仅工作日检查

2. **AI 辅助 Commit Message**
   - 使用 LLM 自动分析代码改动
   - 自动生成规范的 commit message
   - 用户可编辑后再提交

3. **自动生成日总结** ⭐
   - 每次 commit 后自动触发
   - 基于当天所有 commit 和代码改动生成工作总结
   - 保存到 `.idea/daily-summaries/YYYY-MM-DD.txt`

4. **自动生成周总结** ⭐
   - 每周四（可配置）自动生成
   - 基于本周所有 commit message 生成周工作报告
   - 保存到 `.idea/weekly-summaries/YYYY-Wxx.txt`
   - 也可手动触发

### 支持的 LLM

- OpenAI (GPT-3.5/4)
- DeepSeek
- 智谱AI (GLM)
- 通义千问
- 文心一言
- 任何 OpenAI 兼容的 API

## 安装

### 从源码构建

```bash
cd daily-commit-plugin
./gradlew buildPlugin
```

构建完成后，插件 ZIP 文件位于 `build/distributions/`

### 安装到 IDEA

1. 打开 IDEA
2. `Settings` -> `Plugins` -> `⚙️` -> `Install Plugin from Disk...`
3. 选择构建的 ZIP 文件

## 配置

### 首次使用

1. 安装插件后，首次打开项目会显示配置向导
2. 前往 `Settings` -> `Tools` -> `Daily Commit Manager`
3. 配置以下信息：

#### LLM API 配置

- **LLM 提供商**: 选择你使用的模型（OpenAI、DeepSeek 等）
- **API Key**: 你的 API 密钥
- **API Base URL**: API 地址（会自动填充默认值）
- **模型名称**: 模型名称（如 `gpt-3.5-turbo`、`deepseek-chat` 等）

#### 每日提交配置

- **启用每日提交检查**: 是否开启强制提交功能
- **仅工作日检查**: 是否只在工作日检查（周末不检查）
- **检查时间**: 每天检查的时间（默认 17:00）

#### 总结存储配置

- **日总结路径**: 日总结保存路径（默认 `.idea/daily-summaries`）
- **周总结路径**: 周总结保存路径（默认 `.idea/weekly-summaries`）

#### 周总结配置

- **生成日期**: 每周几生成（默认 THURSDAY）
- **生成时间**: 生成时间（默认 17:00）

### 配置示例

**DeepSeek 配置：**
```
LLM 提供商: DeepSeek
API Key: sk-xxxxxxxxxxxxx
API Base URL: https://api.deepseek.com/v1
模型名称: deepseek-chat
```

**OpenAI 配置：**
```
LLM 提供商: OpenAI
API Key: sk-xxxxxxxxxxxxx
API Base URL: https://api.openai.com/v1
模型名称: gpt-3.5-turbo
```

## 使用方法

### 日常使用流程

1. **每天启动 IDE**
   - 如果昨天没有提交，会弹出强制提交对话框
   - 点击"AI 生成 Commit Message"或手动输入
   - 提交后会自动生成日总结

2. **手动生成总结**
   - 右键菜单 -> `VCS` -> `Generate Daily Summary` - 生成日总结
   - 右键菜单 -> `VCS` -> `Generate Weekly Summary` - 生成周总结

3. **查看历史总结**
   - 右键菜单 -> `VCS` -> `View Work Summaries`
   - 选择要查看的总结文件

### 周总结自动生成

- 每周四（默认）17:00 自动检测
- 如果本周还没有生成过周总结，会自动生成
- 显示预览对话框，可编辑后保存

## 项目结构

```
daily-commit-plugin/
├── src/main/kotlin/com/dailycommit/plugin/
│   ├── config/           # 配置模块
│   ├── git/              # Git 操作
│   ├── llm/              # LLM 集成
│   ├── ui/               # UI 界面
│   ├── listeners/        # 监听器
│   ├── summary/          # 总结生成
│   ├── scheduler/        # 调度器
│   ├── actions/          # Actions
│   └── utils/            # 工具类
├── src/main/resources/   # 资源文件
├── build.gradle.kts      # Gradle 构建
└── README.md
```

## 开发

### 技术栈

- Kotlin 1.9.22
- IntelliJ Platform SDK 2023.1+
- Ktor Client (HTTP)
- kotlinx.serialization (JSON)
- Git4Idea (Git 操作)

### 构建命令

```bash
# 构建插件
./gradlew buildPlugin

# 运行测试 IDE
./gradlew runIde

# 验证插件
./gradlew verifyPlugin
```

## 常见问题

### 1. LLM API 调用失败？

- 检查 API Key 是否正确
- 检查网络连接
- 确认 API Base URL 格式正确

### 2. 强制提交对话框无法关闭？

- 必须提交代码才能继续工作（这是设计初衷）
- 如需临时禁用，可在设置中关闭"启用每日提交检查"

### 3. 周总结没有自动生成？

- 检查是否配置了正确的生成日期和时间
- 确保 IDE 在生成时间点是打开的
- 也可以手动触发生成

## 许可证

MIT License

## 作者

P1neapple

## 贡献

欢迎提交 Issue 和 Pull Request！
