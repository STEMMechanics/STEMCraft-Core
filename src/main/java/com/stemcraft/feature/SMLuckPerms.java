package com.stemcraft.feature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.tabcomplete.SMTabComplete;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PermissionNode;

public class SMLuckPerms extends SMFeature {
    private static String dependantName = "LuckPerms";
    private static LuckPerms luckPerms = null;

    @Override
    public Boolean onLoad() {
        if (!super.onLoad()) {
            return false;
        }

        if (Bukkit.getPluginManager().getPlugin(SMLuckPerms.dependantName) == null) {
            STEMCraft.warning(
                SMLuckPerms.dependantName + " is not loaded. Features requiring this plugin won't be available");
            return false;
        }

        return true;
    }

    @Override
    protected Boolean onEnable() {
        SMLuckPerms.luckPerms = LuckPermsProvider.get();

        SMTabComplete.register("group", () -> {
            return SMLuckPerms.groups();
        });

        return true;
    }

    /**
     * Get a list of groups in LuckPerms
     * 
     * @return
     */
    public static List<String> groups() {
        List<String> groupList = new ArrayList<>();

        if (luckPerms != null) {
            Set<Group> groups = luckPerms.getGroupManager().getLoadedGroups();
            for (Group group : groups) {
                groupList.add(group.getName());
            }
        }

        return groupList;
    }

    /**
     * Check that a group exists in LuckPerms
     * 
     * @param group
     * @return
     */
    public static Boolean groupExists(String group) {
        return groups().contains(group);
    }

    /**
     * Check that a player exists in a LuckPerms group
     * 
     * @param player
     * @param group
     * @return
     */
    public static Boolean playerInGroup(Player player, String group) {
        if (luckPerms != null) {
            return player.hasPermission("group." + group);
        }

        return false;
    }

    /**
     * Add a permission to a player
     * 
     * @param player
     * @param permission
     * @return
     */
    public static void addPermission(Player player, String permission) {
        if (luckPerms != null) {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                Node node = PermissionNode.builder(permission).build();
                user.data().add(node);
                luckPerms.getUserManager().saveUser(user);
            }
        }
    }

    /**
     * Remove a permission from a player
     * 
     * @param player
     * @param permission
     * @return
     */
    public static void removePermission(Player player, String permission) {
        if (luckPerms != null) {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                Node node = PermissionNode.builder(permission).build();
                user.data().remove(node);
                luckPerms.getUserManager().saveUser(user);
            }
        }
    }

    /**
     * Add a group to a player
     * 
     * @param player
     * @param group
     * @return
     */
    public static void addGroup(Player player, String group) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());

        if (user != null) {
            InheritanceNode groupNode = InheritanceNode.builder(group).build();
            user.data().add(groupNode);
            luckPerms.getUserManager().saveUser(user);
        }
    }

    /**
     * Remove a group from a player
     * 
     * @param player
     * @param group
     * @return
     */
    public static void removeGroup(Player player, String group) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());

        if (user != null) {
            InheritanceNode groupNode = InheritanceNode.builder(group).build();
            user.data().remove(groupNode);
            luckPerms.getUserManager().saveUser(user);
        }
    }

    /**
     * List player direct groups
     * 
     * @param player
     * @param group
     * @return
     */
    public static Collection<String> listGroups(Player player) {
        Collection<String> groups = null;
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());

        if (user != null) {
            groups = user.getNodes().stream()
                .filter(NodeType.INHERITANCE::matches)
                .map(NodeType.INHERITANCE::cast)
                .map(InheritanceNode::getGroupName)
                .collect(Collectors.toSet());
        }

        return groups;
    }
}
