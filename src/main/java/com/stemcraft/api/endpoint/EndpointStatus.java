package com.stemcraft.api.endpoint;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import com.stemcraft.api.SMApi;
import com.stemcraft.api.SMApiEndpoint;
import com.stemcraft.api.SMApiResponse;
import com.stemcraft.api.SMApi.HttpMethod;

public class EndpointStatus extends SMApiEndpoint {
    public void doRegister() {
        SMApi.registerEndpoint(HttpMethod.GET, "/status", this);
    }

    public SMApiResponse doGet(String path, Map<String, String> variables) {
        int playerCount = Bukkit.getOnlinePlayers().size();
        double mspt = getMspt();

        Map<String, Object> data = new HashMap<>();
        data.put("players", playerCount);
        data.put("mspt", mspt);

        return new SMApiResponse(200, data);
    }

    private double getMspt() {
        Plugin paperPlugin = Bukkit.getPluginManager().getPlugin("Paper");
        if (paperPlugin != null) {
            try {
                Class<?> paperConfigClass = Class.forName("com.destroystokyo.paper.PaperConfig");
                Object paperConfig = paperConfigClass.getMethod("getINSTANCE").invoke(null);
                double mspt = (double) paperConfigClass.getMethod("getAverageTickTime").invoke(paperConfig);
                return Math.round(mspt * 100.0) / 100.0; // Rounded to two decimal places
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return 0;
    }
}
