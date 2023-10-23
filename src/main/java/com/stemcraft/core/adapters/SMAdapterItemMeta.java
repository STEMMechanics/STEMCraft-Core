package com.stemcraft.core.adapters;

import com.google.gson.*;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMJsonAdapter;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.meta.ItemMeta;
import java.lang.reflect.Type;
import java.util.Map;

public class SMAdapterItemMeta implements SMJsonAdapter, JsonSerializer<ItemMeta>, JsonDeserializer<ItemMeta> {
    @Override
    public Class<?> adapterFor() {
        return ItemMeta.class;
    }
    
    @Override
    public ItemMeta deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Map<String, Object> deserialize = context.deserialize(json, Map.class);
        
        if(deserialize.containsKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY) && deserialize.get(ConfigurationSerialization.SERIALIZED_TYPE_KEY) == "ItemMeta") {
            STEMCraft.info("ITEM METAAAAA");
        }

        // STEMCraft.info(deserialize.toString());

        return (ItemMeta) ConfigurationSerialization.deserializeObject(deserialize);
    }

    @Override
    public JsonElement serialize(ItemMeta src, Type typeOfSrc, JsonSerializationContext context) {
        Map<String, Object> serialize = src.serialize();
        return context.serialize(serialize, Map.class);
    }
}