package com.stemcraft.core;

import java.util.HashMap;
import java.util.Map;

public class SMReplacer {
    /**
     * Replace variables in the specified string.
     * @param message
     * @param replacements
     * @return
     */
    public static String replaceVariables(String message, String... replacements) {
        if (replacements.length % 2 != 0) {
            throw new IllegalArgumentException("Odd number of replacements provided. Expecting key-value pairs.");
        }
    
        Map<String, String> replacementMap = new HashMap<>();
        for (int i = 0; i < replacements.length; i += 2) {
            String key = replacements[i].toString();
            String value = (replacements[i + 1] != null) ? replacements[i + 1].toString() : null;
            replacementMap.put(key, value);
        }

        return replaceVariables(message, replacementMap);
    }

    /**
     * Replace variables in the specified string.
     * @param message
     * @param replacements
     * @return
     */
    public static String replaceVariables(String message, Map<String, String> replacements) {
        StringBuilder result = new StringBuilder(message);
        replacements.forEach((key, val) -> {
            if (val != null) {
                String pattern = "{" + key + "}";
                int index;
                while ((index = result.indexOf(pattern)) != -1) {
                    result.replace(index, index + pattern.length(), val);
                }
            }
        });
    
        return result.toString();
    }
}
