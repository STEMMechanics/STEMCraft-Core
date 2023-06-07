package com.stemcraft.config;

import java.io.File;

public class ConfigFile extends Config {
    static {
        DEFAULT_VALUES.put("server-name", "STEMCraft");

        HEADERS.put("server-name", new String[] { "# CoreProtect is donationware. Obtain a donation key from coreprotect.net/donate/" });
    }

    public ConfigFile(File file) {
        super(file);
    }
}
