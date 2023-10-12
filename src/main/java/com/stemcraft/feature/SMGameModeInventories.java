package com.stemcraft.feature;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import com.stemcraft.core.SMDatabase;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.SMMessenger;
import com.stemcraft.core.event.SMEvent;
import com.stemcraft.core.serialize.SMSerialize;
import com.stemcraft.core.util.SMXPCalculator;

public class SMGameModeInventories extends SMFeature {
    @Override
    protected Boolean onEnable() {
        SMDatabase.runMigration("230805142700_CreateGameModeInventoriesTable", () -> {
            SMDatabase.prepareStatement(
            "CREATE TABLE IF NOT EXISTS gamemode_inventories (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "uuid TEXT NOT NULL," +
                "death INTEGER DEFAULT 0," +
                "location TEXT NOT NULL," +
                "gamemode TEXT NOT NULL," +
                "xp INTEGER NOT NULL," +
                "inventory TEXT NOT NULL," +
                "armour TEXT NOT NULL," +
                "enderchest TEXT NOT NULL," +
                "created TIMESTAMP DEFAULT CURRENT_TIMESTAMP)").executeUpdate();
        });

        SMDatabase.runMigration("230805160400_AddReasonToGameModeInventoriesTable", () -> {
            SMDatabase.prepareStatement(
                "ALTER TABLE gamemode_inventories " +
                "ADD COLUMN reason TEXT DEFAULT ''").executeUpdate();
        });

        SMDatabase.runMigration("230817181600_AddWorldToGameModeInventoriesTable", () -> {
            SMDatabase.prepareStatement(
                "ALTER TABLE gamemode_inventories " +
                "ADD COLUMN world TEXT DEFAULT 'world'").executeUpdate();
            
            SMDatabase.prepareStatement(
                    "UPDATE gamemode_inventories SET world = 'world' WHERE world = ''").executeUpdate();
        });

        SMEvent.register(PlayerGameModeChangeEvent.class, ctx -> {
            Player player = ctx.event.getPlayer();
            String newGameMode = ctx.event.getNewGameMode().toString();
            
            if(!this.SaveInventory(player, false, "Gamemode changed to " + newGameMode)) {
                SMMessenger.errorLocale(player, "GMI_FAILED");
                return;
            }
            
            if(!this.LoadLastInventory(player, newGameMode, player.getLocation().getWorld().getName())) {
                SMMessenger.errorLocale(player, "GMI_FAILED");
                return;
            }
        });

        SMEvent.register(PlayerDeathEvent.class, ctx -> {
            if(ctx.event.getEventName().equalsIgnoreCase("playerdeathevent")) {
                Player player = ctx.event.getEntity();
                this.SaveInventory(player, true, "Player death");
            }
        });

        SMEvent.register(PlayerTeleportEvent.class, ctx -> {
            Player player = ctx.event.getPlayer();

            this.SaveInventory(player, ctx.event.getFrom().getWorld().getName(), false, "Player Teleport");
            this.LoadLastInventory(player, player.getGameMode().toString(), ctx.event.getTo().getWorld().getName());
        });


        return true;
    }

    public Boolean LoadLastInventory(Player player) {
        String gameMode = player.getGameMode().toString();
        String world = player.getLocation().getWorld().getName();

        return this.LoadLastInventory(player, gameMode, world, false);
    }

    public Boolean LoadLastInventory(Player player, String gameMode, String world) {
        return this.LoadLastInventory(player, gameMode, world, false);
    }

    public Boolean LoadLastInventory(Player player, String gameMode, String world, Boolean death) {
        Boolean success = true;
        Boolean foundInventory = false;
        SMXPCalculator xpc = new SMXPCalculator(player);
        int xp = 0;
        String uuid = player.getUniqueId().toString();
        String inventoryContents = "";
        String armourContents = "";
        String enderChestContents = "";

        // Load player state
        try {
            PreparedStatement statement = SMDatabase.prepareStatement(
                    "SELECT * FROM gamemode_inventories WHERE uuid = ? AND death = ? AND gamemode = ? AND world = ? ORDER BY created DESC"
            );
            statement.setString(1, uuid);
            statement.setInt(2, 0);
            statement.setString(3, gameMode);
            statement.setString(4, world);
            
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                xp = resultSet.getInt("xp");
                inventoryContents = resultSet.getString("inventory");
                armourContents = resultSet.getString("armour");
                enderChestContents = resultSet.getString("enderchest");

                foundInventory = true;
            }
        } catch(Exception e) {
            e.printStackTrace();
            SMMessenger.errorLocale(player, "GMI_FAILED");
            success = false;
        }

        // Clear current potions
        player.getActivePotionEffects().forEach((effect) -> {
            player.removePotionEffect(effect.getType());
        });

        PlayerInventory playerInventory = player.getInventory();
        if(foundInventory) {
            playerInventory.setContents(SMSerialize.deserialize(ItemStack[].class, inventoryContents));
            playerInventory.setArmorContents(SMSerialize.deserialize(ItemStack[].class, armourContents));
            player.getEnderChest().setContents(SMSerialize.deserialize(ItemStack[].class, enderChestContents));
            xpc.setExp(xp);
        } else {
            playerInventory.clear();
            playerInventory.setBoots(null);
            playerInventory.setChestplate(null);
            playerInventory.setLeggings(null);
            playerInventory.setHelmet(null);
            player.getEnderChest().clear();
            xpc.setExp(0);
        }

        return success;
    }

    public Boolean SaveInventory(Player player) {
        return SaveInventory(player, player.getLocation().getWorld().getName(), false, "");
    }

    public Boolean SaveInventory(Player player, String reason) {
        return SaveInventory(player, player.getLocation().getWorld().getName(), false, reason);
    }

    public Boolean SaveInventory(Player player, Boolean death, String reason) {
        return SaveInventory(player, player.getLocation().getWorld().getName(), death, reason);
    }

    private Boolean SaveInventory(Player player, String world, Boolean death, String reason) {
        Boolean success = true;
        SMXPCalculator xpc = new SMXPCalculator(player);
        String uuid = player.getUniqueId().toString();
        String currentGameMode = player.getGameMode().toString();
        int xp = xpc.getCurrentExp();
        String inventoryContents = SMSerialize.serialize(player.getInventory().getContents());
        String armourContents = SMSerialize.serialize(player.getInventory().getArmorContents());
        String enderChestContents = SMSerialize.serialize(player.getEnderChest().getContents());
        String location = SMSerialize.serialize(player.getLocation());

        // Save player state
        try {
            PreparedStatement statement = SMDatabase.prepareStatement(
                    "INSERT INTO gamemode_inventories (uuid, death, location, gamemode, xp, inventory, armour, enderchest, reason, world) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );
            statement.setString(1, uuid);
            statement.setInt(2, death == true ? 1 : 0);
            statement.setString(3, location);
            statement.setString(4, currentGameMode);
            statement.setInt(5, xp);
            statement.setString(6, inventoryContents);
            statement.setString(7, armourContents);
            statement.setString(8, enderChestContents);
            statement.setString(9, reason);
            statement.setString(10, world);
            statement.executeUpdate();
            statement.close();
        } catch(Exception e) {
            e.printStackTrace();
            SMMessenger.errorLocale(player, "GMI_FAILED");
            success = false;
        }

        return success;
    }
}
