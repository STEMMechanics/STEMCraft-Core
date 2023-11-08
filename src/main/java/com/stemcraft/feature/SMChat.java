package com.stemcraft.feature;

import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMCommon;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.SMLocale;
import com.stemcraft.core.command.SMCommand;
import com.stemcraft.core.config.SMConfig;
import com.stemcraft.core.config.SMConfigFile;
import com.stemcraft.core.event.SMEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEditBookEvent;

/**
 * Set the player chat formatting
 */
public class SMChat extends SMFeature {

    private class SMChatFilterAction {
        @Getter
        private String action;

        SMChatFilterAction(String action) {
            this.action = action;
        }
    }

    /**
     * Chat filter data
     */
    private class SMChatFilter {
        @Getter
        private String id;

        @Getter
        private String label;

        @Getter
        private SMChatFilterAction action;

        @Getter
        private List<String> list;

        /**
         * Constructor.
         * 
         * @param id The filter id.
         * @param label The filter label (or null).
         * @param action The filter action.
         * @param list The filter keyworld list.
         */
        public SMChatFilter(String id, String label, String action, List<String> list) {
            this.id = id;
            this.label = (label == null || label.isEmpty()) ? id : label;
            this.action = new SMChatFilterAction(action);
            this.list = list;
        }

        /**
         * Test a string if it passes the filter keyword list.
         * 
         * @param s The string to test.
         * @return Boolean if the string passes the keyword list.
         */
        public Boolean passes(String s) {
            for (String item : list) {
                if (item.startsWith("regex:")) {
                    Pattern pattern = Pattern.compile(item.substring(6));
                    Matcher matcher = pattern.matcher(s);
                    if (matcher.find()) {
                        return true;
                    }
                } else {
                    if (s.contains(item)) {
                        return false;
                    }
                }
            }

            return true;
        }
    };

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

    /** Handle to the chat filter config file */
    private SMConfigFile chatFilterConfig = null;

    /** The keyword filter list */
    private ArrayList<SMChatFilter> filterList = new ArrayList<>();

    /** Player return message tracking */
    private final HashMap<UUID, UUID> lastTellMessageFrom = new HashMap<>();

    /**
     * When the feature is enabled
     */
    @Override
    protected Boolean onEnable() {
        this.vaultChat = STEMCraft.getPlugin().getServer().getServicesManager().load(Chat.class);
        if (this.vaultChat == null) {
            STEMCraft.info("Vault not enabled. Prefix/suffix will be ignored in chat formatting");
        }

        // Start of config migration from 1.1.0
        if (SMConfig.main().contains("chat-format")) {
            if (!SMConfig.main().contains("chat.format")) {
                SMConfig.main().set("chat.format", SMConfig.main().getString("chat-format", DEFAULT_FORMAT));
            }

            SMConfig.main().remove("chat-format");
        }
        // End of config migration from 1.1.0

        format = SMCommon.colorize(SMConfig.main().getString("chat.format", DEFAULT_FORMAT)
            .replace(NAME_PLACEHOLDER, "%1$s")
            .replace(MESSAGE_PLACEHOLDER, "%2$s"));


        /** Load Chat Filtering */
        chatFilterConfig = SMConfig.getOrLoadConfig("chat.yml");
        if (chatFilterConfig != null) {
            List<String> filterListIds = chatFilterConfig.getKeys("chat");
            if (filterListIds != null && !filterListIds.isEmpty()) {
                for (String id : filterListIds) {
                    String label = chatFilterConfig.getString("chat." + id + ".label", id);
                    String action = chatFilterConfig.getString("chat." + id + ".action", "kick");
                    List<String> keywords = chatFilterConfig.getStringList("chat." + id + ".list");

                    if (keywords == null) {
                        continue;
                    }

                    SMChatFilter chatFilterItem = new SMChatFilter(id, label, action, keywords);
                    filterList.add(chatFilterItem);

                    STEMCraft.info("Loaded chat filter list: " + label);
                }
            }
        }

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

        /** Event Chat - Filter */
        SMEvent.register(AsyncPlayerChatEvent.class, ctx -> {
            if (isFiltered(ctx.event.getPlayer(), ctx.event.getMessage())) {
                ctx.event.setCancelled(true);
            }
        });

        /** Event Command Preprocess - Filter */
        SMEvent.register(PlayerCommandPreprocessEvent.class, ctx -> {
            if (isFiltered(ctx.event.getPlayer(), ctx.event.getMessage())) {
                ctx.event.setCancelled(true);
            }
        });

        /** Event Sign Change - Filter */
        SMEvent.register(SignChangeEvent.class, ctx -> {
            for (String line : ctx.event.getLines()) {
                if (isFiltered(ctx.event.getPlayer(), line)) {
                    ctx.event.setCancelled(true);
                    break;
                }
            }
        });

        /** Event Player Edit Book - Filter */
        SMEvent.register(PlayerEditBookEvent.class, ctx -> {
            ctx.event.getNewBookMeta().getPages().forEach(page -> {
                if (isFiltered(ctx.event.getPlayer(), page)) {
                    ctx.event.setCancelled(true);
                }
            });
        });

        /** Send private message */
        new SMCommand("t")
            .alias("tell", "pm", "msg")
            .tabComplete("{player}")
            .permission("stemcraft.chat.tell")
            .action(ctx -> {
                ctx.checkArgs(2, SMLocale.get("CHAT_TELL_USAGE"));

                Player targetPlayer = ctx.getArgAsPlayer(1, null);
                ctx.checkNotNullLocale(targetPlayer, "CMD_PLAYER_NOT_FOUND");

                if (!ctx.fromConsole() && targetPlayer.getUniqueId().equals(ctx.player.getUniqueId())) {
                    ctx.returnErrorLocale("CHAT_TELL_NO_MSG_TO_SELF");
                }

                String message = String.join(" ", ctx.args).substring(ctx.args.get(0).length() + 1);
                targetPlayer.sendMessage(targetPlayer.getDisplayName() + " whispers: " + message);
                targetPlayer.playSound(targetPlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);

                lastTellMessageFrom.put(targetPlayer.getUniqueId(),
                    ctx.fromConsole() ? null : ctx.player.getUniqueId());
            });

        /** Send private message */
        new SMCommand("r")
            .permission("stemcraft.chat.tell")
            .action(ctx -> {
                ctx.checkNotConsole();

                UUID replyUuid = lastTellMessageFrom.get(ctx.player.getUniqueId());
                ctx.checkNotNullLocale(replyUuid, "CHAT_TELL_NO_REPLY");

                Player targetPlayer = Bukkit.getServer().getPlayer(replyUuid);
                ctx.checkNotNullLocale(replyUuid, "CMD_PLAYER_NOT_FOUND");
                ctx.checkBooleanLocale(targetPlayer.isOnline(), "CMD_PLAYER_NOT_FOUND");

                String message = String.join(" ", ctx.args);
                targetPlayer.sendMessage(targetPlayer.getDisplayName() + " whispers: " + message);
                targetPlayer.playSound(targetPlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                lastTellMessageFrom.put(targetPlayer.getUniqueId(), ctx.player.getUniqueId());
            });

        return true;
    }

    public Boolean isFiltered(Player player, String s) {
        if (player.hasPermission("stemcraft.chat.override")) {
            return false;
        }

        for (SMChatFilter filter : filterList) {
            if (!filter.passes(s)) {
                return true;
            }
        }

        return false;
    }
}
