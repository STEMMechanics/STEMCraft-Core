package com.stemcraft.core.adapters;

import com.google.gson.*;
import com.stemcraft.core.SMJsonAdapter;
import org.bukkit.Color;
import java.lang.reflect.Type;
import java.util.Map;

public class SMAdapterColor implements SMJsonAdapter, JsonSerializer<Color>, JsonDeserializer<Color> {
    @Override
    public Class<?> adapterFor() {
        return Color.class;
    }
    
    @Override
    public Color deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Map<String, Object> deserialize = context.deserialize(json, Map.class);
        
        return (Color) Color.deserialize(deserialize);
    }

    @Override
    public JsonElement serialize(Color src, Type typeOfSrc, JsonSerializationContext context) {
        Map<String, Object> serialize = src.serialize();
        return context.serialize(serialize, Map.class);
    }
}