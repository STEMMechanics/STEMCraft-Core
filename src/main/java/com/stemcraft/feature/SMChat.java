package com.stemcraft.feature;

import net.milkbowl.vault.chat.Chat;
import java.util.regex.Pattern;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMCommon;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.config.SMConfig;
import com.stemcraft.core.event.SMEvent;

/**
 * Set the player chat formatting
 */
public class SMChat extends SMFeature {
    // Format placeholders
    private static final String NAME_PLACEHOLDER = "{name}";
    private static final String MESSAGE_PLACEHOLDER = "{message}";
    private static final String PREFIX_PLACEHOLDER = "{prefix}";
    private static final String SUFFIX_PLACEHOLDER = "{suffix}";

    // Format placeholder patterns
    private static final Pattern PREFIX_PLACEHOLDER_PATTERN = Pattern.compile(PREFIX_PLACEHOLDER, Pattern.LITERAL);
    private static final Pattern SUFFIX_PLACEHOLDER_PATTERN = Pattern.compile(SUFFIX_PLACEHOLDER, Pattern.LITERAL);

    /** The default format */
    private static final String DEFAULT_FORMAT = "<{prefix}{name}{suffix}> {message}";

    /** The format used by this chat formatter instance */
    private String format;

    /**
     * The current Vault chat implementation registered on the server. Automatically updated as new services are
     * registered.
     */
    private Chat vaultChat = null;


    /**
     * When the feature is enabled
     */
    @Override
    protected Boolean onEnable() {
        this.vaultChat = STEMCraft.getPlugin().getServer().getServicesManager().load(Chat.class);
        if (this.vaultChat == null) {
            STEMCraft.info("Vault not enabled. Prefix/suffix will be ignored in chat formatting");
        }

        format = SMCommon.colorize(SMConfig.main().getString("chat-format", DEFAULT_FORMAT)
            .replace(NAME_PLACEHOLDER, "%1$s")
            .replace(MESSAGE_PLACEHOLDER, "%2$s"));

        // Event Chat - Set Format
        SMEvent.register(AsyncPlayerChatEvent.class, EventPriority.LOWEST, ctx -> {
            ctx.event.setFormat(this.format);
        });

        // Event Chat - Replace Placeholders
        SMEvent.register(AsyncPlayerChatEvent.class, EventPriority.HIGH, ctx -> {
            String format = ctx.event.getFormat();

            if (this.vaultChat != null) {
                format = SMCommon.replaceAll(PREFIX_PLACEHOLDER_PATTERN, format,
                    () -> SMCommon.colorize(this.vaultChat.getPlayerPrefix(ctx.event.getPlayer())));
                format = SMCommon.replaceAll(SUFFIX_PLACEHOLDER_PATTERN, format,
                    () -> SMCommon.colorize(this.vaultChat.getPlayerSuffix(ctx.event.getPlayer())));
            }

            ctx.event.setFormat(format);
        });

        return true;
    }
}
