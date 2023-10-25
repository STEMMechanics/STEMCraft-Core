package com.stemcraft.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.config.SMConfigFile;

public class SMLocale {
    /*
     * The loaded locale files
     */
    private static final Map<String, SMConfigFile> localeFiles = new HashMap<>();

    /*
     * Locale file prefix.
     */
    private static String prefix = "messages_";

    /*
     * Locale file extension.
     */
    private static String extension = ".yml";

    private static final String DEFAULT_LOCALE = "en";

    /**
     * Load all the locale files. Will reload if called twice.
     */
    public final static void loadAll() {
        STEMCraft.iteratePluginFiles("", jarFile -> {
            String fileName = jarFile.getName();

            if (fileName.startsWith(prefix) && fileName.endsWith(extension)) {
                String locale = fileName.substring(prefix.length(), fileName.length() - extension.length());
                STEMCraft.info("Loaded built-in locale: " + locale);
                localeFiles.put(locale, new SMConfigFile(fileName));
            }
        });

        File dataFolder = STEMCraft.getPlugin().getDataFolder();
        if (dataFolder.exists()) {
            File[] matchingFiles = dataFolder.listFiles();

            for (File file : matchingFiles) {
                String fileName = file.getName();
                if (fileName.startsWith(prefix) && fileName.endsWith(extension)) {
                    String locale = fileName.substring(prefix.length(), fileName.length() - extension.length());
                    if(!localeFiles.containsKey(locale)) {
                        STEMCraft.info("Replaced locale: " + locale);
                        localeFiles.put(locale, new SMConfigFile(fileName));
                    }
                }
            }
        }
    }

    /**
     * Get a locale string from the messages file.
     * @param id
     * @return
     */
    public final static String get(String id) {
        return get(DEFAULT_LOCALE, id);
    }

    /**
     * Get a locale string from the messages file using the players locale.
     * @param player
     * @param id
     * @return
     */
    public final static String get(Player player, String id) {
        return get(player.getLocale(), id);
    }

    /**
     * Get a locale string from the messages file using the sender locale.
     * @param sender
     * @param id
     * @return
     */
    public final static String get(CommandSender sender, String id) {
        String locale = DEFAULT_LOCALE;
        
        if(sender instanceof Player) {
            locale = ((Player)sender).getLocale();
        }

        return get(locale, id);
    }

    /**
     * Get a locale string from the messages file using the specified locale.
     * @param locale
     * @param id
     * @return
     */
    public final static String get(String locale, String id) {
        return get(locale, id, id);
    }

    /**
     * Get a locale string from the messages file using the specified locale.
     * If it does not exist, use the defValue.
     * @param locale
     * @param id
     * @param defValue
     * @return
     */
    public final static String get(String locale, String id, String defValue) {
        if(localeFiles.containsKey(locale)) {
            SMConfigFile localeFile = localeFiles.get(locale);
            if(localeFile.contains(id)) {
                return localeFiles.get(locale).getString(id);
            }
        }

        SMDebugger.debug("locale", "Missing locale string " + locale + " for " + id);

        if(locale != "en") {
            return get("en", id, defValue);
        }

        return defValue;
    }

    /**
     * Save a locale string.
     * @param locale
     * @param id
     * @param string
     */
    public final static void setDefault(String locale, String id, String string) {
        SMConfigFile localeFile = null;

        if(!localeFiles.containsKey(locale)) {
            localeFile = new SMConfigFile(prefix + locale + extension);
            localeFiles.put(locale, localeFile);
        } else {
            localeFile = localeFiles.get(locale);
        }

        if(!localeFile.contains(id)) {
            localeFile.setDefault(id, string);
        }
    }
}
