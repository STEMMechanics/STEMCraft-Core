package com.stemcraft.manager;

import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import com.stemcraft.STEMCraft;

public class SMDependManager extends SMManager {
    private HashMap<String, String> dependencies = new HashMap<>();
    private HashMap<String, Boolean> dependLoaded = new HashMap<>();
    private HashMap<String, Boolean> dependReady = new HashMap<>();
    private HashMap<String, ArrayList<Runnable>> readyCallbacks = new HashMap<>();

    @Override
    public void onLoad(STEMCraft plugin) {
        super.onLoad(plugin);

        dependencies.put("Citizens", "net.citizensnpcs.api.event.CitizensEnableEvent");
        dependencies.put("ItemsAdder", "dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent");
        dependencies.put("LuckPerms", null);

        dependencies.forEach((name, clazz) -> {
            if(Bukkit.getPluginManager().getPlugin(name) != null) {
                this.dependLoaded.put(name, true);
            } else {
                this.dependLoaded.put(name, false);
                this.plugin.getLogger().warning(name + " is not loaded. Features requiring this plugin won't be available");
            }
        });
    }


    @Override
    public void onEnable() {
        dependencies.forEach((name, clazz) -> {
            if(this.dependLoaded.get(name)) {
                if(clazz != null) {
                    try {
                        Class<? extends Event> eventClass = (Class<? extends Event>)Class.forName(clazz);
                        this.plugin.getEventManager().registerEvent(eventClass, (listener, rawEvent) -> {
                            this.setDependencyReady(name);
                        });
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    this.setDependencyReady(name);
                }
            }
        });
    }

    public Boolean getDependencyLoaded(String name) {
        if(dependLoaded.containsKey(name)) {
            return dependLoaded.get(name);
        }

        return false;
    }

    public Boolean getDependencyReady(String name) {
        if(dependReady.containsKey(name)) {
            return dependReady.get(name);
        }

        return false;
    }

    public void setDependencyReady(String name) {
        this.setDependencyReady(name, true);
    }

    public void setDependencyReady(String name, Boolean ready) {
        dependReady.put(name, ready);

        // Check if there are any stored callbacks for this dependency
        if (ready && readyCallbacks.containsKey(name)) {
            // If the dependency is ready, execute all the stored callbacks
            ArrayList<Runnable> callbacks = readyCallbacks.get(name);
            for (Runnable callback : callbacks) {
                callback.run();
            }
            // Clear the callbacks since they have been called
            readyCallbacks.remove(name);
        }
    }

    public void onDependencyReady(String name, Runnable callback) {
        // If the dependency is already ready, execute the callback immediately
        if (dependReady.getOrDefault(name, false)) {
            callback.run();
        } else {
            // Otherwise, store the callback for future execution
            if (readyCallbacks.containsKey(name)) {
                readyCallbacks.get(name).add(callback);
            } else {
                ArrayList<Runnable> callbacks = new ArrayList<>();
                callbacks.add(callback);
                readyCallbacks.put(name, callbacks);
            }
        }
    }
}
