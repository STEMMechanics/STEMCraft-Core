package com.stemcraft.core.adapters;

import com.google.gson.*;
import com.stemcraft.core.SMJsonAdapter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;

public class SMAdapterItemStackArray implements SMJsonAdapter, JsonSerializer<ItemStack[]>, JsonDeserializer<ItemStack[]> {
    @Override
    public Class<?> adapterFor() {
        return ItemStack[].class;
    }

    @Override
    public ItemStack[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonMap = json.getAsJsonObject();
        JsonArray jsonArray = jsonMap.getAsJsonArray("contents");
        int size = jsonMap.get("size").getAsInt();

        ItemStack[] itemStackArray = new ItemStack[size];

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject itemJsonObject = jsonArray.get(i).getAsJsonObject();
            int slot = itemJsonObject.get("slot").getAsInt();
            JsonElement itemJsonElement = itemJsonObject.get("item");
            ItemStack itemStack = context.deserialize(itemJsonElement, ItemStack.class);
            itemStackArray[slot] = itemStack;
        }
    
        // Fill in any null slots with Material.AIR
        for (int i = 0; i < itemStackArray.length; i++) {
            if (itemStackArray[i] == null) {
                itemStackArray[i] = new ItemStack(Material.AIR);
            }
        }

        return itemStackArray;
    }

    @Override
    public JsonElement serialize(ItemStack[] src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonMap = new JsonObject();
        JsonArray jsonArray = new JsonArray();

        for (int i = 0; i < src.length; i++) {
            ItemStack itemStack = src[i];
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                JsonObject itemJsonObject = new JsonObject();
                itemJsonObject.addProperty("slot", i);
                JsonElement itemJsonElement = context.serialize(itemStack, ItemStack.class);
                itemJsonObject.add("item", itemJsonElement);
                jsonArray.add(itemJsonObject);
            }
        }

        jsonMap.addProperty("size", src.length);
        jsonMap.add("contents", jsonArray);

        return jsonMap;
    }
}