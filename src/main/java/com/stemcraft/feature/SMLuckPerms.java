package com.stemcraft.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.entity.Player;
import com.stemcraft.core.SMDependency;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.tabcomplete.SMTabComplete;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;

public class SMLuckPerms extends SMFeature {
    LuckPerms luckPerms;

    @Override
    public Boolean onLoad() {
        if(!super.onLoad()) {
            return false;
        }

        if(!SMDependency.dependencyLoaded("LuckPerms")) {
            return false;
        }

        return true;
    }

    @Override
    protected Boolean onEnable() {
        this.luckPerms = LuckPermsProvider.get();

        SMTabComplete.register("groups", () -> {
            return this.groups();
        });

        return true;
    }

    /**
     * Get a list of groups in LuckPerms
     * @return
     */
    public List<String> groups() {
        List<String> groupList = new ArrayList<>();

        if(this.isEnabled()) {
            Set<Group> groups = this.luckPerms.getGroupManager().getLoadedGroups();
            for (Group group : groups) {
                groupList.add(group.getName());
            }
        }

        return groupList;
    }

    /**
     * Check that a group exists in LuckPerms
     * @param group
     * @return
     */
    public Boolean groupExists(String group) {
        return this.groups().contains(group);
    }

    /**
     * Check that a player exists in a LuckPerms group
     * @param player
     * @param group
     * @return
     */
    public Boolean playerInGroup(Player player, String group) {
        if(this.isEnabled()) {
            return player.hasPermission("group." + group);
        }

        return false;
    }
}
