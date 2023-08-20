package com.stemcraft.feature;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.PlayerInventory;
import com.stemcraft.SMSerialize;
import com.stemcraft.library.SMXPCalculator;

public class SMGameModeInventories extends SMFeature {
    @Override
    protected Boolean onEnable() {
        this.plugin.getDatabaseManager().addMigration("230805142700_CreateGameModeInventoriesTable", (databaseManager) -> {
            databaseManager.prepareStatement(
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

        this.plugin.getDatabaseManager().addMigration("230805160400_AddReasonToGameModeInventoriesTable", (databaseManager) -> {
            databaseManager.prepareStatement(
                "ALTER TABLE gamemode_inventories " +
                "ADD COLUMN reason TEXT DEFAULT ''").executeUpdate();
        });

        this.plugin.getDatabaseManager().addMigration("230817181600_AddWorldToGameModeInventoriesTable", (databaseManager) -> {
            databaseManager.prepareStatement(
                "ALTER TABLE gamemode_inventories " +
                "ADD COLUMN world TEXT DEFAULT 'world'").executeUpdate();
            
            databaseManager.prepareStatement(
                    "UPDATE gamemode_inventories SET world = 'world' WHERE world = ''").executeUpdate();
        });

        this.plugin.getLanguageManager().registerPhrase("GMI_FAILED", "&cThe server failed updating your inventory");

        this.plugin.getEventManager().registerEvent(PlayerGameModeChangeEvent.class, (listener, rawEvent) -> {
            PlayerGameModeChangeEvent event = (PlayerGameModeChangeEvent)rawEvent;
            Player player = event.getPlayer();
            String newGameMode = event.getNewGameMode().toString();
            
            if(!this.SaveInventory(player, false, "Gamemode changed to " + newGameMode)) {
                this.plugin.getLanguageManager().sendPhrase(player, "GMI_FAILED");
                return;
            }
            
            if(!this.LoadLastInventory(player, newGameMode, player.getLocation().getWorld().getName())) {
                this.plugin.getLanguageManager().sendPhrase(player, "GMI_FAILED");
                return;
            }
        });

        this.plugin.getEventManager().registerEvent(PlayerDeathEvent.class, (listener, rawEvent) -> {
            if(rawEvent.getEventName().equalsIgnoreCase("playerdeathevent")) {
                PlayerDeathEvent event = (PlayerDeathEvent)rawEvent;
                Player player = event.getEntity();
                this.SaveInventory(player, true, "Player death");
            }
        });

        this.plugin.getEventManager().registerEvent(PlayerTeleportEvent.class, (listener, rawEvent) -> {
            PlayerTeleportEvent event = (PlayerTeleportEvent)rawEvent;
            Player player = event.getPlayer();

            this.SaveInventory(player, event.getFrom().getWorld().getName(), false, "Player Teleport");
            this.LoadLastInventory(player, player.getGameMode().toString(), event.getTo().getWorld().getName());
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
            PreparedStatement statement = this.plugin.getDatabaseManager().prepareStatement(
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
            this.plugin.getLanguageManager().sendPhrase(player, "GMI_FAILED");
            success = false;
        }

        // Clear current potions
        player.getActivePotionEffects().forEach((effect) -> {
            player.removePotionEffect(effect.getType());
        });

        PlayerInventory playerInventory = player.getInventory();
        if(foundInventory) {
            playerInventory.setContents(SMSerialize.deserializeItemStackArray(inventoryContents));
            playerInventory.setArmorContents(SMSerialize.deserializeItemStackArray(armourContents));
            player.getEnderChest().setContents(SMSerialize.deserializeItemStackArray(enderChestContents));
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
            PreparedStatement statement = this.plugin.getDatabaseManager().prepareStatement(
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
            this.plugin.getLanguageManager().sendPhrase(player, "GMI_FAILED");
            success = false;
        }

        return success;
    }
}
