package com.stemcraft.feature;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.stemcraft.STEMCraft;

public class SMWorldGuard extends SMFeature {
    public Boolean allowFlagType = false;
    public String nameFlagType = "sm-region-type";

    @Override
    public Boolean onLoad(STEMCraft plugin) {
        if(!super.onLoad(plugin)) {
            return false;
        }

        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StringFlag flag = new StringFlag(this.nameFlagType);
            registry.register(flag);
            this.allowFlagType = true;
        } catch (FlagConflictException e) {
            Flag<?> existing = registry.get(this.nameFlagType);
            if (existing instanceof StringFlag) {
                this.allowFlagType = true;
            }
        }

        return true;
    }
}