package com.stemcraft.feature;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import com.stemcraft.SMSerialize;
import dev.lone.itemsadder.api.CustomFurniture;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.Events.FurnitureBreakEvent;
import dev.lone.itemsadder.api.Events.FurnitureInteractEvent;

public class SMGraves extends SMFeature {
    private HashMap<Inventory, UUID> trackedInventories = new HashMap<>();

    @Override
    protected Boolean onEnable() {
        this.plugin.getDatabaseManager().addMigration("230804162200_CreateGravestoneTable", (databaseManager) -> {
            databaseManager.prepareStatement(
            "CREATE TABLE IF NOT EXISTS graves (" +
                "id TEXT PRIMARY KEY," +
                "data TEXT)").executeUpdate();
        });

        this.plugin.getDependManager().onDependencyReady("itemsadder", () -> {
            this.plugin.getEventManager().registerEvent(FurnitureInteractEvent.class, (listener, rawEvent) -> {
                FurnitureInteractEvent event = (FurnitureInteractEvent)rawEvent;
                if(event.getNamespacedID().equalsIgnoreCase("stemcraft:grave")) {
                    UUID id = event.getFurniture().getEntity().getUniqueId();
                    
                    Inventory inventory = loadGrave(id);
                    if(inventory == null) {
                        String title = "Unknown Grave";
                        inventory = Bukkit.createInventory(null, 54, title);
                        this.saveGrave(id, inventory, title);
                    }

                    if(inventory != null) {
                        event.getPlayer().openInventory(inventory);
                        this.trackedInventories.put(inventory, id);
                    }
                }
            });

            this.plugin.getEventManager().registerEvent(FurnitureBreakEvent.class, (listener, rawEvent) -> {
                FurnitureBreakEvent event = (FurnitureBreakEvent)rawEvent;
                if(event.getNamespacedID().equalsIgnoreCase("stemcraft:grave")) {
                    Entity grave = event.getFurniture().getEntity();
                    UUID id = grave.getUniqueId();

                    ItemStack wall = new ItemStack(Material.MOSSY_STONE_BRICK_WALL);
                    ItemStack slab = new ItemStack(Material.MOSSY_STONE_BRICK_SLAB);
                    grave.getWorld().dropItemNaturally(grave.getLocation(), wall);
                    grave.getWorld().dropItemNaturally(grave.getLocation(), slab);

                    this.clearGrave(id);
                }
            });

            this.plugin.getEventManager().registerEvent(PlayerDeathEvent.class, (listener, rawEvent) -> {
                if(rawEvent.getEventName().equalsIgnoreCase("playerdeathevent")) {
                    PlayerDeathEvent event = (PlayerDeathEvent)rawEvent;
                    Player player = event.getEntity();

                    String title = player.getName() + (player.getName().endsWith("s") ? "'" : "'s") + " Grave";
                    Inventory inventory = Bukkit.createInventory(null, 54, title);
                    event.getDrops().forEach((itemStack) -> {
                        inventory.addItem(itemStack.clone());
                    });

                    event.getDrops().clear();

                    CustomFurniture grave = CustomFurniture.spawn("stemcraft:grave", player.getLocation().getBlock());
                    this.saveGrave(grave.getEntity().getUniqueId(), inventory, title);
                }
            });

            this.plugin.getEventManager().registerEvent(ItemSpawnEvent.class, (listener, rawEvent) -> {
                if(rawEvent.getEventName().equalsIgnoreCase("ItemSpawnEvent")) {
                    ItemSpawnEvent event = (ItemSpawnEvent)rawEvent;
                                        
                    CustomStack customStack = CustomStack.byItemStack(event.getEntity().getItemStack());
                    if(customStack != null) {
                        if(customStack.getNamespacedID().equalsIgnoreCase("stemcraft:grave")) {
                            event.setCancelled(true);
                        }
                    }
                }
            });

            this.plugin.getEventManager().registerEvent(InventoryCloseEvent.class, (listener, rawEvent) -> {
                InventoryCloseEvent event = (InventoryCloseEvent)rawEvent;
                Inventory inventory = event.getInventory();
                
                if(this.trackedInventories.containsKey(inventory)) {
                    UUID id = this.trackedInventories.get(inventory);

                    this.trackedInventories.remove(inventory);
                    this.saveGrave(id, inventory, event.getView().getTitle());
                }
            });
        });

        return true;
    }

    private Inventory loadGrave(UUID id) {
        try {
            PreparedStatement statement = this.plugin.getDatabaseManager().prepareStatement(
                "SELECT data FROM graves WHERE id = ? LIMIT 1");
            statement.setString(1, id.toString());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String value = resultSet.getString("data");
                Inventory inventory = SMSerialize.deserializeInventory(value);

                return inventory;
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void saveGrave(UUID id, Inventory inventory, String title) {
        try {
            PreparedStatement statement = this.plugin.getDatabaseManager().prepareStatement(
                    "DELETE FROM graves WHERE id = ?");
            statement.setString(1, id.toString());
            statement.executeUpdate();

            statement = this.plugin.getDatabaseManager().prepareStatement(
                    "INSERT INTO graves (id, data) VALUES (?, ?)");
            statement.setString(1, id.toString());
            statement.setString(2, SMSerialize.serialize(inventory, title));
            statement.executeUpdate();

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void clearGrave(UUID id) {
        try {
            PreparedStatement statement = this.plugin.getDatabaseManager().prepareStatement(
                    "DELETE FROM graves WHERE id = ?");
            statement.setString(1, id.toString());
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
