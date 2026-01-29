# CrossAnywhere

CrossAnywhere is a Paper/Spigot teleport plugin for Minecraft 1.21+ (Java 21). It provides personal/global waypoints, TPA requests, back, safety checks, cooldowns, and configurable costs. All messages use Adventure/MiniMessage and support i18n.

## Features

- Personal and global waypoints (with yaw/pitch and description)
- Waypoint listing with clickable teleport/delete/edit buttons
- TPA / TPAHere requests with timeout and duplicate policy
- Back point recorded before teleports and on death (configurable)
- World whitelist and cross-world permission gate
- Teleport cooldowns (per feature)
- Teleport costs (exp and/or item)
- Safety checks with confirm or nearby-safe search
- Easy teleport: `/ca <name>` (personal > global > player)
- Shortcut commands (optional override for `/tp`)

## Requirements

- Paper 1.21+
- Java 21

## Installation

1. Build the plugin: `./gradlew build`
2. Put the jar in `plugins/`.
3. Start the server to generate config and message files.
4. Edit `config.yml` and `messages_*.yml` as needed.

## Commands

Main command: `/ca` (alias `/stp`)

Waypoints (personal):
- `/ca setp|setpersonal [-f] <name> [desc...]`
- `/ca tpp|tpersonal <name>`
- `/ca delp|delpersonal <name>`
- `/ca listp|listpersonal`
- `/ca descp <name> <desc...>`

Waypoints (global):
- `/ca setg|setglobal [-f] <name> [desc...]`
- `/ca tpg|tglobal <name>`
- `/ca delg|delglobal <name>`
- `/ca listg|listglobal`
- `/ca descg <name> <desc...>`

Other:
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
    - `/ca <name>` (easy_tp=true)

Shortcut commands:
- `/setp /tpp /delp /listp /descp`
- `/setg /tpg /delg /listg /descg`
- `/tplist`
- `/back`
- `/tpa /tpahere /tphere /tpaccept /tpdeny /tpcancel`
- `/tpconfirm /tpcancelconfirm`
- `/tp` (only if `commands.override_tp=true`)

## Permissions

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

## Data Files

- `plugins/CrossAnywhere/personal_waypoints.json`
- `plugins/CrossAnywhere/global_waypoints.json`
- `plugins/CrossAnywhere/stp_uuid_map.json` (optional name -> UUID map for import)
- `plugins/CrossAnywhere/stp_world_map.json` (optional dimension -> world map for import)

## Configuration

See `config.yml` for full options.

Key sections:
- `worlds`: whitelist
- `easy_tp`: enable `/ca <name>`
- `waypoint_name_max_length`, `allow_unicode_names`
- `personal_max_waypoints`, `global_max_waypoints`
- `cooldown`: per-feature cooldowns
- `cost`: exp and item costs (with crossworld handling)
- `safety_check`: confirm or nearby-safe search
- `commands.override_tp`: whether to register `/tp`

## i18n

- `messages_en_US.yml`
- `messages_zh_CN.yml`

## Notes

- All shortcut commands reuse the same checks (world, permission, cooldown, cost, safety).
- TPA requests and safety confirmations are in-memory and reset on restart.
