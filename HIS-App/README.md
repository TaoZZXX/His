# HIS-App

HIS 小程序端（原生微信小程序结构），模块与 `his-web` 对齐的轻量实现。

## 已实现页面

- 主页：`pages/home/index`
- 挂号预约：`pages/register/index`
- 医生明细：`pages/doctor-detail/index`
- 费用明细：`pages/payment/index`
- 检查检验结果：`pages/check-result/index`
- 我的：`pages/profile/index`

## 目录说明

- `utils/request.js`：请求封装
- `utils/api.js`：接口定义（已对接现有 HIS 后端路径）

## 运行方式（微信开发者工具）

1. 打开微信开发者工具
2. 选择“导入项目”
3. 项目目录选择：`HIS/HIS-App`
4. AppID 可先用测试号
5. 在 `app.js` 修改 `globalData.baseUrl` 到你的网关地址（例如 `http://localhost:8080`）
6. 在 `app.js` 的 `globalData.backendAuth` 配置后端账号（密码为 **MD5**）：
   - `username`
   - `passwordMd5`

## 说明

- 已接通主流程接口：科室/医生查询、挂号创建、应收查询、一键缴费
- 若接口异常会回退 mock 数据，保证页面可继续预览
