# CrossAnywhere

CrossAnywhere is a Paper/Spigot teleport plugin for Minecraft 1.20+ (Java 21). It provides personal/global waypoints,
TPA requests, back, safety checks, cooldowns, and configurable costs. All messages use Adventure/MiniMessage and support
i18n.

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

- Paper 1.20+
- Java 21

## Installation

1. Build the plugin: `./gradlew build`
2. Put the jar in `plugins/`.
3. Start the server to generate config and message files.
4. Edit `config.yml` and `messages_*.yml` as needed.

## Commands

Main command: `/ca` (alias `/stp`)

Waypoints (personal):

- `/ca setp|setpersonal [-f] <name> [desc...]` - Create or update a personal waypoint at your current location; use `-f`
  to overwrite.
- `/ca tpp|tpersonal <name>` - Teleport to one of your personal waypoints.
- `/ca delp|delpersonal <name>` - Delete one of your personal waypoints.
- `/ca listp|listpersonal` - List your personal waypoints.
- `/ca descp <name> <desc...>` - Set or update the description of a personal waypoint.

Waypoints (global):

- `/ca setg|setglobal [-f] <name> [desc...]` - Create or update a global waypoint at your current location; use `-f` to
  overwrite.
- `/ca tpg|tglobal <name>` - Teleport to a global waypoint.
- `/ca delg|delglobal <name>` - Delete a global waypoint.
- `/ca listg|listglobal` - List global waypoints.
- `/ca descg <name> <desc...>` - Set or update the description of a global waypoint.

Other:

- `/ca list` - List both personal and global waypoints.
- `/ca tp <player>` - Teleport yourself to the target player (admin permission).
- `/ca tphere <player>` - Teleport the target player to you (admin permission).
- `/ca tpa <player>` - Send a teleport request to the target player.
- `/ca tpahere <player>` - Ask the target player to teleport to you.
- `/ca cancel` - Cancel all outgoing TPA requests you sent.
- `/ca accept|allow [player]` - Accept the latest request or a specific player's request.
- `/ca deny|reject [player]` - Deny the latest request or a specific player's request.
- `/ca tpaallow <player>` - Allow a player to teleport to you directly without confirmation.
- `/ca tpadisallow <player>` - Remove a player from your direct-teleport allowlist.
- `/ca tpaallowlist` - Show your direct-teleport allowlist.
- `/ca back` - Return to your last recorded location.
- `/ca confirm` - Confirm a pending unsafe teleport.
- `/ca cancelconfirm` - Cancel a pending unsafe teleport confirmation.
- `/ca importstp [file] [--include-back] [--offline-uuid|--raw-uuid|--auto-uuid] [--clear]` - Import MCDR STP data from
  a JSON file in the plugin data folder.
- `/ca <name>` (easy_tp=true) - Easy teleport: personal > global > player.

Shortcut commands:

- `/setp` - Shortcut for `/ca setp`.
- `/tpp` - Shortcut for `/ca tpp`.
- `/delp` - Shortcut for `/ca delp`.
- `/listp` - Shortcut for `/ca listp`.
- `/descp` - Shortcut for `/ca descp`.
- `/setg` - Shortcut for `/ca setg`.
- `/tpg` - Shortcut for `/ca tpg`.
- `/delg` - Shortcut for `/ca delg`.
- `/listg` - Shortcut for `/ca listg`.
- `/descg` - Shortcut for `/ca descg`.
- `/tplist` - Shortcut for `/ca list`.
- `/back` - Shortcut for `/ca back`.
- `/tpa` - Shortcut for `/ca tpa`.
- `/tpahere` - Shortcut for `/ca tpahere`.
- `/tphere` - Shortcut for `/ca tphere`.
- `/tpaccept` - Shortcut for `/ca accept`.
- `/tpdeny` - Shortcut for `/ca deny`.
- `/tpcancel` - Shortcut for `/ca cancel`.
- `/tpconfirm` - Shortcut for `/ca confirm`.
- `/tpcancelconfirm` - Shortcut for `/ca cancelconfirm`.
- `/tp` (only if `commands.override_tp=true`) - Override vanilla `/tp` with CrossAnywhere handling.

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
- `crossanywhere.tpa.allowlist`
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
- `plugins/CrossAnywhere/tpa_allowlist.json`
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
