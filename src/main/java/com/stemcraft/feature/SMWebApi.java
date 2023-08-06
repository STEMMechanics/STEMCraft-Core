package com.stemcraft.feature;

import com.stemcraft.SMConfig;

public class SMWebApi extends SMFeature {
    @Override
    protected Boolean onEnable() {
        SMConfig config = this.plugin.getConfigManager().getConfig();

        config.registerValue("api-enabled", false,          "Enable the Web API");
        config.registerValue("api-port",    7775,           "The port the Web API listen on");
        config.registerValue("api-key",     "stemcraft",    "Web API Key");

        return true;
    }

}
