# HIS

HIS（Hospital Information System，医院信息系统）是一个面向医院业务场景的前后端项目，覆盖患者服务、门诊诊疗、检查检验、药房、财务结算、权限管理和运营看板等功能。

> 本项目主要用于学习、课程实践和功能验证，不建议直接用于生产环境。医疗业务上线前必须完成安全评审、数据脱敏、权限审计、性能测试和合规评估。

## 功能概览

- 患者端：主页、科室/医生查询、预约挂号、费用查询、缴费、检查检验结果和个人中心
- 医生工作站：患者接诊、病历/处方相关业务和检查检验结果处理
- 药房工作站：药品查询、处方处理和发药相关流程
- 财务模块：应收、支付和日结等业务
- 系统管理：员工、角色、权限、科室和排班管理
- 平台能力：网关鉴权、JWT、Redis 缓存、异步通知、定时任务、文件服务和监控配置

## 项目结构

```text
HIS/
├── HIS-Gateway/              # API 网关、路由和统一鉴权
├── HIS-Auth-Service/         # 登录、注册和员工认证
├── HIS-Registration-Service/ # 挂号/预约业务
├── HIS-Doctor-Service/       # 医生工作站业务
├── HIS-ExamLab-Service/      # 检查检验业务
├── HIS-Finance-Service/      # 收费、支付和财务业务
├── HIS-Pharmacy-Service/     # 药房业务
├── HIS-Home-Service/         # 首页和运营看板
├── HIS-Notify-Service/       # 通知相关能力
├── HIS-Job-Service/          # 定时任务
├── HIS-File-Service/         # 文件和检查结果图片
├── HIS-common/               # 公共实体、工具和基础组件
├── his-web/                  # Web 管理端/工作站前端
├── HIS-App/                  # 患者端小程序
├── ops/                      # 运维与监控配置
└── document/                 # 架构图、流程图和界面资料
```

## 技术栈

### 后端

- Java、Maven、Spring Boot、Spring Cloud Alibaba
- Spring Cloud Gateway/Zuul（以代码实际配置为准）、MyBatis、MySQL
- Redis、RabbitMQ、JWT、Swagger/OpenAPI 相关组件
- Docker Compose（可选，用于本地基础设施和监控）

### 前端

- `his-web`：Vue 2、Vue Router、Vuex、Element UI、Axios、ECharts
- `HIS-App`：原生微信小程序目录结构

## 环境要求

- JDK 8 或与项目 Maven 配置兼容的 JDK
- Maven 3.6+
- Node.js 及 npm（运行 `his-web` 时使用）
- MySQL、Redis；RabbitMQ 是否启用取决于对应模块配置
- 微信开发者工具（仅运行 `HIS-App` 时需要）

## 本地配置

不要把真实密码、令牌、私钥、患者数据或公司内网地址提交到 Git。建议为每个环境建立本地配置文件，或通过环境变量覆盖配置，例如：

```yaml
spring:
  datasource:
    url: ${HIS_DB_URL:jdbc:mysql://localhost:3306/his}
    username: ${HIS_DB_USERNAME:his}
    password: ${HIS_DB_PASSWORD:}

jwt:
  secret: ${HIS_JWT_SECRET:}
```

请在启动前准备数据库及初始化数据，并根据各服务的 `src/main/resources/application.yaml` 检查端口、数据库、Redis、消息队列和文件存储配置。默认值只适合本地开发，不代表安全配置。

## 启动后端

在项目根目录执行：

```bash
mvn clean package -DskipTests
```

然后按依赖顺序启动公共基础设施、网关和业务服务。推荐先启动认证/基础能力，再启动业务服务，最后启动网关。具体端口和服务名以各模块配置文件为准。

单独启动某个服务时，可执行：

```bash
mvn -pl HIS-Gateway -am spring-boot:run
```

## 启动 Web 前端

```bash
cd his-web
npm install
npm run dev
```

前端请求地址请配置为本地网关地址，不要把个人电脑的绝对路径、内网 IP 或真实账号写入源码。

## 启动患者端小程序

1. 使用微信开发者工具导入 `HIS-App`。
2. 使用测试 AppID 或已授权的 AppID。
3. 在 `HIS-App/app.js` 配置本地网关地址和仅用于开发的测试账号。
4. 真机调试前配置合法域名，并确认接口已启用 HTTPS。

## 隐私与安全检查清单

- 配置文件中的数据库密码、Redis 密码和 JWT 密钥必须改为环境变量或本地未跟踪配置。
- 禁止提交 `target/`、日志、IDE 数据源、导出的数据库文件和真实业务数据。
- 测试账号使用随机密码，禁止复用生产账号或个人账号。
- 日志、截图和接口示例中的患者信息必须脱敏或使用虚构数据。
- 如果密钥曾经提交到远程仓库，应立即轮换；仅删除当前文件不能消除 Git 历史中的泄露。
- 生产环境应使用强随机 JWT 密钥、最小权限数据库账号、HTTPS 和定期备份。

## 许可证

仓库当前包含 [`LICENSE`](LICENSE)。使用或再分发前请确认第三方依赖和历史代码的许可证要求。
