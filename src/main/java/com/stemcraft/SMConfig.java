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
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.Block;
import dev.dejvokep.boostedyaml.block.implementation.Section;

public class SMConfig {
    private STEMCraft plugin;
    private String fileName = "";
    private String lineSeparator = "\n";
    private String fileHeader = "";
    private Boolean loaded = false;
    private LinkedHashMap<String, String> values = new LinkedHashMap<>();
    private HashMap<String, String> headers = new HashMap<>();

    private YamlDocument yamlDocument = null;

    public SMConfig(STEMCraft plugin) {
        this(plugin, "config");
    }

    public SMConfig(STEMCraft plugin, String name) {
        this.plugin = plugin;
        this.fileName = name + ".yml";
    }

    // UPDATE
    public void setLineSeparator(String seperator) {
        this.lineSeparator = seperator;
    }

    // UPDATE
    public void setHeader(String header) {
        this.fileHeader = "# " + header;
    }

    // UPDATE
    public Boolean isLoaded() {
        return this.loaded;
    }

    public Boolean getBoolean(String name) {
        return this.yamlDocument.getBoolean(name, false);
    }
    
    public Boolean getBoolean(String name, Boolean defaultValue) {
        return this.yamlDocument.getBoolean(name, defaultValue);
    }

    public Integer getInt(String name) {
        return this.yamlDocument.getInt(name, 0);
    }
    
    public Integer getInt(String name, Integer defaultValue) {
        return this.yamlDocument.getInt(name, defaultValue);
    }

    public String getString(String name) {
        return this.yamlDocument.getString(name, "");
    }
    
    public String getString(String name, String defaultValue) {
        return this.yamlDocument.getString(name, defaultValue);
    }

    public List<String> getStringList(String name) {
        List<String> emptyList = new ArrayList<>();
        return this.yamlDocument.getStringList(name, emptyList);
    }
    
    public List<String> getStringList(String name, List<String> defaultValue) {
        return this.yamlDocument.getStringList(name, defaultValue);
    }

    public Boolean registerBoolean(String name, Boolean defaultValue) {
        return this.registerBoolean(name, defaultValue, "");
    }

    public Boolean registerBoolean(String name, Boolean defaultValue, String header) {
        if(this.yamlDocument.contains(name) == false) {
            this.yamlDocument.set(name, defaultValue);

            if(header.length() > 0) {
                this.yamlDocument.getBlock(name).addComment(" " + header);
            }
        }
        
        return this.yamlDocument.getBoolean(name);
    }

    public Integer registerInt(String name, Integer defaultValue) {
        return this.registerInt(name, defaultValue, "");
    }

    public Integer registerInt(String name, Integer defaultValue, String header) {
        if(this.yamlDocument.contains(name) == false) {
            this.yamlDocument.set(name, defaultValue);

            if(header.length() > 0) {
                this.yamlDocument.getBlock(name).addComment(" " + header);
            }
        }
        
        return this.yamlDocument.getInt(name);
    }

    public String registerString(String name, String defaultValue) {
        return this.registerString(name, defaultValue, "");
    }

    public String registerString(String name, String defaultValue, String header) {
        if(this.yamlDocument.contains(name) == false) {
            this.yamlDocument.set(name, defaultValue);

            if(header.length() > 0) {
                this.yamlDocument.getBlock(name).addComment(" " + header);
            }
        }
        
        return this.yamlDocument.getString(name);
    }

    public List<String> registerStringList(String name, List<String> defaultValue) {
        return this.registerStringList(name, defaultValue, "");
    }

    public List<String> registerStringList(String name, List<String> defaultValue, String header) {
        if(defaultValue == null) {
            defaultValue = new ArrayList<>();
        }

        if(this.yamlDocument.contains(name) == false) {
            this.yamlDocument.set(name, defaultValue);

            if(header.length() > 0) {
                this.yamlDocument.getBlock(name).addComment(" " + header);
            }
        }
        
        return this.yamlDocument.getStringList(name);
    }

    public void registerSection(String name) {
        this.registerSection(name, "");
    }

    public void registerSection(String name, String comment) {
        Section section = this.yamlDocument.createSection(name);
        if(comment.length() > 0) {
            section.addComment(comment);
        }
    }

    /**
     * Load the Config file
     */
    public void loadConfig() {
        try {
            File yamlFile = new File(this.plugin.getDataFolder(), this.fileName);
            Boolean exists = yamlFile.exists();

            this.yamlDocument = YamlDocument.create(yamlFile);
            if(exists == false) {
                if(this.fileHeader.length() > 0) {
                    this.yamlDocument.addComment(" " + this.fileHeader);
                }

                this.yamlDocument.set("webapi.cats", true);
                this.yamlDocument.getBlock("webapi.cats").addComment("Cats comment");

            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Save the Config file
     */
    public void saveConfig() {
        try {
            if(this.yamlDocument != null) {
                this.yamlDocument.save();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reload the Config file (without saving)
     */
    public void reloadConfig() {
        try {
            if(this.yamlDocument != null) {
                this.yamlDocument.reload();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
