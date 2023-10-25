package com.stemcraft.core.adapters;

import com.google.gson.*;
import com.stemcraft.core.SMJsonAdapter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import java.lang.reflect.Type;

public class SMAdapterWorld implements SMJsonAdapter, JsonSerializer<World>, JsonDeserializer<World> {
    @Override
    public Class<?> adapterFor() {
        return World.class;
    }
    
    @Override
    public World deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String worldName = json.getAsString();
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            throw new JsonParseException("World with name " + worldName + " not found");
        }

        return world;
    }

    @Override
    public JsonElement serialize(World src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.getName());
    }
}