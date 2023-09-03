package com.stemcraft;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class SMConfig {
    private STEMCraft plugin;
    private String fileName = "";
    private String lineSeparator = "\n";
    private String fileHeader = "";
    private Boolean loaded = false;
    private UUID dirtyNonce = null;
    private LinkedHashMap<String, String> values = new LinkedHashMap<>();
    private String lastRegisteredValue = "";
    private HashMap<String, String> headers = new HashMap<>();

    public SMConfig(STEMCraft plugin) {
        this(plugin, "config");
    }

    public SMConfig(STEMCraft plugin, String name) {
        this.plugin = plugin;
        this.fileName = name + ".yml";
    }

    public void setLineSeparator(String seperator) {
        this.lineSeparator = seperator;
    }

    public void setHeader(String header) {
        this.fileHeader = "# " + header;
    }

    public Boolean isLoaded() {
        return this.loaded;
    }

    public Boolean getBoolValue(String name) {
        Boolean defaultValue = false;
        return Boolean.parseBoolean(this.getValue(name, defaultValue.toString()));
    }

    public Boolean getBoolValue(String name, Boolean defaultValue) {
        return Boolean.parseBoolean(this.getValue(name, defaultValue.toString()));
    }

    public Integer getIntValue(String name) {
        Integer defaultValue = 0;
        return Integer.parseInt(this.getValue(name, defaultValue.toString()));
    }

    public Integer getIntValue(String name, Integer defaultValue) {
        return Integer.parseInt(this.getValue(name, defaultValue.toString()));
    }

    public String getValue(String name) {
        String defaultValue = "";
        return getValue(name, defaultValue);
    }

    public String getValue(String name, String defaultValue) {
        if(this.values.containsKey(name)) {
            return this.values.get(name);
        }

        this.values.put(name, defaultValue);
        return defaultValue;
    }

    public Boolean registerValue(String name, Boolean defaultValue) {
        return Boolean.parseBoolean(this.registerValue(name, defaultValue.toString(), ""));
    }

    public Integer registerValue(String name, Integer defaultValue) {
        return Integer.parseInt(this.registerValue(name, defaultValue.toString(), ""));
    }

    public String registerValue(String name, String defaultValue) {
        return registerValue(name, defaultValue, "");
    }

    public Boolean registerValue(String name, Boolean defaultValue, String header) {
        return Boolean.parseBoolean(this.registerValue(name, defaultValue.toString(), header));
    }

    public Integer registerValue(String name, Integer defaultValue, String header) {
        return Integer.parseInt(this.registerValue(name, defaultValue.toString(), header));
    }

    public String registerValue(String name, String defaultValue, String header) {
        if(header.length() > 0) {
            this.headers.put(name, header);
        }

        if(!this.values.containsKey(name)) {
            SMUtil.insertAfter(this.values, this.lastRegisteredValue, name, defaultValue);
            this.dirty();
        }
        
        this.lastRegisteredValue = name;
        return this.values.get(name);
    }

    public void loadConfig() {
        if(this.dirtyNonce != null) {
            this.plugin.cancelDelayedTask(this.dirtyNonce);
            this.dirtyNonce = null;
        }

        File configFolder = this.plugin.getDataFolder();
        if (!configFolder.exists()) {
            configFolder.mkdirs();
        }

        File configFile = new File(configFolder, this.fileName);
        TreeMap<String, String> fileOptions = new TreeMap<>();

        if(configFile.exists()) {
            try {
                List<String> lines = Files.readAllLines(configFile.toPath(), StandardCharsets.UTF_8);
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
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        fileOptions.forEach((key, value) -> {
            this.values.put(key, value);
        });
    }

    private void dirty() {
        if(this.dirtyNonce == null) {
            this.dirtyNonce = UUID.randomUUID();
        }

        long delay = 10;    // 10 ticks
        this.plugin.delayedTask(delay, this.dirtyNonce, (data) -> {
            dirtyNonce = null;
            updateConfig();
        }, null);
    }

    private void updateConfig() {
        File configFolder = this.plugin.getDataFolder();
        if (!configFolder.exists()) {
            configFolder.mkdirs();
        }

        File configFile = new File(configFolder, this.fileName);
        List<String> updatedFile = new ArrayList<>();
        List<String> foundKeys = new ArrayList<>();

        if(this.fileHeader.length() > 0 && (!configFile.exists() || configFile.length() == 0)) {
            updatedFile.add(this.fileHeader);
        }

        if(configFile.exists()) {
            try {
                // find existing keys
                List<String> lines = Files.readAllLines(configFile.toPath(), StandardCharsets.UTF_8);
                for (String line : lines) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#") && line.contains(":")) {
                        int colonIndex = line.indexOf(":");
                        String key = line.substring(0, colonIndex).trim();
                        foundKeys.add(key);
                    }
                }

                // create append keys
                HashMap<String, String> appendKeys = new HashMap<>();
                String prevKey = "";
                
                for (Map.Entry<String, String> entry : values.entrySet()) {
                    String key = entry.getKey();
        
                    if (!foundKeys.contains(key)) {
                        appendKeys.put(prevKey, key);
                    }
        
                    prevKey = key;
                }

                // create updated file data
                prevKey = "";
                for (String line : lines) {
                    while(appendKeys.containsKey(prevKey)) {
                        String addKey = appendKeys.get(prevKey);

                        if(this.headers.containsKey(addKey)) {
                            if(prevKey.length() > 0) {
                                updatedFile.add("");
                            }
                            updatedFile.add("# " + this.headers.get(addKey));
                        }

                        updatedFile.add(addKey + ": " + this.formatValue(this.values.get(addKey)));
                        appendKeys.remove(prevKey);
                        prevKey = addKey;
                    }

                    line = line.trim();
                    if(!line.isEmpty() && !line.startsWith("#") && line.contains(":")) {
                        int colonIndex = line.indexOf(":");
                        String key = line.substring(0, colonIndex).trim();
                        if(this.values.containsKey(key)) {
                            line = key + ": " + this.formatValue(this.values.get(key));
                            prevKey = key;
                        }
                    }

                    updatedFile.add(line);
                }

            } catch(Exception e) {
                e.printStackTrace();
            }
        } else {
            this.values.forEach((key, value) -> {
                if(this.headers.containsKey(key)) {
                    updatedFile.add("");
                    updatedFile.add("# " + this.headers.get(key));
                }

                updatedFile.add(key + ": " + value);
            });
        }

        try (final FileOutputStream fout = new FileOutputStream(configFile, false)) {
            OutputStreamWriter out = new OutputStreamWriter(new BufferedOutputStream(fout), StandardCharsets.UTF_8);
            out.write(String.join(this.lineSeparator, updatedFile));
            out.flush();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public String formatValue(String value) {
        if (value == null) {
            return null;
        }
        
        // Check if the value starts with "&"
        boolean startsWithAmpersand = value.startsWith("&");
        
        // Check if the value needs quotes
        boolean needsQuotes = false;
        
        // Check for special characters
        if (value.contains(":") || value.contains("-") || value.contains("[") || value.contains("]")) {
            needsQuotes = true;
        }
        
        // Check for whitespace
        if (value.trim().length() != value.length()) {
            needsQuotes = true;
        }
        
        // Check for special formats
        // Add more checks for other formats as needed
        
        if (startsWithAmpersand || needsQuotes) {
            return "\"" + escapeString(value) + "\"";
        } else {
            return value;
        }
    }
    
    // Additional function to escape special characters in the string
    private String escapeString(String value) {
        return value
            .replace("\\", "\\\\")   // Escape backslashes
            .replace("\"", "\\\"")   // Escape double quotes
            .replace("\n", "\\n")    // Escape newline
            .replace("\t", "\\t");   // Escape tab
    }
}
