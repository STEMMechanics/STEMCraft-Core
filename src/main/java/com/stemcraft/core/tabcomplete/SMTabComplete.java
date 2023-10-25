package com.stemcraft.core.tabcomplete;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class SMTabComplete {
    private static HashMap<String, Supplier<List<String>>> tabCompletionPlaceholders = new HashMap<>();

    public static void register(String name, Supplier<List<String>> callback) {
        tabCompletionPlaceholders.put(name, callback);
    }

    public static List<String> get(String name) {
        if(tabCompletionPlaceholders.containsKey(name)) {
            return tabCompletionPlaceholders.get(name).get();
        }

        return new ArrayList<String>();
    }
}
