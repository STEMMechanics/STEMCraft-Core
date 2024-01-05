package com.stemcraft.feature;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMCommon;
import com.stemcraft.core.SMDatabase;
import com.stemcraft.core.SMDependency;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.SMJson;
import com.stemcraft.core.event.SMEvent;
import dev.lone.itemsadder.api.CustomFurniture;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.Events.FurnitureBreakEvent;
import dev.lone.itemsadder.api.Events.FurnitureInteractEvent;

public class SMGraves extends SMFeature {
    private HashMap<Inventory, UUID> trackedInventories = new HashMap<>();
    private List<Location> spawnCancellations = new ArrayList<>();

    @Override
    protected Boolean onEnable() {
        return false;
        // SMDatabase.runMigration("230804162200_CreateGravestoneTable", () -> {
        // SMDatabase.prepareStatement(
        // "CREATE TABLE IF NOT EXISTS graves (" +
        // "id TEXT PRIMARY KEY," +
        // "data TEXT)")
        // .executeUpdate();
        // });

        // SMDatabase.runMigration("231023201700_UpdateGravestoneTable", () -> {
        // // Lazy
        // SMDatabase.prepareStatement(
        // "DROP TABLE graves").executeUpdate();

        // SMDatabase.prepareStatement(
        // "CREATE TABLE IF NOT EXISTS graves (" +
        // "id TEXT PRIMARY KEY," +
        // "title TEXT," +
        // "contents TEXT)")
        // .executeUpdate();
        // });

        // SMDependency.onDependencyReady("itemsadder", () -> {
        // SMEvent.register(FurnitureInteractEvent.class, ctx -> {
        // if (ctx.event.getNamespacedID().equalsIgnoreCase("stemcraft:grave")) {
        // UUID id = ctx.event.getFurniture().getEntity().getUniqueId();

        // Inventory inventory = loadGrave(id);
        // if (inventory == null) {
        // String title = "Unknown Grave";
        // inventory = Bukkit.createInventory(null, 54, title);
        // this.saveGrave(id, inventory.getContents(), title);
        // }

        // if (inventory != null) {
        // ctx.event.getPlayer().openInventory(inventory);
        // this.trackedInventories.put(inventory, id);
        // }
        // }
        // });

        // SMEvent.register(FurnitureBreakEvent.class, ctx -> {
        // if (ctx.event.getNamespacedID().equalsIgnoreCase("stemcraft:grave")) {
        // Entity grave = ctx.event.getFurniture().getEntity();
        // World world = grave.getWorld();
        // Location location = grave.getLocation();
        // UUID id = grave.getUniqueId();

        // ItemStack wall = new ItemStack(Material.MOSSY_STONE_BRICK_WALL);
        // ItemStack slab = new ItemStack(Material.MOSSY_STONE_BRICK_SLAB);
        // Inventory inventory = loadGrave(id);

        // if (!SMCommon.playerHasSilkTouch(ctx.event.getPlayer())) {
        // spawnCancellations.add(location);
        // // break grave and drop stone brick parts
        // world.dropItemNaturally(location, wall);
        // world.dropItemNaturally(location, slab);
        // }

        // // drop grave items
        // if (inventory != null) {
        // for (ItemStack item : inventory.getContents()) {
        // if (item != null && item.getType() != Material.AIR) {
        // world.dropItemNaturally(location, item);
        // }
        // }
        // }

        // this.clearGrave(id);
        // }
        // });

        // SMEvent.register(PlayerDeathEvent.class, ctx -> {
        // if (ctx.event.getEventName().equalsIgnoreCase("playerdeathevent")) {
        // Player player = ctx.event.getEntity();

        // if (player.getGameMode() != GameMode.SURVIVAL) {
        // return;
        // }

        // Location graveLocation = SMCommon.findSafeLocation(player.getLocation(), 64);
        // if (graveLocation == null) {
        // STEMCraft.info("No suitable location was found near the player for a grave");
        // return;
        // }

        // String title = player.getName() + (player.getName().endsWith("s") ? "'" : "'s") + " Grave";
        // Inventory inventory = Bukkit.createInventory(null, 54, title);
        // ctx.event.getDrops().forEach((itemStack) -> {
        // inventory.addItem(itemStack.clone());
        // });

        // try {
        // CustomFurniture grave =
        // CustomFurniture.spawn("stemcraft:grave", graveLocation.getBlock());
        // this.saveGrave(grave.getEntity().getUniqueId(), inventory.getContents(), title);
        // ctx.event.getDrops().clear();
        // } catch (Exception e) {
        // /* fallback to vanilla drops */
        // e.printStackTrace();
        // }
        // }
        // });

        // SMEvent.register(ItemSpawnEvent.class, ctx -> {
        // if (ctx.event.getEventName().equalsIgnoreCase("ItemSpawnEvent")) {
        // CustomStack customStack = CustomStack.byItemStack(ctx.event.getEntity().getItemStack());
        // if (customStack != null) {
        // if (customStack.getNamespacedID().equalsIgnoreCase("stemcraft:grave")) {
        // for (Location location : spawnCancellations) {
        // if (ctx.event.getLocation().distanceSquared(location) < 2) {
        // ctx.event.setCancelled(true);
        // spawnCancellations.remove(location);
        // break;
        // }
        // }
        // }
        // }
        // }
        // });

        // SMEvent.register(InventoryCloseEvent.class, ctx -> {
        // Inventory inventory = ctx.event.getInventory();

        // if (this.trackedInventories.containsKey(inventory)) {
        // UUID id = this.trackedInventories.get(inventory);

        // this.trackedInventories.remove(inventory);
        // this.saveGrave(id, inventory.getContents(), ctx.event.getView().getTitle());
        // }
        // });
        // });

        // return true;
    }

    private Inventory loadGrave(UUID id) {
        try {
            PreparedStatement statement = SMDatabase.prepareStatement(
                "SELECT title, contents FROM graves WHERE id = ? LIMIT 1");
            statement.setString(1, id.toString());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String title = resultSet.getString("title");
                String contents = resultSet.getString("contents");
                ItemStack[] items = SMJson.fromJson(ItemStack[].class, contents);

                Inventory inventory = Bukkit.createInventory(null, items.length, title);
                inventory.setContents(items);

                return inventory;
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void saveGrave(UUID id, ItemStack[] items, String title) {
        try {
            PreparedStatement statement = SMDatabase.prepareStatement(
                "DELETE FROM graves WHERE id = ?");
            statement.setString(1, id.toString());
            statement.executeUpdate();

            statement = SMDatabase.prepareStatement(
                "INSERT INTO graves (id, title, contents) VALUES (?, ?, ?)");
            statement.setString(1, id.toString());
            statement.setString(2, title);
            statement.setString(3, SMJson.toJson(items, ItemStack[].class));
            statement.executeUpdate();

            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearGrave(UUID id) {
        try {
            PreparedStatement statement = SMDatabase.prepareStatement(
                "DELETE FROM graves WHERE id = ?");
            statement.setString(1, id.toString());
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
