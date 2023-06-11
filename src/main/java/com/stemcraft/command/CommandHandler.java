package com.stemcraft.command;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import com.stemcraft.STEMCraft;

public final class CommandHandler {
    public CommandHandler(STEMCraft plugin) {
        List<Class<?>> classCommandList = STEMCraft.getClassList("com/stemcraft/command/", false);

        for (Class<?> classCommandItem : classCommandList) {
            if (classCommandItem.getSimpleName().equals("CommandHandler") || classCommandItem.getSimpleName().equals("CommandItem")) {
                continue; // Skip this class
            }

            try {
                Constructor<?> constructor = classCommandItem.getDeclaredConstructor(STEMCraft.class);
                CommandItem commandInstance = (CommandItem) constructor.newInstance(plugin);
                ArrayList<String> commandList = commandInstance.commandList();
                
                for(String command : commandList) {
                    plugin.getCommand(command).setExecutor(commandInstance);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
