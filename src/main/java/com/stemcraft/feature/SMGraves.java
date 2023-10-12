package com.stemcraft.feature;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
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
import com.stemcraft.core.SMDatabase;
import com.stemcraft.core.SMDependency;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.event.SMEvent;
import com.stemcraft.core.serialize.SMSerialize;
import dev.lone.itemsadder.api.CustomFurniture;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.Events.FurnitureBreakEvent;
import dev.lone.itemsadder.api.Events.FurnitureInteractEvent;

public class SMGraves extends SMFeature {
    private HashMap<Inventory, UUID> trackedInventories = new HashMap<>();

    @Override
    protected Boolean onEnable() {
        SMDatabase.runMigration("230804162200_CreateGravestoneTable", () -> {
            SMDatabase.prepareStatement(
            "CREATE TABLE IF NOT EXISTS graves (" +
                "id TEXT PRIMARY KEY," +
                "data TEXT)").executeUpdate();
        });

        SMDependency.onDependencyReady("itemsadder", () -> {
            SMEvent.register(FurnitureInteractEvent.class, ctx -> {
                if(ctx.event.getNamespacedID().equalsIgnoreCase("stemcraft:grave")) {
                    UUID id = ctx.event.getFurniture().getEntity().getUniqueId();
                    
                    Inventory inventory = loadGrave(id);
                    if(inventory == null) {
                        String title = "Unknown Grave";
                        inventory = Bukkit.createInventory(null, 54, title);
                        this.saveGrave(id, inventory, title);
                    }

                    if(inventory != null) {
                        ctx.event.getPlayer().openInventory(inventory);
                        this.trackedInventories.put(inventory, id);
                    }
                }
            });

            SMEvent.register(FurnitureBreakEvent.class, ctx -> {
                if(ctx.event.getNamespacedID().equalsIgnoreCase("stemcraft:grave")) {
                    Entity grave = ctx.event.getFurniture().getEntity();
                    UUID id = grave.getUniqueId();

                    ItemStack wall = new ItemStack(Material.MOSSY_STONE_BRICK_WALL);
                    ItemStack slab = new ItemStack(Material.MOSSY_STONE_BRICK_SLAB);
                    grave.getWorld().dropItemNaturally(grave.getLocation(), wall);
                    grave.getWorld().dropItemNaturally(grave.getLocation(), slab);

                    this.clearGrave(id);
                }
            });

            SMEvent.register(PlayerDeathEvent.class, ctx -> {
                if(ctx.event.getEventName().equalsIgnoreCase("playerdeathevent")) {
                    Player player = ctx.event.getEntity();

                    String title = player.getName() + (player.getName().endsWith("s") ? "'" : "'s") + " Grave";
                    Inventory inventory = Bukkit.createInventory(null, 54, title);
                    ctx.event.getDrops().forEach((itemStack) -> {
                        inventory.addItem(itemStack.clone());
                    });

                    ctx.event.getDrops().clear();

                    CustomFurniture grave = CustomFurniture.spawn("stemcraft:grave", player.getLocation().getBlock());
                    this.saveGrave(grave.getEntity().getUniqueId(), inventory, title);
                }
            });

            SMEvent.register(ItemSpawnEvent.class, ctx -> {
                if(ctx.event.getEventName().equalsIgnoreCase("ItemSpawnEvent")) {
                    CustomStack customStack = CustomStack.byItemStack(ctx.event.getEntity().getItemStack());
                    if(customStack != null) {
                        if(customStack.getNamespacedID().equalsIgnoreCase("stemcraft:grave")) {
                            ctx.event.setCancelled(true);
                        }
                    }
                }
            });

            SMEvent.register(InventoryCloseEvent.class, ctx -> {
                Inventory inventory = ctx.event.getInventory();
                
                if(this.trackedInventories.containsKey(inventory)) {
                    UUID id = this.trackedInventories.get(inventory);

                    this.trackedInventories.remove(inventory);
                    this.saveGrave(id, inventory, ctx.event.getView().getTitle());
                }
            });
        });

        return true;
    }

    private Inventory loadGrave(UUID id) {
        try {
            PreparedStatement statement = SMDatabase.prepareStatement(
                "SELECT data FROM graves WHERE id = ? LIMIT 1");
            statement.setString(1, id.toString());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String value = resultSet.getString("data");
                Inventory inventory = SMSerialize.deserialize(Inventory.class, value);

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
            Map<String, Object> inventoryMap = new HashMap<>();
            inventoryMap.put("size", inventory.getSize());
            inventoryMap.put("title", title);
            inventoryMap.put("inventory", inventory.getContents());
            
            PreparedStatement statement = SMDatabase.prepareStatement(
                    "DELETE FROM graves WHERE id = ?");
            statement.setString(1, id.toString());
            statement.executeUpdate();

            statement = SMDatabase.prepareStatement(
                    "INSERT INTO graves (id, data) VALUES (?, ?)");
            statement.setString(1, id.toString());
            statement.setString(2, SMSerialize.serialize(inventoryMap));
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
