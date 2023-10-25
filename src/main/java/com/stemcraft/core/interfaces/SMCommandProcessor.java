package com.stemcraft.core.interfaces;

import com.stemcraft.core.command.SMCommandContext;

@FunctionalInterface
public interface SMCommandProcessor {
    void process(SMCommandContext context);
}
