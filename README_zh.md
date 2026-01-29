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

- `/ca setp|setpersonal [-f] <name> [desc...]` - 在当前位置创建或更新个人传送点，`-f` 强制覆盖。
- `/ca tpp|tpersonal <name>` - 传送到你的个人传送点。
- `/ca delp|delpersonal <name>` - 删除你的个人传送点。
- `/ca listp|listpersonal` - 列出你的个人传送点。
- `/ca descp <name> <desc...>` - 设置或更新个人传送点描述。

全局传送点：

- `/ca setg|setglobal [-f] <name> [desc...]` - 在当前位置创建或更新全局传送点，`-f` 强制覆盖。
- `/ca tpg|tglobal <name>` - 传送到全局传送点。
- `/ca delg|delglobal <name>` - 删除全局传送点。
- `/ca listg|listglobal` - 列出全局传送点。
- `/ca descg <name> <desc...>` - 设置或更新全局传送点描述。

其他：

- `/ca list` - 同时列出个人与全局传送点。
- `/ca tp <player>` - 传送到指定玩家（管理员权限）。
- `/ca tphere <player>` - 将指定玩家传送到你身边（管理员权限）。
- `/ca tpa <player>` - 向指定玩家发送传送请求。
- `/ca tpahere <player>` - 请求指定玩家传送到你这里。
- `/ca cancel` - 取消你发出的所有 TPA 请求。
- `/ca accept|allow [player]` - 接受最新请求或指定玩家的请求。
- `/ca deny|reject [player]` - 拒绝最新请求或指定玩家的请求。
- `/ca back` - 返回上一个记录的位置。
- `/ca confirm` - 确认危险传送。
- `/ca cancelconfirm` - 取消危险传送确认。
- `/ca importstp [file] [--include-back] [--offline-uuid|--raw-uuid|--auto-uuid] [--clear]` - 从插件数据目录导入 MCDR
  STP JSON 数据。
- `/ca <name>`（easy_tp=true） - 快捷传送：个人 > 全局 > 玩家。

快捷命令：

- `/setp` - `/ca setp` 的快捷命令。
- `/tpp` - `/ca tpp` 的快捷命令。
- `/delp` - `/ca delp` 的快捷命令。
- `/listp` - `/ca listp` 的快捷命令。
- `/descp` - `/ca descp` 的快捷命令。
- `/setg` - `/ca setg` 的快捷命令。
- `/tpg` - `/ca tpg` 的快捷命令。
- `/delg` - `/ca delg` 的快捷命令。
- `/listg` - `/ca listg` 的快捷命令。
- `/descg` - `/ca descg` 的快捷命令。
- `/tplist` - `/ca list` 的快捷命令。
- `/back` - `/ca back` 的快捷命令。
- `/tpa` - `/ca tpa` 的快捷命令。
- `/tpahere` - `/ca tpahere` 的快捷命令。
- `/tphere` - `/ca tphere` 的快捷命令。
- `/tpaccept` - `/ca accept` 的快捷命令。
- `/tpdeny` - `/ca deny` 的快捷命令。
- `/tpcancel` - `/ca cancel` 的快捷命令。
- `/tpconfirm` - `/ca confirm` 的快捷命令。
- `/tpcancelconfirm` - `/ca cancelconfirm` 的快捷命令。
- `/tp`（仅当 `commands.override_tp=true`）- 覆盖原版 `/tp`，使用 CrossAnywhere 的处理。

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
