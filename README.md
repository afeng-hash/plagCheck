# plagChenckPro

基于 `Spring Boot 3` 的中文论文查重后端系统，支持文档上传、文本解析、句级查重、批量异步处理、结果追踪与用户权限管理。

## 1. 项目简介

`plagChenckPro` 面向高校课程设计/毕业论文场景，核心目标是实现“可落地、可扩展”的论文查重流程：

- 上传论文并存储到 MinIO
- 解析 PDF/Word/TXT 文档内容
- 分句、分词、特征提取（MinHash/LSH/语义向量）
- 相似候选筛选与精排
- 输出查重率、相似句、疑似来源论文
- 支持批量异步导入和任务状态追踪

## 2. 技术栈

- 后端框架：Spring Boot 3.0.13
- 持久层：MyBatis-Plus 3.5.8 + MySQL 8
- 文件存储：MinIO
- 文档解析：Apache Tika
- 中文 NLP：HanLP
- 语义向量：DeepLearning4J / ND4J（内置词向量模型）
- 认证授权：JWT + Spring MVC Interceptor
- 构建与部署：Maven + Docker

## 3. 核心功能

- 用户模块：登录、注册、个人信息修改、管理员权限控制
- 论文模块：单篇上传、分页查询、删除、下载
- 查重模块：上传待检文档，返回重复率和相似句详情
- 批量模块：多文件异步导入，任务进度与结果查询
- 反馈模块：用户意见收集

## 4. 查重流程（实现思路）

1. 文档解析：通过 Apache Tika 提取文本。
2. 文本预处理：清洗文本、分句、分词、停用词过滤。
3. 候选召回：基于 MinHash + LSH 桶检索快速召回候选句。
4. 相似度计算：结合编辑距离、语义向量余弦、关键词重合度进行综合评分。
5. 结果生成：输出重复率、相似句列表、Top 来源论文信息与等级结论。

## 5. 项目结构

```text
src/main/java/com/afeng/plagchenckpro
├── controller      # 接口层
├── service         # 业务层
├── mapper          # 数据访问层
├── entity          # DTO/VO/POJO
├── common          # 工具、统一返回、异常处理
├── config          # Web、异步、CORS、MinIO 配置
├── interceptor     # JWT 拦截器
└── algorithm       # MinHash / LSH 核心算法
```

## 6. 本地启动

### 6.1 环境要求

- JDK 17
- Maven 3.8+
- MySQL 8.x
- MinIO

### 6.2 配置说明

编辑 `src/main/resources/application.properties`：

- `spring.datasource.*`：数据库连接
- `minio.*`：对象存储配置
- `server.port`：默认 `8090`

建议将敏感信息（数据库密码、MinIO 密钥）改为环境变量读取。

### 6.3 启动命令

```bash
mvn clean spring-boot:run
```

或：

```bash
mvn clean package
java -jar target/plagChenckPro-0.0.1-SNAPSHOT.jar
```

## 7. 主要接口（示例）

- 用户登录：`POST /api/user/login`
- 用户注册：`POST /api/user/register`
- 单篇查重：`POST /api/plagiarism/check`
- 论文上传：`POST /api/papers/upload`
- 论文列表：`GET /api/papers/list`
- 批量上传：`POST /api/batch/upload`
- 批量进度：`GET /api/batch/status/{taskId}`
- 批量结果：`GET /api/batch/result/{taskId}`

> 除登录/注册外，大部分接口需在请求头携带 `token`。

## 8. 当前亮点

- 面向中文文本的句级查重实现
- LSH 预筛 + 综合相似度精排，兼顾性能与效果
- 支持批量异步处理和任务状态追踪
- 服务分层明确，便于二次开发

## 9. 后续优化方向

- 安全：密码哈希从 MD5 升级到 BCrypt，配置外置化
- 工程：补齐单元测试/集成测试与 OpenAPI 文档
- 稳定性：完善线程池隔离、重试机制、幂等控制
- 算法：建立标注数据集，按 Precision/Recall/F1 持续优化

## 10. 声明

本项目用于学习与工程实践展示，不替代学校或机构的正式学术查重系统。

