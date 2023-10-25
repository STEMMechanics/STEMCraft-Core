package com.stemcraft.feature;

import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.command.SMCommand;

public class SMRules extends SMFeature {
    private final static String RULE_BOOK_NAME = "server-rules";

    /**
     * When the feature is enabled
     */
    @Override
    protected Boolean onEnable() {
        new SMCommand("rules")
            .action(ctx -> {
                ctx.checkNotConsole();

                SMBooks bookFeature = STEMCraft.getFeature("SMBooks", SMBooks.class);
                ctx.checkBoolean(bookFeature.isEnabled(), "Book feature not enabled in STEMCraft");
                ctx.checkBoolean(bookFeature.bookExists(RULE_BOOK_NAME), "Rule book does not exist on server");
                
                bookFeature.showBook(ctx.player, RULE_BOOK_NAME);
            })
            .register();

        return true;
    }
}
