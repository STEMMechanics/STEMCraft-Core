package com.stemcraft.core.adapters;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.stemcraft.core.SMJson;
import com.stemcraft.core.SMJsonAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

public class SMAdapterItemStack implements SMJsonAdapter, JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {
    @Override
    public Class<?> adapterFor() {
        return ItemStack.class;
    }
    
    @Override
    @SuppressWarnings({"deprecation", "unchecked"})
    public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Map<String, Object> map = SMJson.getGson().fromJson(json, TypeToken.get(Map.class).getType());
        map.putIfAbsent("v", Bukkit.getUnsafe().getDataVersion());

        if(map.containsKey("meta")) {
            Map<String, Object> meta = (Map<String, Object>) map.get("meta");
            map.remove("meta");
            ItemStack is = ItemStack.deserialize(map);

            if(is.getType() == Material.PLAYER_HEAD) {
                Map<String, Object> skullOwner = (Map<String, Object>)meta.get("skull-owner");
                Map<String, Object> profile = (Map<String, Object>)skullOwner.get("profile");

                SkullMeta skullMeta = (SkullMeta)is.getItemMeta();
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(profile.get("id").toString())));
                is.setItemMeta(skullMeta);

            } else {
                ItemMeta deserializedMeta = context.deserialize(SMJson.getGson().toJsonTree(meta), ItemMeta.class);
                is.setItemMeta((ItemMeta) deserializedMeta);
            }

            return is;
        } else {
            return ItemStack.deserialize(map);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context) {
        Map<String, Object> map = src.serialize();
        map.putIfAbsent("v", Bukkit.getUnsafe().getDataVersion());
        if(src.hasItemMeta()) {
            JsonElement meta = context.serialize(src.getItemMeta(), ConfigurationSerializable.class);
            map.put("meta", meta.getAsJsonObject());
        }
        return SMJson.getGson().toJsonTree(map);
    }
}