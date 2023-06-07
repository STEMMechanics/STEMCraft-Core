import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.EulerAngle;
import stemcraft.objects.SMComponent;
import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class PlayerCorpses extends SMComponent {
    private Connection dbData;
    private Map<Player, ItemStack[]> playerInventories;

    @Override
    public Boolean onEnable() {
        playerInventories = new HashMap<>();

        try {
            Class.forName("org.sqlite.JDBC");
            dbData = DriverManager.getConnection("jdbc:sqlite:" + dataFolder + "/graveyard.db");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public void onDisable() {
        playerInventories.clear();
    }

    private void createTable() throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS inventories (" + "  player_uuid VARCHAR(36) PRIMARY KEY,"
                + "  inventory TEXT" + ")");
        statement.close();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        ItemStack[] inventoryContents = player.getInventory().getContents();
        playerInventories.put(player, inventoryContents);

        event.getDrops().clear(); // Cancel the normal drops

        Location deathLocation = player.getLocation();

        boolean isVoidOrLava = deathLocation.getBlock().getType() == Material.VOID_AIR
                || deathLocation.getBlock().getType() == Material.LAVA;
        if (isVoidOrLava) {
            // Player fell into the void or lava, cancel corpse creation and drops
            playerInventories.remove(player);
            return;
        }

        // Find a suitable block for the sign
        Block signBlock = findSignBlock(deathLocation);
        if (signBlock == null) {
            playerInventories.remove(player);
            return;
        }

        // Create the sign
        signBlock.setType(Material.OAK_WALL_SIGN);
        Sign sign = (Sign) signBlock.getState();
        String playerName = player.getName();
        sign.setLine(0, "RIP");
        if (playerName.length() <= 14) {
            sign.setLine(1, playerName);
        } else {
            sign.setLine(1, playerName.substring(0, 14));
            sign.setLine(2, playerName.substring(14));
        }
        sign.update();

        // Create the player corpse entity
        ArmorStand corpse = (ArmorStand) deathLocation.getWorld().spawnEntity(deathLocation, EntityType.ARMOR_STAND);
        corpse.setVisible(false);
        corpse.setSmall(true);
        corpse.setGravity(false);
        corpse.setArms(true);
        corpse.setBasePlate(false);
        corpse.setHeadPose(new EulerAngle(Math.toRadians(270), 0, 0));
        corpse.setRightArmPose(new EulerAngle(Math.toRadians(270), 0, 0));
        corpse.setItemInHand(new ItemStack(Material.PLAYER_HEAD));
        corpse.setCustomName(player.getName());
        corpse.setCustomNameVisible(true);

        // Store the armor stand data in the database
        storeArmorStandToDatabase(player, corpse, inventoryContents);
    }

    private Block findSignBlock(Location deathLocation) {
        int radius = 10;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Block block = deathLocation.clone().add(x, 0, z).getBlock();
                Block aboveBlock = block.getRelative(0, 1, 0);
                if (block.getType() == Material.AIR && aboveBlock.getType() == Material.AIR) {
                    return block;
                }
            }
        }

        return null; // No suitable sign block found
    }

    private void storeArmorStandToDatabase(Player player, ArmorStand armorStand, ItemStack[] inventory) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "REPLACE INTO inventories (player_uuid, armor_stand_location, armor_stand_rotation, inventory) VALUES (?, ?, ?, ?)");
            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, serializeLocation(armorStand.getLocation()));
            statement.setString(3, serializeRotation(armorStand.getHeadPose()));
            statement.setString(4, serializeInventory(inventory));
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Location deserializeLocation(String serializedLocation) {
        String[] parts = serializedLocation.split(",");
        String worldName = parts[0];
        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);
        float yaw = Float.parseFloat(parts[4]);
        float pitch = Float.parseFloat(parts[5]);
        return new Location(getServer().getWorld(worldName), x, y, z, yaw, pitch);
    }

    private EulerAngle deserializeRotation(String serializedRotation) {
        String[] parts = serializedRotation.split(",");
        double x = Double.parseDouble(parts[0]);
        double y = Double.parseDouble(parts[1]);
        double z = Double.parseDouble(parts[2]);
        return new EulerAngle(x, y, z);
    }

    private String serializeLocation(Location location) {
        return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ()
                + "," + location.getYaw() + "," + location.getPitch();
    }

    private String serializeRotation(EulerAngle rotation) {
        return rotation.getX() + "," + rotation.getY() + "," + rotation.getZ();
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player))
            return;

        Player clickedPlayer = (Player) event.getRightClicked();
        Player player = event.getPlayer();

        if (playerInventories.containsKey(clickedPlayer) && player.hasPermission("graveyard.loot")) {
            event.setCancelled(true);
            Inventory inventory = getServer().createInventory(null, 36, ChatColor.RED + "Graveyard Loot");

            ItemStack[] contents = playerInventories.get(clickedPlayer);
            inventory.setContents(contents);

            player.openInventory(inventory);

            if (inventory.isEmpty()) {
                // Remove the player corpse entity if the inventory is empty
                for (LivingEntity entity : clickedPlayer.getLocation().getWorld().getLivingEntities()) {
                    if (entity instanceof ArmorStand && entity.getCustomName() != null
                            && entity.getCustomName().equals(clickedPlayer.getName())) {
                        entity.remove();
                        break;
                    }
                }
                playerInventories.remove(clickedPlayer);

                // Delete the armor stand data from the database
                deleteArmorStandFromDatabase(clickedPlayer);
            }
        }
    }

    private void deleteArmorStandFromDatabase(Player player) {
        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM inventories WHERE player_uuid = ?");
            statement.setString(1, player.getUniqueId().toString());
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ArmorStand armorStand = loadArmorStandFromDatabase(player);
        if (armorStand != null) {
            ItemStack[] inventory = loadInventoryFromDatabase(player);
            playerInventories.put(player, inventory);
            armorStand.setCustomName(player.getName());
            armorStand.setCustomNameVisible(true);
        }
    }

    private ArmorStand loadArmorStandFromDatabase(Player player) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT armor_stand_location, armor_stand_rotation FROM inventories WHERE player_uuid = ?");
            statement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String serializedLocation = resultSet.getString("armor_stand_location");
                String serializedRotation = resultSet.getString("armor_stand_rotation");
                Location location = deserializeLocation(serializedLocation);
                EulerAngle rotation = deserializeRotation(serializedRotation);

                // Create the player corpse entity
                ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
                armorStand.setVisible(false);
                armorStand.setSmall(true);
                armorStand.setGravity(false);
                armorStand.setArms(true);
                armorStand.setBasePlate(false);
                armorStand.setHeadPose(rotation);
                armorStand.setRightArmPose(rotation);
                armorStand.setItemInHand(new ItemStack(Material.PLAYER_HEAD));

                return armorStand;
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ItemStack[] loadInventoryFromDatabase(Player player) {
        try {
            PreparedStatement statement =
                    connection.prepareStatement("SELECT inventory FROM inventories WHERE player_uuid = ?");
            statement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String serializedInventory = resultSet.getString("inventory");
                return deserializeInventory(serializedInventory);
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String serializeInventory(ItemStack[] inventory) {
        StringBuilder builder = new StringBuilder();
        for (ItemStack item : inventory) {
            if (item != null) {
                builder.append(item.serialize()).append(",");
            } else {
                builder.append("null,");
            }
        }
        return builder.toString();
    }

    private ItemStack[] deserializeInventory(String serializedInventory) {
        String[] itemStrings = serializedInventory.split(",");
        ItemStack[] inventory = new ItemStack[itemStrings.length];
        for (int i = 0; i < itemStrings.length; i++) {
            String itemString = itemStrings[i];
            if (!itemString.equals("null")) {
                inventory[i] = ItemStack.deserialize(JsonParser.parseString(itemString).getAsJsonObject());
            }
        }
        return inventory;
    }
}
