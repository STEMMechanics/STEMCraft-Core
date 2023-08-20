package com.stemcraft.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import com.stemcraft.STEMCraft;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;

public class SMLuckPerms extends SMFeature {
    LuckPerms luckPerms;

    @Override
    public Boolean onLoad(STEMCraft plugin) {
        if(!super.onLoad(plugin)) {
            return false;
        }

        if(!this.plugin.getDependManager().getDependencyLoaded("LuckPerms")) {
            return false;
        }

        return true;
    }

    @Override
    protected Boolean onEnable() {
        this.luckPerms = LuckPermsProvider.get();

        this.plugin.getCommandManager().registerTabPlaceholder("groups", (Server server, String match) -> {
            return this.groups();
        });

        return true;
    }

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

    public Boolean groupExists(String group) {
        return this.groups().contains(group);
    }

    public Boolean playerInGroup(Player player, String group) {
        if(this.isEnabled()) {
            return player.hasPermission("group." + group);
        }

        return false;
    }
}
