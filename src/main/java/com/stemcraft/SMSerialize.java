package com.stemcraft;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import com.stemcraft.json.JSONObject;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;

public class SMSerialize {
    public static String serialize(Map<String,Object> map) {
        return new JSONObject(map).toString();
    }

    public static String serialize(ItemStack items) {
        ReadWriteNBT nbt = NBT.itemStackToNBT(items);
        return nbt.toString();
    }

    public static String serialize(ItemStack[] items) {
        ReadWriteNBT nbt = NBT.itemStackArrayToNBT(items);
        return nbt.toString();
    }

    public static String serialize(Location location) {
        Map<String, Object> map = new HashMap<>();

        map.put("world", location.getWorld().getName());
        map.put("x", location.getX());
        map.put("y", location.getY());
        map.put("z", location.getZ());
        map.put("yaw", location.getYaw());
        map.put("pitch", location.getPitch());

        return serialize(map);
    }

    public static String serialize(Inventory inventory, String title) {
        Map<String, Object> map = new HashMap<>();

        map.put("size", inventory.getSize());
        map.put("title", title);
        map.put("contents", serialize(inventory.getContents()));

        return serialize(map);
    }

    public static Map<String,Object> deserialize(String str) {
        try {
            return new JSONObject(str).toMap();
        } catch(Exception e) {
            e.printStackTrace();
            return new HashMap<String,Object>();
        }
    }

    public static ItemStack deserializeItemStack(String str) {
        try {
            ReadWriteNBT nbt = NBT.parseNBT(str);
            return NBT.itemStackFromNBT(nbt);
        } catch(Exception e) {
            e.printStackTrace();
            return new ItemStack(Material.AIR);
        }
    }

    public static ItemStack[] deserializeItemStackArray(String str) {
        try {
            ReadWriteNBT nbt = NBT.parseNBT(str);
            return NBT.itemStackArrayFromNBT(nbt);
        } catch(Exception e) {
            e.printStackTrace();
            return new ItemStack[0];
        }
    }

    public static Inventory deserializeInventory(String str) {
        try {
            Map<String, Object> map = deserialize(str);
            ItemStack[] contents = deserializeItemStackArray((String)map.get("contents"));
            Inventory inventory = Bukkit.createInventory(null, (Integer)map.get("size"), (String)map.get("title"));

            inventory.setContents(contents);
            return inventory;
        } catch(Exception e) {
            e.printStackTrace();
            return Bukkit.createInventory(null, 9, "");
        }
    }

    public static Location deserializeLocation(String str) {
        try {
            Map<String, Object> map = deserialize(str);
        
            World world = Bukkit.getWorld((String)map.get("world"));
            Location location = new Location(world, (Double)map.get("x"), (Double)map.get("y"), (Double)map.get("z"), (Float)map.get("yaw"), (Float)map.get("pitch"));
            
            return location;
        } catch(Exception e) {
            e.printStackTrace();
            return Bukkit.getWorlds().get(0).getSpawnLocation();
        }
    }
}
