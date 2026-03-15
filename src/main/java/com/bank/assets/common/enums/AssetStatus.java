package com.bank.assets.common.enums;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum AssetStatus {
    REGISTERED,
    ASSIGNED,
    IN_REPAIR,
    LOST,
    WRITTEN_OFF;

    public boolean canBeAssigned() {
        return this == REGISTERED || this == IN_REPAIR;
    }

    public boolean canTransitionTo(AssetStatus target) {
        if (target == ASSIGNED) return false;
        return ALLOWED.getOrDefault(this, EnumSet.noneOf(AssetStatus.class)).contains(target);
    }

    private static final Map<AssetStatus, Set<AssetStatus>> ALLOWED = new EnumMap<>(AssetStatus.class);

    static {
        ALLOWED.put(REGISTERED, EnumSet.of(IN_REPAIR, LOST, WRITTEN_OFF));
        ALLOWED.put(ASSIGNED, EnumSet.of(REGISTERED, IN_REPAIR, LOST, WRITTEN_OFF));
        ALLOWED.put(IN_REPAIR, EnumSet.of(REGISTERED, LOST, WRITTEN_OFF));
        ALLOWED.put(LOST, EnumSet.of(WRITTEN_OFF));
        ALLOWED.put(WRITTEN_OFF, EnumSet.noneOf(AssetStatus.class));
    }
}
