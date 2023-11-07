package com.stemcraft.feature;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import com.stemcraft.core.SMCommon;
import com.stemcraft.core.SMDatabase;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.SMMessenger;
import com.stemcraft.core.command.SMCommand;
import com.stemcraft.core.tabcomplete.SMTabComplete;

/**
 * Player Ban/Kick/Warning/Note/Mute (moderation) management
 */
public class SMModerate extends SMFeature {
    private static Map<String, Long> banList = new HashMap<>();

    public static Integer ACTION_BAN = 1;
    public static Integer ACTION_KICK = 2;
    public static Integer ACTION_WARN = 3;
    public static Integer ACTION_MUTE = 4;
    public static Integer ACTION_NOTE = 5;

    /**
     * Called when the feature is requested to be enabled.
     * 
     * @return If the feature enabled successfully.
     */
    @Override
    protected Boolean onEnable() {

        SMDatabase.runMigration("231105075800_CreateModerateTable", () -> {
            SMDatabase.prepareStatement(
                "CREATE TABLE IF NOT EXISTS moderate (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "uuid TEXT NOT NULL," +
                    "byUuid TEXT NOT NULL," +
                    "type INTEGER NOT NULL," +
                    "note TEXT NOT NULL," +
                    "duration INTEGER NOT NULL," +
                    "created TIMESTAMP DEFAULT CURRENT_TIMESTAMP)")
                .executeUpdate();
        });

        new SMCommand("book")
            .tabComplete("new")
            .tabComplete("save")
            .tabComplete("get", "{book}")
            .tabComplete("show", "{book}", "{player}")
            .tabComplete("del", "{book}")
            .tabComplete("unlock")
            .permission("stemcraft.book")
            .action(ctx -> {
                // Check there are args
                ctx.checkArgsLocale(1, "BOOK_USAGE");

                String sub = ctx.args.get(0).toLowerCase();

                // Sub command - new
                if ("new".equals(sub)) {
                    ctx.checkNotConsole();
                    ctx.checkPermission("stemcraft.book.edit");

                    Material material = Material.getMaterial("BOOK_AND_QUILL");
                    if (material == null)
                        material = Material.getMaterial("WRITABLE_BOOK");
                    if (material == null)
                        throw new UnsupportedOperationException("Something went wrong with Bukkit Material!");

                    ItemStack item = new ItemStack(material);
                    PlayerInventory inventory = ctx.player.getInventory();
                    Map<Integer, ItemStack> result = inventory.addItem(item);
                    if (!result.isEmpty()) {
                        ctx.returnErrorLocale("BOOK_INVENTORY_FULL");
                    } else {
                        ctx.returnInfoLocale("BOOK_GIVEN_NEW");
                    }

                    // Sub command - save
                }
            })
            .register();

        return true;
    }

    private static void updateBanList() {
        // PreparedStatement statement = SMDatabase.prepareStatement(
        // "SELECT uuid, duration, created, reason FROM moderate WHERE action = ?");
        // statement.setInt(1, ACTION_BAN);
        // ResultSet resultSet = statement.executeQuery();

        // banList = new
        // while (resultSet.next()) {
        // String name = resultSet.getString("name");
        // this.cacheList.add(name);
        // }
    }
}
