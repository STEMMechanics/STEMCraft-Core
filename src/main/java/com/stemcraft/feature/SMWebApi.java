package com.stemcraft.feature;

import com.stemcraft.SMConfig;

public class SMWebApi extends SMFeature {
    @Override
    protected Boolean onEnable() {
        SMConfig config = this.plugin.getConfigManager().getConfig();

        config.registerSection("webapi");
        config.registerBoolean("api-enabled", false,          "Enable the Web API");
        config.registerInt("api-port",    7775,           "The port the Web API listen on");
        config.registerString("api-key",     "stemcraft",    "Web API Key");

        return true;
    }

}
