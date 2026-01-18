package io.github.qianmo0721.crossAnywhere.teleport;

import io.github.qianmo0721.crossAnywhere.manager.CooldownManager;

public enum TeleportType {
    WAYPOINT_PERSONAL(CooldownManager.CooldownType.WAYPOINT),
    WAYPOINT_GLOBAL(CooldownManager.CooldownType.WAYPOINT),
    TP(CooldownManager.CooldownType.TP),
    TPHERE(CooldownManager.CooldownType.TP),
    TPA(CooldownManager.CooldownType.TP),
    TPAHERE(CooldownManager.CooldownType.TP),
    BACK(CooldownManager.CooldownType.BACK);

    private final CooldownManager.CooldownType cooldownType;

    TeleportType(CooldownManager.CooldownType cooldownType) {
        this.cooldownType = cooldownType;
    }

    public CooldownManager.CooldownType getCooldownType() {
        return cooldownType;
    }
}
