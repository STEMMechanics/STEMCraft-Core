package com.stemcraft.manager;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import com.stemcraft.STEMCraft;
import com.stemcraft.feature.SMFeature;

public class SMFeatureManager extends SMManager {
    private HashMap<String, SMFeature> features = new HashMap<>();

    @Override
    public void onLoad(STEMCraft plugin) {
        super.onLoad(plugin);
        this.loadFeatures();
    }

    @Override
    public void onEnable() {
        this.features.forEach((name, clazz) -> {
            clazz.enable();
        });
    }

    @Override
    public void onDisable() {
        this.features.forEach((name, feature) -> {
            if(feature.isEnabled()) {
                feature.disable();
            }
        });
    }

    private void loadFeatures() {
        List<Class<?>> classFeatureList = this.plugin.getClassList("com/stemcraft/feature/", false);

        for (Class<?> classFeatureItem : classFeatureList) {
            if (classFeatureItem.getSimpleName().equals("SMFeature") || classFeatureItem.getSimpleName().equals("SMFeature")) {
                continue; // Skip this class
            }

            try {
                Constructor<?> constructor = classFeatureItem.getDeclaredConstructor();
                SMFeature featureInstance = (SMFeature) constructor.newInstance();
                String featureName = featureInstance.getName();
                if(featureName.length() > 0 && !this.features.containsKey(featureName)) {
                    if(featureInstance.onLoad(this.plugin)) {
                        this.features.put(featureName, featureInstance);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Boolean hasFeature(String name) {
        return this.features.containsKey(name);
    }

    public Boolean featureEnabled(String name) {
        if(this.hasFeature(name)) {
            return this.features.get(name).isEnabled();
        }
        
        return false;
    }

    public SMFeature getFeature(String name) {
        if(this.hasFeature(name)) {
            return this.features.get(name);
        }
        
        return null;
    }
}
