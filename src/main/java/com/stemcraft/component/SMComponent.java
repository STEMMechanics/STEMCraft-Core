package com.stemcraft.component;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import com.stemcraft.STEMCraft;

public class SMComponent {
    private static final List<SMComponent> loadedComponents = new ArrayList<>();

    public static void loadComponents() {
        STEMCraft plugin = STEMCraft.getInstance();
        List<Class<?>> classComponentList = STEMCraft.getClassList("com/stemcraft/component/", false);

        for (Class<?> classComponentItem : classComponentList) {
            if (classComponentItem.getSimpleName().equals("SMComponent")) {
                continue; // Skip this class
            }

            try {
                Constructor<?> constructor = classComponentItem.getDeclaredConstructor();
                SMComponent componentInstance = (SMComponent) constructor.newInstance();

                componentInstance.load();
                loadedComponents.add(componentInstance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void unloadComponents() {
        for (SMComponent component : loadedComponents) {
            component.unload();
        }
        loadedComponents.clear();
    }

    public void load() {}
    public void unload() {}
}
