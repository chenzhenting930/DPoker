# DPoker 前端接口文档

> 适用版本：当前 `main` 分支（Spring Boot + STOMP WebSocket）
> 文档目的：给前端工程师提供开发所需的全部接口定义、消息方向、订阅地址与数据结构说明。
> 阅读对象：负责对接德州扑克前后端的前端工程师。

---

## 目录

1. [基础信息](#1-基础信息)
2. [通用约定](#2-通用约定)
3. [通用数据结构](#3-通用数据结构)
4. [连接与鉴权流程](#4-连接与鉴权流程)
5. [REST 接口](#5-rest-接口)
6. [STOMP 消息端点](#6-stomp-消息端点)
7. [订阅地址与服务器推送](#7-订阅地址与服务器推送)
8. [典型业务流程](#8-典型业务流程)
9. [错误码与异常处理](#9-错误码与异常处理)
10. [前端接入注意事项](#10-前端接入注意事项)

---

## 1. 基础信息

| 项目 | 值 |
| --- | --- |
| 后端框架 | Spring Boot + STOMP over WebSocket |
| WebSocket 连接地址 | `ws://{host}:{port}/ws`（启用 SockJS 回退） |
| 应用前缀（客户端 → 服务端） | `/app` |
| 广播主题前缀（服务端 → 客户端） | `/topic` |
| 点对点队列前缀（服务端 → 客户端） | `/queue`（用户维度的目的地前缀为 `/user`） |
| REST 基址 | `http://{host}:{port}` |
| 默认端口 | 参考 `application.yml`（Spring Boot 默认 `8080`） |

> 重要：所有「游戏实时状态」是**点对点**推送（每人的底牌不同），订阅地址是 `/user/queue`；
> 房间级别的文本提示与房间快照（`GameRoomVO`）走 `/topic/game/{roomId}` 广播。

---

## 2. 通用约定

### 2.1 鉴权

- 登录成功后，后端返回 `token`（字符串）。
- 客户端在 STOMP `CONNECT` 帧的 **Header** 中以 `token: {token}` 形式携带。
- 后端在 `StompAuthConfig#preSend` 拦截 `CONNECT`，校验 `token`：
  - 校验失败：抛出 `IllegalArgumentException("STOMP CONNECT 鉴权失败")`，连接被拒。
  - 校验成功：把 `userId` 写入 session，并作为 Principal 绑定。
- REST 接口 `/login` 不走 STOMP，**不需要** token 头。

### 2.2 统一响应封装 `Result`

所有 REST 与 STOMP 业务接口都返回 `Result` 结构（即使是 `GameController` 中带 `@SendTo` 的方法，载荷仍是 `Result`）。

```json
{
  "code": 200,
  "msg":  "操作成功",
  "data": null
}
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `code` | int | 状态码。`200` 成功；`0` 业务失败；`207` 行动已受理（详见 [第 9 节](#9-错误码与异常处理)）；其他自定义值按接口说明 |
| `msg` | string | 提示信息 |
| `data` | object \| null | 业务数据，结构随接口变化 |

### 2.3 时间与卡牌编码

- 服务器无时区特殊处理，前端按本地时区展示即可。
- 卡牌编码为「花色符号 + 点数」，如 `♠A`、`♥10`、`♦2`：
  - 花色符号：`♥(HEARTS)`、`♦(DIAMONDS)`、`♣(CLUBS)`、`♠(SPADES)`。
  - 点数：`2~10`、`J`、`Q`、`K`、`A`（`A` 视为 14）。

---

## 3. 通用数据结构

### 3.1 `Result`

参见 [2.2](#22-统一响应封装-result)。

### 3.2 `User`（用户信息）

```json
{
  "id": 1,
  "username": "tom",
  "password": "******",
  "point": 100000.0,
  "nickname": "Tom",
  "createTime": "2026-06-01T10:00:00"
}
```

> 注意：`getUserInfo` 把整个 `User` 返回，**包含 `password` 字段**。前端请勿在 UI 上展示密码，并建议接到后立即丢弃该字段或在后端移除。

### 3.3 `Card`（扑克牌）

仅以 `String` 形式在 `GameUpdateDto.communityCards` 和 `PlayerView.holeCards` 中传输。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `communityCards` | `string[]` | 公牌列表，已用 `Card.toString()` 序列化 |
| `holeCards` | `string[]` | 底牌，**只有当前玩家本人非空**，他人是 `[]` |

### 3.4 `ActionRequest`（行动 / 房间操作请求体）

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `playerId` | int | 是 | 当前操作用户的 `userId` |
| `userName` | string | 是 | 用户昵称，用于日志与房间提示 |
| `name` | string | 视接口 | 创建房间时为「房间名称」 |
| `action` | string | 视接口 | 行动类型：`fold` / `call` / `raise` / `check` |
| `amount` | int | 视接口 | 加注筹码（仅 `raise` 需要） |
| `bigBlind` | int | 创建房间时 | 大盲，默认 `200` |
| `smallBlind` | int | 创建房间时 | 小盲，默认 `100` |

### 3.5 `PlayerVO`（房间内玩家快照）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `userId` | int | 用户 ID |
| `playerName` | string | 玩家昵称 |
| `point` | float | 用户积分 |
| `chips` | int | 当前筹码 |
| `ready` | bool | 是否已准备（仅在房间内有效） |

### 3.6 `GameRoomVO`（房间快照，type=`"RoomInfo"`）

`type` 字段固定为 `"RoomInfo"`，便于前端在同一个 `Object` 流中识别消息类型。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `type` | string | 固定 `"RoomInfo"` |
| `roomId` | int | 房间 ID |
| `name` | string | 房间名称 |
| `players` | `PlayerVO[]` | 房间内的玩家列表（按座位顺序，无人座位不在数组中） |
| `gameStarted` | bool | 房间内游戏是否已开始 |
| `blinds` | `int[]` | `[smallBlind, bigBlind]` |

### 3.7 `PlayerView`（游戏内玩家视图，type=`"GameUpdate"`）

每局游戏的实时状态中，每个玩家对应一个 `PlayerView`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `userId` | int | 用户 ID |
| `chips` | int | 剩余筹码 |
| `totalBetInHand` | int | 本手牌累计投入底池的筹码 |
| `folded` | bool | 是否已弃牌 |
| `allIn` | bool | 是否全下 |
| `holeCards` | `string[]` | 底牌；**只有当前推送目标的玩家自己非空**，其他人是 `[]` |
| `isCurrentPlayer` | bool | 是否轮到该玩家行动 |
| `index` | int | 座位号（0 起） |
| `posName` | string | 位置名称：`"庄家"` / `"小盲"` / `"大盲"` / `"普通位置"` |

### 3.8 `PotView`（奖池视图）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `amount` | int | 该奖池筹码总额 |
| `eligiblePlayerIds` | `int[]` | 有资格争夺该奖池的玩家 ID 集合 |

### 3.9 `GameUpdateDto`（游戏实时状态，type=`"GameUpdate"`）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `type` | string | 固定 `"GameUpdate"` |
| `roomId` | int | 房间 ID |
| `players` | `PlayerView[]` | 房间内每位玩家的视图 |
| `communityCards` | `string[]` | 公牌（`flop`/`turn`/`river` 阶段逐步增加） |
| `currentBet` | int | 当前轮次最高下注额 |
| `currentPlayerId` | int \| null | 当前行动玩家 ID |
| `pots` | `PotView[]` | 当前所有奖池 |
| `currentRound` | string | `"preflop"` / `"flop"` / `"turn"` / `"river"` / `"showdown"` |
| `gameEnded` | bool | 本手牌是否已结束 |
| `gameReports` | `GameReport[]` | 结算报告；游戏未结束或报告未生成时为 `null` |

### 3.10 `GameReport`（结算报告）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `userId` | int | 玩家 ID |
| `playerName` | string | 玩家昵称 |
| `point` | float | 用户积分（结算时） |
| `chips` | int | 用户筹码（结算时） |
| `totalBetInHand` | int | 整手牌累计投入 |
| `winChips` | int | 赢得的筹码 |
| `loseChips` | int | 输掉的筹码 |
| `holeCards` | `Card[]` | 底牌（结算时用于展示摊牌） |

> `Card` 字段在 `GameReport` 中以对象形式传输（包含 `suit` / `rank`）；如需展示，调用 `Card.toString()` 规则见 [2.3](#23-时间与卡牌编码)。

---

## 4. 连接与鉴权流程

### 4.1 步骤

1. 前端调用 `POST /login`，获取 `token`。
2. 前端用 SockJS 客户端连接 WebSocket：
   - URL：`/ws`
   - 在 `connectHeaders` 中携带 `token: {token}`。
3. STOMP `CONNECT` 帧到达后端 → `StompAuthConfig` 校验 `token`。
4. 校验通过后，前端订阅：
   - **房间级广播**：`/topic/game/{roomId}`
   - **点对点状态与提示**：`/user/queue`（Spring 会展开为 `/user/queue-{sessionId}`，STOMP 客户端订阅时只需写 `/user/queue`）

### 4.2 JavaScript 示例（`@stomp/stompjs` + `sockjs-client`）

```javascript
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const token = '<login 后拿到的 token>';

const client = new Client({
  webSocketFactory: () => new SockJS('/ws'),
  connectHeaders: { token },
  onConnect: () => {
    // 点对点：状态更新、登录后用户信息等
    client.subscribe('/user/queue', (msg) => {
      handlePrivateMessage(JSON.parse(msg.body));
    });

    // 房间广播：加入/离开提示、房间快照
    client.subscribe('/topic/game/1001', (msg) => {
      handleRoomBroadcast(JSON.parse(msg.body));
    });
  },
  onStompError: (frame) => {
    console.error('STOMP error', frame.headers['message']);
  },
});

client.activate();
```

### 4.3 关键点

- 必须先 `POST /login` 拿到 `token` 才能 `CONNECT`，否则后端拒绝。
- `token` 失效或缺失时，`STOMP CONNECT` 会被服务端拒绝，客户端需引导用户重新登录。
- 鉴权只在 `CONNECT` 阶段做一次；业务消息不再校验 token。

---

## 5. REST 接口

### 5.1 登录

| 项 | 值 |
| --- | --- |
| 方法 | `POST` |
| 路径 | `/login` |
| 鉴权 | 无 |
| 请求体 | `LoginRequest`（见下） |
| 成功响应 | `Result.code=200`，`Result.data` 为 `token` 字符串 |

请求体 `LoginRequest`：

```json
{
  "username": "tom",
  "password": "123456"
}
```

响应示例：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": "<token 字符串>"
}
```

错误情况：

| 场景 | code | msg |
| --- | --- | --- |
| 请求体为空 | 0 | 用户名或密码不能为空 |
| 用户不存在 | 0 | 用户不存在 |
| 密码错误 | 0 | 密码错误 |

---

## 6. STOMP 消息端点

> 所有端点都由前端发送 `SEND` 帧到 `/app/...` 触发。
> 表中「返回方向」指 Spring `MessageMapping` 方法本身的返回被广播到何处（来自 `@SendTo` / `@SendToUser`）。
> 房间内的实际状态推送，**大部分走 `GameNotificationService` 发到 `/user/queue`**，详见 [第 7 节](#7-订阅地址与服务器推送)。

### 6.1 房间生命周期

| 端点 | 触发 | 客户端发送目的地 | 请求体 | 返回方向 | 成功返回 |
| --- | --- | --- | --- | --- | --- |
| 创建房间 | `createGameRoom` | `/app/game/{roomId}/create` | `ActionRequest`（`playerId`, `userName`, `name`, `smallBlind`, `bigBlind`） | `/user/queue` | `Result.success("房间创建成功", GameRoomVO)` |
| 加入房间 | `joinGameRoom` | `/app/game/{roomId}/join` | `ActionRequest`（`playerId`, `userName`） | `/user/queue` | `Result.success("加入房间成功！", GameRoomVO)` |
| 离开房间 | `leaveGameRoom` | `/app/game/{roomId}/leave` | `ActionRequest`（`playerId`, `userName`） | `/user/queue` | `Result.success("离开房间成功！", null)` |
| 房间列表 | `getGameRoomList` | `/app/getGameRoomList` | 无 | `/user/queue` | `Result.success(List<GameRoomVO>)` |
| 房间详情 | `getGameRoomInfo` | `/app/game/{roomId}/getGameRoomInfo` | `ActionRequest`（`playerId`, `userName`） | `/user/queue` | `Result.success(GameRoomVO)` |
| 获取实时状态 | `getGameUpdate` | `/app/game/{roomId}/getGameUpdate` | `ActionRequest`（`playerId`, `userName`） | `/user/queue` | `Result.success("成功获取游戏状态！")`，状态由点对点推送 |

附加说明：
- `joinGameRoom` 同时会向 `/topic/game/{roomId}` 广播两条消息：一句「玩家 xxx 加入房间」文本，再附带一个 `GameRoomVO`。
- `leaveGameRoom` 类似：先广播离开文本，再广播新的 `GameRoomVO`。
- `createGameRoom` 失败时返回 `Result.fail(...)`，**不**广播。
- `getGameRoomList` 当无房间时返回 `Result.fail("房间列表为空！")`。

### 6.2 游戏流程

| 端点 | 触发 | 客户端发送目的地 | 请求体 | 返回方向 | 成功返回 |
| --- | --- | --- | --- | --- | --- |
| 准备 / 开始新一局 | `ReadyForGame` | `/app/game/{roomId}/ready` | `ActionRequest`（`playerId`, `userName`） | `/topic/game/{roomId}` | 所有人都已准备且 `players.size >= 3` 时返回 `Result.success("游戏开始！", GameRoomVO)`，否则返回 `Result.success("xxx 已准备...", GameRoomVO)` |
| 玩家行动 | `handlePlayerAction` | `/app/game/{roomId}/action` | `ActionRequest`（`playerId`, `userName`, `action`, `amount`） | `/topic/game/{roomId}` | `raise` → `Result.success(200, "<name> 进行了 raise 金额：xxx")`；其他 → `Result.success(207, "<name> 进行了 <action>")` |
| 全局结算 | `globalSettlement` | `/app/game/{roomId}/globalSettlement` | `ActionRequest`（`playerId`） | `/topic/game/{roomId}` | `Result.success("全局结算完成！")` |

附加说明：
- `action` 字段取值：`fold` / `call` / `raise` / `check`。
- `raise` 时必须传 `amount`，且 `amount <= chips + betThisRound`，否则后端会自动把 `amount` 截断到最大值。
- `startNewGame` 会校验筹码：若 `chips < 1000`，自动扣除 10000 积分并补满到 10000 筹码（写回 `user` 表）。
- `globalSettlement` 把所有玩家的 `chips - 10000` 差额折算回积分，重置筹码为 10000；要求 `gameStarted=false`，否则返回 `Result.fail("游戏已经开始，请结束后进行结算！")`。
- `handlePlayerAction` 失败（如房间不存在）时 `code=0`，**不会**广播。

### 6.3 用户信息 / 排行榜

| 端点 | 触发 | 客户端发送目的地 | 请求体 | 返回方向 | 成功返回 |
| --- | --- | --- | --- | --- | --- |
| 当前用户信息 | `handlePlayerAction` (`/getUserInfo`) | `/app/getUserInfo` | 无 | `/user/queue` | `Result.success(User)` |
| 积分排行榜 | `getPointRank` | `/app/getPointRank` | 无 | `/user/queue` | `Result.success("获取排行榜成功", List<User>)`（仅 `nickname` 与 `point` 字段） |

---

## 7. 订阅地址与服务器推送

### 7.1 房间广播：`/topic/game/{roomId}`

由 `GameController` 的 `@SendTo("/topic/game/{roomId}")` 与 `GameNotificationService.notifyAllInRoom` 发出。
消息载荷有三种可能：

1. **字符串**（纯文本提示，前端直接展示）

   ```json
   "玩家tom加入房间"
   ```

2. **`GameRoomVO` 快照**（`type="RoomInfo"`）

   ```json
   {
     "type": "RoomInfo",
     "roomId": 1001,
     "name": "德州一桌",
     "players": [ { "userId": 1, "playerName": "tom", "point": 100000, "chips": 10000, "ready": false } ],
     "gameStarted": false,
     "blinds": [100, 200]
   }
   ```

3. **`Result`**（由 `@SendTo` 直接返回的接口产生，比如 `action` / `ready` / `globalSettlement`）

   ```json
   { "code": 207, "msg": "tom 进行了 call", "data": null }
   ```

> 推荐做法：先判断 `data` / `type` 字段是否为对象，再决定渲染策略：
> - `data` 是字符串 → 文本提示。
> - `data` 是对象且 `type === "RoomInfo"` → 渲染房间快照。
> - `data` 是 `Result` 或 `code` 是 0/200/207 → 业务反馈。

### 7.2 点对点推送：`/user/queue`

由 `GameController` 的 `@SendToUser("/queue")` 与 `GameNotificationService.notifyRoom` / `notifyRoomToPlayer` / `notifyPlayer` 发出。
载荷类型：

1. **`Result`**（业务反馈，如 `getUserInfo` / `getPointRank` / `getGameRoomList` 等）
2. **`GameUpdateDto`**（`type="GameUpdate"`，实时游戏状态，由 `notifyRoom` 推送）
3. **错误信息**（`processOneEvent` 异常时通过 `notifyPlayer` 推送的 `e.getMessage()`）

`GameUpdateDto` 示例（注意 `holeCards` 的隔离）：

```json
{
  "type": "GameUpdate",
  "roomId": 1001,
  "players": [
    {
      "userId": 1,
      "chips": 9800,
      "totalBetInHand": 200,
      "folded": false,
      "allIn": false,
      "holeCards": ["♠A", "♥K"],
      "isCurrentPlayer": true,
      "index": 0,
      "posName": "庄家"
    },
    {
      "userId": 2,
      "chips": 9700,
      "totalBetInHand": 300,
      "folded": false,
      "allIn": false,
      "holeCards": [],
      "isCurrentPlayer": false,
      "index": 1,
      "posName": "小盲"
    }
  ],
  "communityCards": ["♣5", "♦8", "♠J"],
  "currentBet": 300,
  "currentPlayerId": 1,
  "pots": [ { "amount": 500, "eligiblePlayerIds": [1, 2, 3] } ],
  "currentRound": "flop",
  "gameEnded": false,
  "gameReports": null
}
```

> 重要：`holeCards` 字段按订阅人隔离，只有「自己的 `userId`」对应的 `PlayerView` 会带底牌，其他人的 `holeCards` 是 `[]`。前端不要在收到他人视角时尝试读取底牌。

---

## 8. 典型业务流程

### 8.1 登录

1. 前端：`POST /login` → 拿 `token`。
2. 前端：用 `token` 建立 STOMP 连接。
3. 连接成功后订阅：
   - `/user/queue`（接收点对点消息）
   - 进入房间后再订阅 `/topic/game/{roomId}`

### 8.2 房间创建 → 加入 → 准备 → 对局

```
前端                                  后端
 |  POST /login                      |
 |---------------------------------> |
 |   { code:200, data:"<token>" }   |
 |                                   |
 |  CONNECT ws://.../ws  token=xxx   |
 |---------------------------------> |
 |  SUBSCRIBE /user/queue            |
 |  SUBSCRIBE /topic/game/1001       |
 |                                   |
 |  SEND /app/game/1001/create       |  (roomId=1001)
 |  { playerId, userName, name,      |
 |    smallBlind, bigBlind }         |
 |   -> /user/queue: RoomInfo       |
 |                                   |
 |  其他玩家                          |
 |  SEND /app/game/1001/join         |
 |  { playerId, userName }           |
 |   -> /user/queue: RoomInfo       |
 |   -> /topic/game/1001: "玩家xxx加入房间"
 |   -> /topic/game/1001: RoomInfo  |
 |                                   |
 |  每人: SEND /app/game/1001/ready  |
 |   -> /topic/game/1001: Result(207, "xxx 已准备", RoomInfo)
 |  全部 ready 且 ≥3 人时:
 |   -> /topic/game/1001: Result(200, "游戏开始！", RoomInfo)
 |   -> /user/queue: GameUpdate (发牌)
 |                                   |
 |  当前玩家: SEND /app/game/1001/action { action:"call" }
 |   -> /topic/game/1001: Result(207, "...call")
 |   -> /user/queue: GameUpdate      (每位玩家一份)
 |                                   |
 |  ... 更多行动 / 轮次切换 ...        |
 |                                   |
 |  游戏结束: GameUpdate.gameEnded=true 且 gameReports 填充
 |  房间: SEND /app/game/1001/globalSettlement
 |   -> /topic/game/1001: Result(200, "全局结算完成！")
 |                                   |
 |  SEND /app/game/1001/leave        |
 |   -> /user/queue: Result(200, "离开房间成功！", null)
 |   -> /topic/game/1001: "玩家xxx离开房间"
 |   -> /topic/game/1001: RoomInfo
```

### 8.3 断线与重连

- 建议前端在收到 `STOMP ERROR` 帧时，引导用户重新登录。
- 重连成功后，需要**重新订阅**房间广播并按需调用 `getGameUpdate` 拉取最新状态。

---

## 9. 错误码与异常处理

### 9.1 `Result.code` 通用值

| code | 含义 |
| --- | --- |
| 200 | 成功 |
| 0 | 业务失败（具体原因看 `msg`） |
| 207 | 行动已受理（`/app/game/{roomId}/action` 的非 `raise` 行为专用） |
| 2 | 玩家已加入房间（`joinGameRoom` 专用） |
| 3 | 游戏已开始，请观战（`joinGameRoom` 专用） |
| 401 | 预留：未登录（当前实现未使用，前端可保留处理逻辑） |
| 500 | 预留：服务器内部错误 |

### 9.2 典型业务错误 `msg`

| 触发点 | msg |
| --- | --- |
| `login` | 用户名或密码不能为空 / 用户不存在 / 密码错误 |
| `joinGameRoom` | 房间不存在！请先创建房间！ / 用户不存在！ / 玩家已加入房间！ / 游戏已开始，请观战！ |
| `createGameRoom` | 房间已存在！ / 房间创建失败！... / 用户不存在！ |
| `startNewGame` | 房间不存在！请先创建房间！ / 玩家不存在！ |
| `onPlayerAction` | 房间不存在 |
| `leaveGameRoom` | 游戏已开始，不允许离开房间！ / 离开房间失败！... |
| `globalSettlement` | 房间不存在！ / 游戏已经开始，请结束后进行结算！ |
| `getGameRoomList` | 房间列表为空！ |

### 9.3 异常推送

- 游戏过程中 `processOneEvent` 抛出的异常，会通过 `notifyPlayer` 推一条**字符串**到 `/user/queue`，前端可作为全局 toast 展示。

---

## 10. 前端接入注意事项

1. **状态推送是点对点**：`GameUpdateDto` 通过 `/user/queue` 推给「每个玩家各自一份」，`holeCards` 字段是隔离的。前端应主要从 `/user/queue` 维护游戏界面，而不是从 `/topic/game/{roomId}`。
2. **房间广播内容是混合的**：`/topic/game/{roomId}` 上会出现字符串、`Result`、`GameRoomVO` 三种 payload，请按字段判断并分别处理（详见 [7.1](#71-房间广播topictopicgameroomid)）。
3. **`getUserInfo` 会返回 `password` 字段**：前端拿到后请主动忽略该字段，不要回显到 UI。
4. **`ready` 端点会「自动开局」**：当所有玩家 `ready` 且 `>=3` 人时，后端会立即开一局新牌并向所有人推送 `GameUpdate`，无需前端额外触发。
5. **`raise` 时 `amount` 校验**：前端可信赖后端 `Result.msg` 中返回的「实际下注金额」（后端会自动把超出 `chips + betThisRound` 的部分截断）。
6. **`gameStarted` 期间不允许加入/全局结算/离开**：`joinGameRoom` 在游戏进行中会返回 `code=3` 的失败结果；`globalSettlement` 同理。
7. **后端状态是内存态**：`rooms` 是 `ConcurrentHashMap`，**重启即清空**。前端需在断线或长时间无响应时引导刷新大厅。
8. **WebSocket 跨域**：`WebSocketConfig` 启用了 `setAllowedOriginPatterns("*")`，开发环境可直连；生产环境请按运维策略收紧。

---

## 附录 A：端点速查

| 类别 | 客户端发送目的地 | 触发方法 |
| --- | --- | --- |
| 登录 | `POST /login` | 登录 |
| 鉴权 | `CONNECT /ws` Header `token` | 鉴权 |
| 用户 | `/app/getUserInfo` | 当前用户信息 |
| 排行榜 | `/app/getPointRank` | 积分排行榜 |
| 房间 | `/app/getGameRoomList` | 房间列表 |
| 房间 | `/app/game/{roomId}/create` | 创建房间 |
| 房间 | `/app/game/{roomId}/join` | 加入房间 |
| 房间 | `/app/game/{roomId}/leave` | 离开房间 |
| 房间 | `/app/game/{roomId}/getGameRoomInfo` | 房间详情 |
| 游戏 | `/app/game/{roomId}/getGameUpdate` | 拉取实时状态 |
| 游戏 | `/app/game/{roomId}/ready` | 准备 / 开始新局 |
| 游戏 | `/app/game/{roomId}/action` | 玩家行动 |
| 游戏 | `/app/game/{roomId}/globalSettlement` | 全局结算 |

## 附录 B：订阅速查

| 订阅地址 | 推送源 | 载荷类型 |
| --- | --- | --- |
| `/user/queue` | 点对点 | `Result` / `GameUpdateDto` / 字符串（异常） |
| `/topic/game/{roomId}` | 房间广播 | 字符串（提示） / `GameRoomVO` / `Result` |
