# Imagerator 2API — AI 图片/视频生成器 APK 逆向分析与 OpenAI 兼容网关

> 逆向对象：**AI Image to Video Generator**（`com.techinfortainment.imagerator`，Telegram 流通重打包版 `Ai Image to Video Generator_3_rd.apk`）
> 交付物：中文逆向报告 + OpenAI 兼容 demo 服务器（`demo_server.py`，端口 8902）+ 网页 demo（`demo.html`）+ 自动化免费号池
> 上游：`https://techinfortainment.com/ai-generation/*`（第三方生产服务，本项目只做低频功能验证，**不做压测**）

## 逆向核心发现

### 基本信息
| 项 | 值 |
|---|---|
| 包名 | `com.techinfortainment.imagerator`（主逻辑 `com.tit.imagegenerator.*`） |
| 版本 | v3 协议（`version=3` 请求字段） |
| 保护 | pairip 加固 + `libSignatureKiller.so`（**重打包版**，签名校验被 hook 绕过） |
| 技术栈 | 原生 Android（Kotlin + OkHttp + Room + Compose 组件），重度混淆（jadx 重命名包 `p078j6` 等） |
| 上游 | `techinfortainment.com/ai-generation`（自建网关，背后疑似 ComfyUI/LTXV/Flux 工作流） |

### 协议：明文，无加密无签名（比 OpenChat 的 AES 和 NoTrack 的三层签名简单得多）
- **鉴权 = 仅 `device_id`**（即 Android `Settings.Secure.ANDROID_ID`，可任意伪造）+ `app_name=imagerator`
- **注册无任何验证**（无 CAPTCHA / 无设备指纹 / 无 IP 绑定）：每个新 `device_id` 送 20~30 credits，`plan=Ads`

### 端点清单（全部已实测验证 ✅）

| 端点 | 方法 | 用途 | 关键参数 |
|---|---|---|---|
| `/users/register` | POST JSON | 注册设备拿 credits | `device_id, app_name, user_type(Organic/Deeplink), purchase_token, product_id` |
| `/users/{id}/credits?app_name=` | GET | 查余额 | 返回 `{device_id, credits, plan, type, blocked, whitelisted}` |
| `/admin/pricing` | GET | 价格表+配置 | `costs` + `settings.register_credits=30, daily_claim_credits=10` |
| `/generate/text2image` | POST JSON | 文生图 | `prompt, model(ZImageTurbo_INT8/Flux1schnell/NanoBanana), width, height, steps=8, guidance=7.5` |
| `/generate/image2image` | POST multipart | 图生图 | `image` 文件 + `model(Flux_2_Klein_4B_BF16)` |
| `/generate/text2video` | POST JSON | 文生视频 | `model(Ltxv_13B_0_9_8_Distilled_FP8), frames=120`（30fps → 4 秒视频） |
| `/generate/image2video` | POST multipart | 图生视频 | `image` + `frames=120, fps=30, steps=1, negative_prompt=blur` |
| `/generate/image2video-plus` | POST multipart | 图生视频 Plus（2 credits） | 同上无 model 字段 |
| `/result?request_id=&app_name=` | GET | 轮询结果 | 客户端 3s 间隔轮询 |
| `/suggestions/all?appName=` | GET | 提示词推荐 | — |
| `/live_generation?refresh=&cursor=` | GET | 社区实时广场 | header `device-id`, `X-Is-Whitelisted` |

### 生成流程（异步任务制）
```
提交 generate/* → {"request_id": "..."} 
→ 每 3s 轮询 /result 
→ {"status": "processing|completed|failed|expired|blocked|ad_required",
    "image_url": ".../media/imagerator/Turbo/safe/<uuid>.png",
    "video_url": "...", "credits_remaining": N, "message": "..."}
```
- `ad_required`：免费用户偶尔需看广告解锁
- 内容安全审核：输出走 `safe/` 目录，NSFW 会被 `blocked`

### Credits 体系（实测）
- 注册送 20~30；每日可领 10（有冷却）；价格：文生图 1、图生图 1、视频 2、NanoBanana 2、视频+声音 10
- **号池白嫖**：`device_id` 就是 32 位 hex 字符串，伪造零成本 → `demo_server.py` 内置自动化号池：余额耗尽自动注册新号、启动时从 `device_pool.json` 恢复、并发安全、`/health` 暴露池状态

## Demo 服务器（OpenAI 兼容）

```bash
python demo_server.py        # 0.0.0.0:8902
```
| 端点 | 说明 |
|---|---|
| `GET /v1/models` | 模型列表 |
| `POST /v1/images/generations` | 文生图（`model`: imagerator-turbo/flux/nanobanana，`size`，支持 b64_json） |
| `POST /v1/images/edits` | 图生图 / 图生视频（multipart） |
| `POST /v1/video/generations` | 文生视频异步任务 → `GET /v1/video/generations/{id}` 轮询 |
| `POST /v1/chat/completions` | 聊天接口返回图片 markdown |
| `GET /health` | 号池状态 + 总余额 |
| `POST /api/register`、`GET /api/credits`、`GET /api/pricing` | 原生透传 |
| `GET /` | demo.html 网页 |

## 实测记录
- ✅ 注册：HTTP 200，`{"credits":20,"plan":"Ads"}`（30 为 register_credits 配置，实测拿到 20/30 均有）
- ✅ 文生图（Turbo）：~20s 完成，真实图片输出
- ✅ 图生图（Flux_2_Klein_4B_BF16）：~9s 完成，真实图片输出
- ⚠️ 文生视频（LTXV 13B）：任务接受正常（`request_id` 返回），但上游成功率低 —— 3 次实测：1 次上游内部 `failed`、1 次内容审核 `blocked`（"Content not allowed"，连热气球这类完全无害提示词也会误拦）、视频生成约需 1-5 分钟
- ✅ 本地压测（仅压 demo server 自身，不压上游）：`/health` 310 rps@10 并发、188 rps@60 并发；参数校验路径 90/90 正确 400
- 坑：`curl` 需 `--ssl-no-revoke`（schannel 证书吊销检查离线失败）

## 自动化号池（device-pool）
上游注册**无任何验证**（无 CAPTCHA/设备指纹/IP 绑定），`device_id` 就是 32 位 hex，伪造零成本：
```
get_device() 三级回退:
1. 本地池找余额 ≥1 的设备 (O(1), 并发安全 _pool_lock)
2. 全欠费 → 向上游 /users/{id}/credits 同步真实余额 (本地计数可能偏低)
3. 仍无 → 注册新号 (uuid4().hex, 送 20~30 credits, 重试 3 次)
```
- `device_pool.json` 持久化：重启不丢号
- `deduct()` 扣减后落盘；`/health` 返回池明细 + 总余额
- 理论产能：每号 ~20-30 credits，号池自动滚动 = 实际无限免费额度（受上游 IP 风控约束，建议低频使用）

## 代码位置
- `demo_server.py` / `demo.html` — 网关 + 网页
- 关键反编译源文件（jadx 重命名后）：
  - `p078j6/H.java` — text2image + 轮询状态机
  - `p078j6/C1361m.java` — image2image
  - `p078j6/C1365q.java` — image2video
  - `p078j6/C1367t.java` — image2video-plus
  - `p078j6/J.java` — text2video
  - `p078j6/C1350b.java` — 注册/余额/广场/推荐
  - `D3/AbstractC0316x6.java` — device_id = ANDROID_ID

## 已移植进 new-api（生产）
渠道 type 66 已合并进 [lza6/new-api-Max](https://github.com/lza6/new-api-Max)（v1.3.211 上线）：
- `relay/channel/imagerator/` — 适配器（images + chat 双模）+ Go 自动化号池（三级回退自动注册）
- 2API 管理面板 + 模型广场徽标已接入
- 部署脚本：`deploy/post-deploy-imagerator.sh`
- 排障要点：上游对空 device_id 不自动生成（必须客户端自带）；passthrough 场景需在 DoRequest 补默认字段

## 免责声明
仅供安全研究与协议学习。上游为第三方生产服务，请勿高频滥用；生成的请求全部使用自注册设备号。
