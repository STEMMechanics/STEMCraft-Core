package com.stemcraft.core.adapters;

import com.google.gson.*;
import com.stemcraft.core.SMJsonAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.lang.reflect.Type;

public class SMAdapterLocation implements SMJsonAdapter, JsonSerializer<Location>, JsonDeserializer<Location> {
    @Override
    public Class<?> adapterFor() {
        return Location.class;
    }
    
    @Override
    public Location deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        
        String worldName = jsonObject.get("world").getAsString();
        double x = jsonObject.get("x").getAsDouble();
        double y = jsonObject.get("y").getAsDouble();
        double z = jsonObject.get("z").getAsDouble();
        float yaw = 0.0f;
        float pitch = 0.0f;
    
        // Check if yaw and pitch properties are present, and if so, read their values
        if (jsonObject.has("yaw")) {
            yaw = jsonObject.get("yaw").getAsFloat();
        }
        if (jsonObject.has("pitch")) {
            pitch = jsonObject.get("pitch").getAsFloat();
        }

        return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }

    @Override
    public JsonElement serialize(Location src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        
        jsonObject.addProperty("world", src.getWorld().getName());
        jsonObject.addProperty("x", src.getX());
        jsonObject.addProperty("y", src.getY());
        jsonObject.addProperty("z", src.getZ());
        jsonObject.addProperty("pitch", src.getPitch());
        jsonObject.addProperty("yaw", src.getYaw());

        return jsonObject;
    }
}