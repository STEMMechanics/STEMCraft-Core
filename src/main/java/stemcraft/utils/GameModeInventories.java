package stemcraft.utils;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class GameModeInventories extends JavaPlugin implements Listener {

    private File inventoriesFolder;

    @Override
    public void onEnable() {
        inventoriesFolder = new File(getDataFolder(), "inventories");
        if (!inventoriesFolder.exists()) {
            inventoriesFolder.mkdirs();
        }

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        saveAllInventories();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        loadInventory(player);
        loadEnderChest(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        saveInventory(player);
        saveEnderChest(player);
    }

    @EventHandler
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        GameMode newGameMode = event.getNewGameMode();
        handleGameModeChange(player, newGameMode);
    }

    private File getPlayerInventoryFile(Player player) {
        return new File(inventoriesFolder, player.getUniqueId() + ".yml");
    }

    private void loadInventory(Player player) {
        File inventoryFile = getPlayerInventoryFile(player);
        if (inventoryFile.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(inventoryFile);
            Inventory inventory = player.getInventory();
            inventory.clear();

            for (String key : config.getKeys(false)) {
                int slot = Integer.parseInt(key);
                ItemStack item = config.getItemStack(key);
                inventory.setItem(slot, item);
            }

            player.updateInventory();
        }
    }

    private void saveInventory(Player player) {
        File inventoryFile = getPlayerInventoryFile(player);
        FileConfiguration config = YamlConfiguration.loadConfiguration(inventoryFile);
        Inventory inventory = player.getInventory();

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item != null) {
                config.set(String.valueOf(slot), item);
            } else {
                config.set(String.valueOf(slot), null);
            }
        }

        try {
            config.save(inventoryFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadEnderChest(Player player) {
        File enderChestFile = new File(inventoriesFolder, player.getUniqueId() + "_enderchest.yml");
        if (enderChestFile.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(enderChestFile);
            Inventory enderChest = player.getEnderChest();
            enderChest.clear();

            for (String key : config.getKeys(false)) {
                int slot = Integer.parseInt(key);
                ItemStack item = config.getItemStack(key);
                enderChest.setItem(slot, item);
            }
        }
    }

    private void saveEnderChest(Player player) {
        File enderChestFile = new File(inventoriesFolder, player.getUniqueId() + "_enderchest.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(enderChestFile);
        Inventory enderChest = player.getEnderChest();
    
        for (int slot = 0; slot < enderChest.getSize(); slot++) {
            ItemStack item = enderChest.getItem(slot);
            if (item != null) {
                config.set(String.valueOf(slot), item);
            } else {
                config.set(String.valueOf(slot), null);
            }
        }
    
        try {
            config.save(enderChestFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void saveAllInventories() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            saveInventory(player);
            saveEnderChest(player);
        }
    }
    
    private void handleGameModeChange(Player player, GameMode newGameMode) {
        // Save current inventory and ender chest
        saveInventory(player);
        saveEnderChest(player);
    
        if (newGameMode == GameMode.CREATIVE) {
            // Switch to creative mode inventory
            loadInventory(player);
        } else {
            // Load survival mode inventory
            File inventoryFile = getPlayerInventoryFile(player);
            if (inventoryFile.exists()) {
                loadInventory(player);
            } else {
                // If survival inventory does not exist, clear the inventory
                player.getInventory().clear();
            }
        }
    
        // Load ender chest
        loadEnderChest(player);
    }
}