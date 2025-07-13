# 我聊 (WoLiao) - 实时聊天应用

一个基于 Vue 3 + Spring Boot 的现代化实时聊天应用，采用前后端分离架构。

## 技术栈

### 后端
- **Java 17**
- **Spring Boot 3**
- **Spring Security** - 认证与授权
- **Spring Data JPA** - 数据持久化
- **MySQL** - 主数据库
- **Redis** - 缓存与令牌管理
- **JJWT** - JWT令牌生成与解析
- **Kaptcha** - 图形验证码

### 前端
- **Vue 3** - 使用组合式 API 和 `<script setup>`
- **TypeScript** - 类型安全
- **Vite** - 构建工具
- **Vue Router** - 路由管理
- **Element Plus** - UI组件库
- **Axios** - HTTP客户端

## 项目结构

```
WoLiao/
├── backend/                 # Spring Boot 后端
│   ├── src/main/java/com/woliao/backend/
│   │   ├── config/         # 配置类
│   │   ├── controller/     # 控制器
│   │   ├── dto/           # 数据传输对象
│   │   ├── entity/        # 实体类
│   │   ├── repository/    # 数据访问层
│   │   ├── security/      # 安全相关
│   │   └── service/       # 业务逻辑层
│   └── src/main/resources/
│       └── application.yml # 配置文件
├── frontend/               # Vue 3 前端
│   ├── src/
│   │   ├── api/           # API 接口
│   │   ├── components/    # 组件
│   │   ├── layouts/       # 布局组件
│   │   ├── router/        # 路由配置
│   │   ├── views/         # 页面组件
│   │   └── main.ts        # 入口文件
│   ├── package.json
│   └── vite.config.ts
└── README.md
```

## 功能特性

### 认证系统
- ✅ 基于JWT的无状态认证
- ✅ 图形验证码登录
- ✅ 7天内免登录（刷新令牌）
- ✅ 自动令牌刷新机制

### 用户界面
- ✅ 现代化响应式设计
- ✅ 移动端友好的底部导航
- ✅ 消息会话列表（支持置顶排序）
- ✅ 联系人管理（群聊/个人分组）

### 安全特性
- ✅ Spring Security 保护
- ✅ CORS 配置
- ✅ 密码加密存储
- ✅ 令牌过期处理

## 快速开始

### 环境要求
- Java 17+
- Node.js 16+
- MySQL 8.0+
- Redis 6.0+

### 后端启动

1. **配置数据库**
   ```sql
   CREATE DATABASE woliao CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **配置Redis**
   确保Redis服务运行在 `localhost:6379`

3. **修改配置**
   编辑 `backend/src/main/resources/application.yml`，更新数据库连接信息

4. **启动后端**
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   后端将在 `http://localhost:8080` 启动

### 前端启动

1. **安装依赖**
   ```bash
   cd frontend
   npm install
   ```

2. **启动开发服务器**
   ```bash
   npm run dev
   ```
   前端将在 `http://localhost:3000` 启动

## API 接口

### 认证相关
- `POST /api/captcha/generate` - 生成图形验证码
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/refresh` - 刷新访问令牌

### 数据接口
- `GET /api/sessions` - 获取聊天会话列表
- `GET /api/contacts` - 获取联系人列表

## 开发说明

### 测试用户
由于目前是演示版本，您需要先在数据库中创建测试用户：

```sql
INSERT INTO users (phone_number, password, nickname, created_at, updated_at, is_active) 
VALUES ('13800138000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '测试用户', NOW(), NOW(), true);
```

密码为 `123456`（已BCrypt加密）

### 验证码
- 图形验证码长度为4位字符
- 验证码有效期为5分钟
- 验证成功后自动失效

## 部署

### 后端部署
```bash
cd backend
mvn clean package
java -jar target/woliao-backend-1.0.0.jar
```

### 前端部署
```bash
cd frontend
npm run build
```
将 `dist` 目录部署到Web服务器

## 许可证

MIT License 