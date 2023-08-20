package com.stemcraft.feature;

import net.milkbowl.vault.chat.Chat;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class SMChatFormat extends SMFeature {
    // Format placeholders
    private static final String NAME_PLACEHOLDER = "{name}";
    private static final String DISPLAYNAME_PLACEHOLDER = "{displayname}";
    private static final String MESSAGE_PLACEHOLDER = "{message}";
    private static final String PREFIX_PLACEHOLDER = "{prefix}";
    private static final String SUFFIX_PLACEHOLDER = "{suffix}";

    // Format placeholder patterns
    private static final Pattern NAME_PLACEHOLDER_PATTERN = Pattern.compile(NAME_PLACEHOLDER, Pattern.LITERAL);
    private static final Pattern PREFIX_PLACEHOLDER_PATTERN = Pattern.compile(PREFIX_PLACEHOLDER, Pattern.LITERAL);
    private static final Pattern SUFFIX_PLACEHOLDER_PATTERN = Pattern.compile(SUFFIX_PLACEHOLDER, Pattern.LITERAL);

    /** The default format */
    private static final String DEFAULT_FORMAT = "<" + PREFIX_PLACEHOLDER + NAME_PLACEHOLDER + SUFFIX_PLACEHOLDER + "> " + MESSAGE_PLACEHOLDER;

    /** Pattern matching "nicer" legacy hex chat color codes - &#rrggbb */
    private static final Pattern NICER_HEX_COLOR_PATTERN = Pattern.compile("&#([0-9a-fA-F]{6})");

    /** The format used by this chat formatter instance */
    private String format;

    /**
     * The current Vault chat implementation registered on the server.
     * Automatically updated as new services are registered.
     */
    private Chat vaultChat = null;

    @Override
    protected Boolean onEnable() {
        this.vaultChat = this.plugin.getServer().getServicesManager().load(Chat.class);
        if(this.vaultChat == null) {
            this.plugin.getLogger().severe("Could not connect to the Vault plugin");
            return false;
        }

        String configFormat = this.plugin.getConfigManager().getConfig().registerValue("chat-format", DEFAULT_FORMAT, "Chat format on the server");

        this.format = colorize(configFormat
            .replace(DISPLAYNAME_PLACEHOLDER, "%1$s")
            .replace(MESSAGE_PLACEHOLDER, "%2$s"));

        this.plugin.getEventManager().registerEvent(AsyncPlayerChatEvent.class, EventPriority.LOWEST, (listener, rawEvent) -> {
            AsyncPlayerChatEvent event = (AsyncPlayerChatEvent)rawEvent;

            event.setFormat(this.format);
        });

        this.plugin.getEventManager().registerEvent(AsyncPlayerChatEvent.class, EventPriority.HIGH, (listener, rawEvent) -> {
            AsyncPlayerChatEvent event = (AsyncPlayerChatEvent)rawEvent;

            String format = event.getFormat();

            if (this.vaultChat != null) {
                format = replaceAll(PREFIX_PLACEHOLDER_PATTERN, format, () -> colorize(this.vaultChat.getPlayerPrefix(event.getPlayer())));
                format = replaceAll(SUFFIX_PLACEHOLDER_PATTERN, format, () -> colorize(this.vaultChat.getPlayerSuffix(event.getPlayer())));
            } else {
                System.out.println("vault null");
            }
            format = replaceAll(NAME_PLACEHOLDER_PATTERN, format, () -> event.getPlayer().getName());

            event.setFormat(format);
        });

        return true;
    }

    /**
     * Equivalent to {@link String#replace(CharSequence, CharSequence)}, but uses a
     * {@link Supplier} for the replacement.
     *
     * @param pattern the pattern for the replacement target
     * @param input the input string
     * @param replacement the replacement
     * @return the input string with the replacements applied
     */
    private static String replaceAll(Pattern pattern, String input, Supplier<String> replacement) {
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.replaceAll(Matcher.quoteReplacement(replacement.get()));
        }
        return input;
    }

    /**
     * Translates color codes in the given input string.
     *
     * @param string the string to "colorize"
     * @return the colorized string
     */
    private static String colorize(String string) {
        if (string == null) {
            return "null";
        }

        // Convert from the '&#rrggbb' hex color format to the '&x&r&r&g&g&b&b' one used by Bukkit.
        Matcher matcher = NICER_HEX_COLOR_PATTERN.matcher(string);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            StringBuilder replacement = new StringBuilder(14).append("&x");
            for (char character : matcher.group(1).toCharArray()) {
                replacement.append('&').append(character);
            }
            matcher.appendReplacement(sb, replacement.toString());
        }
        matcher.appendTail(sb);

        // Translate from '&' to '§' (section symbol)
        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }
}
