# CrossAnywhere

CrossAnywhere 是一个面向 Paper/Spigot 的传送插件（Minecraft 1.21+，Java 21）。提供个人/全局传送点、TPA 请求、返回点、安全检查、冷却与消耗等功能，所有提示使用 Adventure/MiniMessage 并支持中英双语。

## 功能概览

- 个人与全局传送点（含朝向、描述）
- 列表带可点击按钮（传送/删除/编辑描述）
- TPA/TPAHere 请求，支持超时与重复策略
- 传送前记录 back 点，支持死亡记录（可配置）
- 世界白名单与跨世界权限控制
- 传送冷却（按功能区分）
- 传送消耗（经验与物品，可同时启用）
- 安全检查（确认或附近安全点）
- easy_tp：`/ca <name>`（个人 > 全局 > 玩家）
- 快捷命令（可选覆盖 `/tp`）

## 环境要求

- Paper 1.21+
- Java 21

## 安装

1. 构建插件：`./gradlew build`
2. 将 jar 放入 `plugins/`。
3. 启动服务器生成配置与语言文件。
4. 根据需要修改 `config.yml` 与 `messages_*.yml`。

## 命令

主命令：`/ca`（别名 `/stp`）

个人传送点：
- `/ca setp|setpersonal [-f] <name> [desc...]`
- `/ca tpp|tpersonal <name>`
- `/ca delp|delpersonal <name>`
- `/ca listp|listpersonal`
- `/ca descp <name> <desc...>`

全局传送点：
- `/ca setg|setglobal [-f] <name> [desc...]`
- `/ca tpg|tglobal <name>`
- `/ca delg|delglobal <name>`
- `/ca listg|listglobal`
- `/ca descg <name> <desc...>`

其他：
- `/ca list`
- `/ca tp <player>`
- `/ca tphere <player>`
- `/ca tpa <player>`
- `/ca tpahere <player>`
- `/ca cancel`
- `/ca accept|allow [player]`
- `/ca deny|reject [player]`
    - `/ca back`
    - `/ca confirm`
    - `/ca cancelconfirm`
    - `/ca importstp [file] [--include-back] [--offline-uuid|--raw-uuid|--auto-uuid] [--clear]`
    - `/ca <name>`（easy_tp=true）

快捷命令：
- `/setp /tpp /delp /listp /descp`
- `/setg /tpg /delg /listg /descg`
- `/tplist`
- `/back`
- `/tpa /tpahere /tphere /tpaccept /tpdeny /tpcancel`
- `/tpconfirm /tpcancelconfirm`
- `/tp`（仅当 `commands.override_tp=true`）

## 权限节点

- `crossanywhere.personal`
- `crossanywhere.personal.tp`
- `crossanywhere.global`
- `crossanywhere.global.tp`
- `crossanywhere.list`
- `crossanywhere.tp`
- `crossanywhere.tphere`
- `crossanywhere.tpa`
- `crossanywhere.tpahere`
- `crossanywhere.back`
- `crossanywhere.easy`
- `crossanywhere.crossworld`
- `crossanywhere.cooldown.bypass`
- `crossanywhere.cost.bypass`
- `crossanywhere.safety.bypass`
- `crossanywhere.admin`

## 数据文件

- `plugins/CrossAnywhere/personal_waypoints.json`
- `plugins/CrossAnywhere/global_waypoints.json`
- `plugins/CrossAnywhere/stp_uuid_map.json`（可选：导入时的 name -> UUID 映射）
- `plugins/CrossAnywhere/stp_world_map.json`（可选：导入时的 dimension -> world 映射）

## 配置说明

请查看 `config.yml`。

关键项：
- `worlds`：世界白名单
- `easy_tp`：开启 `/ca <name>`
- `waypoint_name_max_length`，`allow_unicode_names`
- `personal_max_waypoints`，`global_max_waypoints`
- `cooldown`：各功能冷却
- `cost`：经验/物品消耗与跨世界折算
- `safety_check`：安全检查模式与搜索半径
- `commands.override_tp`：是否注册 `/tp`

## 语言文件

- `messages_en_US.yml`
- `messages_zh_CN.yml`

## 备注

- 所有快捷命令均复用同一套权限、冷却、消耗与安全检查。
- TPA 请求与安全确认状态仅保存在内存中，重启后清空。
