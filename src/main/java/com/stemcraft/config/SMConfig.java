package com.stemcraft.config;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import com.stemcraft.STEMCraft;

public class SMConfig {
    private static SMConfig instance;

    public static final String CONFIG = "config.yml";
    public static final String LINE_SEPARATOR = "\n";

    private static final String DEFAULT_FILE_HEADER = "# STEMMechanics Config File";

    private static final TreeMap<String, String> DEFAULT_VALUES = new TreeMap<>();
    static {
        DEFAULT_VALUES.put("api-enabled", "false");
        DEFAULT_VALUES.put("api-port", "7775");
        DEFAULT_VALUES.put("api-key", "");
        DEFAULT_VALUES.put("mob-count", "990");
    }

    private static final TreeMap<String, String> VALUE_HEADERS = new TreeMap<>();
    static {
        VALUE_HEADERS.put("api-enabled", "Enable API");
        VALUE_HEADERS.put("api-port", "The port the API listens for connections");
        VALUE_HEADERS.put("api-key", "API Key");
        VALUE_HEADERS.put("mob-count", "Number of Mobs");
    }
    
    public static Boolean API_ENABLED;
    public static Integer API_PORT;
    public static String API_KEY;
    public static Integer MOB_COUNT;

    public SMConfig() {
        instance = this;
    }

    public static SMConfig getInstance() {
        return instance;
    }

    public static void loadValues() throws IOException {
        final STEMCraft plugin = STEMCraft.getInstance();
        final File configFolder = plugin.getDataFolder();
        if (!configFolder.exists()) {
            configFolder.mkdirs();
        }

        final File configFile = new File(configFolder, CONFIG);
        final TreeMap<String, String> fileOptions = new TreeMap<>();

        if(configFile.exists()) {
            // Read existing options from the file
            final List<String> lines = Files.readAllLines(configFile.toPath(), StandardCharsets.UTF_8);
            for (String line : lines) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#") && line.contains(":")) {
                    int colonIndex = line.indexOf(":");
                    String key = line.substring(0, colonIndex).trim();
                    String value = line.substring(colonIndex + 1).trim();

                    // Strip out single and double quotes from the start/end of the value
                    if (value.length() >= 2 && value.startsWith("'") && value.endsWith("'")) {
                        value = value.replaceAll("^'|'$", "");
                        value = value.replace("''", "'");
                        value = value.replace("\\'", "'");
                        value = value.replace("\\\\", "\\");
                    }
                    else if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.replaceAll("^\"|\"$", "");
                        value = value.replace("\\\"", "\"");
                        value = value.replace("\\\\", "\\");
                    }

                    fileOptions.put(key, value);
                }
            }
        }

        // Add missing options from DEFAULT_VALUES
        for (Map.Entry<String, String> entry : DEFAULT_VALUES.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (!fileOptions.containsKey(key)) {
                fileOptions.put(key, value);
            }
        }

        // Update the corresponding variables in SMConfig
        API_ENABLED = Boolean.parseBoolean(fileOptions.get("api-enabled"));
        API_PORT = Integer.parseInt(fileOptions.get("api-port"));
        API_KEY = fileOptions.get("api-key");
        MOB_COUNT = Integer.parseInt(fileOptions.get("mob-count"));

        addMissingOptions(configFile);
    }

    public static void addMissingOptions(final File file) throws IOException {
        final boolean writeHeader = !file.exists() || file.length() == 0;
        try (final FileOutputStream fout = new FileOutputStream(file, true)) {
            OutputStreamWriter out = new OutputStreamWriter(new BufferedOutputStream(fout), StandardCharsets.UTF_8);
            if (writeHeader) {
                out.append(DEFAULT_FILE_HEADER);
                out.append(LINE_SEPARATOR);
                out.append(LINE_SEPARATOR);
            }

            // Read existing options from the file
            final TreeMap<String, String> existingOptions = new TreeMap<>();
            final TreeMap<String, String> ignoreOptions = new TreeMap<>();
            if (file.exists()) {
                final List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
                for (String line : lines) {
                    line = line.trim();
                    if (!line.isEmpty() && line.contains(":")) {
                        int colonIndex = line.indexOf(":");
                        String key = line.substring(0, colonIndex).trim();
                        String value = line.substring(colonIndex + 1).trim();
                        
                        if (!key.startsWith("#")) {
                            existingOptions.put(key, value);
                        } else {
                            String ignoredKey = key.substring(1).trim();  // Remove "#" and trim the remaining text
                            ignoreOptions.put(ignoredKey, value);
                        }
                    }
                }
            }

            // Add missing options from DEFAULT_VALUES
            for (Map.Entry<String, String> entry : DEFAULT_VALUES.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (!existingOptions.containsKey(key) && !ignoreOptions.containsKey(key)) {
                    if (VALUE_HEADERS.containsKey(key)) {
                        String header = "# " + VALUE_HEADERS.get(key);
                        out.append(LINE_SEPARATOR);
                        out.append(header);
                        out.append(LINE_SEPARATOR);
                    }
                    
                    out.append(key).append(": \"").append(value).append("\"");
                    out.append(LINE_SEPARATOR);
                }
            }

            out.flush();
        }
    }
}
