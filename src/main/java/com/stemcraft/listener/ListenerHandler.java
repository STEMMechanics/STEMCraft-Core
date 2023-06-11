package com.stemcraft.listener;

import java.lang.reflect.Constructor;
import java.util.List;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import com.stemcraft.STEMCraft;
import com.stemcraft.listener.entity.EntityDeathListener;

public final class ListenerHandler {
    public ListenerHandler(STEMCraft plugin) {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        List<Class<?>> listenerList = STEMCraft.getClassList("com/stemcraft/listener/", true);

        for (Class<?> listenerItem : listenerList) {
            try {
                Constructor<?> constructor = listenerItem.getDeclaredConstructor();
                pluginManager.registerEvents((Listener) constructor.newInstance(), plugin);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        pluginManager.registerEvents(new EntityDeathListener(), plugin);
    }
}
