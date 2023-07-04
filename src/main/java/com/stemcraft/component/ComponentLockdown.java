package com.stemcraft.component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ComponentLockdown extends SMComponent {
    public final static Set<UUID> blockedPlayers = new HashSet<>();
}
