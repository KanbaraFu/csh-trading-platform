# 淘学二手 · 校园二手交易平台

技术栈：Vite 8 + Vue 3 + Element Plus + Pinia + vue-router

## 仓库结构

本仓库为 monorepo，前后端并列存放：

```
csh-trading-platform/
├── frontend/   # 前端工程（Vite root 即此目录）
├── backend/    # 后端工程（SpringBoot）
└── docs/       # 前后端共享的接口契约与联调文档
```

## 启动

所有前端命令均在 `frontend/` 目录下执行：

```bash
cd frontend
npm install
npm run dev   # http://localhost:5173
npm run build
```


## 演示账号
手机号 13800000001 / 密码 123456（登录页可一键填入）

## 页面路由
/login 登录注册 | / 首页 | /category 分类 | /products 商品列表 | /products/:id 商品详情
/publish 发布 | /favorites 收藏 | /cart 购物车 | /checkout 确认订单 | /orders 订单列表 | /orders/:id 订单详情
/messages 消息中心 | /search 搜索 | /rank 排行榜 | /profile 个人中心

## 前端目录结构

以下路径均相对 `frontend/`：

src/api 接口层 | src/mock Mock 内存数据源 | src/store Pinia | src/layout 布局 | src/components 通用组件 | src/views 页面 | src/styles 设计变量

## 对接真实后端

前端通过环境变量 `VITE_USE_MOCK` 切换 Mock / 真实后端（读取逻辑在 `frontend/src/utils/request.js`）：

| 文件 | 值 | 用途 |
|------|----|------|
| `.env` | `true` | 默认走 Mock，无后端也能完整演示 |
| `.env.production` | `false` | 生产构建走真实后端 |
| `.env.development.local` | 自定 | 本地联调覆盖，**不入库** |

切到 `false` 后请求发往 `/api`，由 `frontend/vite.config.js` 的 proxy 转发到 `http://localhost:8080`。

> 改完 `.env*` **必须重启 dev server** —— Vite 只在启动时读取环境变量。

## 说明
- 全部数据为前端 Mock 内存态，刷新页面即重置为基准数据，写操作（收藏/购物车/订单/消息）真实生效。
- 后端尚未实现（`backend/` 待创建）；支付为模拟支付，不产生真实扣款。
- 后端需实现的接口清单见 `docs/后端接口对接说明.md`。
