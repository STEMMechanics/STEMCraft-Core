package com.stemcraft.core;

import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.event.SMEvent;

public class SMDependency extends SMFeature {
    private static HashMap<String, String> dependencies = new HashMap<>();
    private static HashMap<String, Boolean> dependLoaded = new HashMap<>();
    private static HashMap<String, Boolean> dependReady = new HashMap<>();
    private static HashMap<String, ArrayList<Runnable>> readyCallbacks = new HashMap<>();

    @Override
    public Boolean onLoad() {
        super.onLoad();

        dependencies.put("Citizens", "net.citizensnpcs.api.event.CitizensEnableEvent");
        dependencies.put("ItemsAdder", "dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent");
        dependencies.put("LuckPerms", null);

        dependencies.forEach((name, clazz) -> {
            if(Bukkit.getPluginManager().getPlugin(name) != null) {
                SMDependency.dependLoaded.put(name, true);
            } else {
                SMDependency.dependLoaded.put(name, false);
                STEMCraft.warning(name + " is not loaded. Features requiring this plugin won't be available");
            }
        });

        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Boolean onEnable() {
        dependencies.forEach((name, clazz) -> {
            if(SMDependency.dependLoaded.get(name)) {
                if(clazz != null) {
                    try {
                        Class<? extends Event> eventClass = (Class<? extends Event>)Class.forName(clazz);
                        SMEvent.register(eventClass, ctx -> {
                            SMDependency.setDependencyReady(name);
                        });
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    SMDependency.setDependencyReady(name);
                }
            }
        });

        return true;
    }

    public static Boolean dependencyLoaded(String name) {
        if(dependLoaded.containsKey(name)) {
            return dependLoaded.get(name);
        }

        return false;
    }

    public static Boolean dependencyReady(String name) {
        if(dependReady.containsKey(name)) {
            return dependReady.get(name);
        }

        return false;
    }

    public static void onDependencyReady(String name, Runnable callback) {
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

    private static void setDependencyReady(String name) {
        SMDependency.setDependencyReady(name, true);
    }

    private static void setDependencyReady(String name, Boolean ready) {
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
}
