package com.stemcraft.component;

import java.lang.reflect.Constructor;
import java.util.List;
import com.stemcraft.STEMCraft;

public class SMComponent {
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
