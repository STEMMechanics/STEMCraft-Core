package com.stemcraft.core;

import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import lombok.Getter;
import lombok.Setter;

public class SMMessenger {
    @Getter
    @Setter
    private static String infoPrefix = "";

    @Getter
    @Setter
    private static String successPrefix = "";

    @Getter
    @Setter
    private static String warnPrefix = "";

    @Getter
    @Setter
    private static String errorPrefix = "";

    @Getter
    @Setter
    private static String announcePrefix = "";

    /**
     * Send information message to sender.
     * 
     * @param sender
     * @param message
     */
    public static void info(final CommandSender sender, final String message) {
        tell(sender, infoPrefix, message);
    }

    /**
     * Send information message to sender.
     * 
     * @param sender
     * @param messages
     */
    public static void info(final CommandSender sender, final List<String> messages) {
        tell(sender, infoPrefix, messages);
    }

    /**
     * Send information message to sender.
     * 
     * @param sender
     * @param message
     */
    public static void infoLocale(final CommandSender sender, final String id) {
        tell(sender, infoPrefix, SMLocale.get(sender, id));
    }

    /**
     * Send information message to sender.
     * 
     * @param sender
     * @param message
     */
    public static void infoLocale(final CommandSender sender, final String id, String... replacements) {
        tell(sender, infoPrefix, SMReplacer.replaceVariables(SMLocale.get(sender, id), replacements));
    }

    /**
     * Send information message to sender.
     * 
     * @param player
     * @param message
     */
    public static void info(final Player player, final String message) {
        tell(player, infoPrefix, message);
    }

    /**
     * Send information message to sender.
     * 
     * @param player
     * @param messages
     */
    public static void info(final Player player, final List<String> messages) {
        tell(player, infoPrefix, messages);
    }

    /**
     * Send information message to sender.
     * 
     * @param player
     * @param message
     */
    public static void infoLocale(final Player player, final String id) {
        tell(player, infoPrefix, SMLocale.get(player, id));
    }

    /**
     * Send information message to sender.
     * 
     * @param player
     * @param message
     */
    public static void infoLocale(final Player player, final String id, String... replacements) {
        tell(player, infoPrefix, SMReplacer.replaceVariables(SMLocale.get(player, id), replacements));
    }

    /**
     * Send success message to sender with locale support.
     * 
     * @param sender the command sender
     * @param id the message id
     */
    public static void successLocale(final CommandSender sender, final String id) {
        tell(sender, successPrefix, SMLocale.get(sender, id));
    }

    /**
     * Send success message to sender with locale and replacement support.
     * 
     * @param sender the command sender
     * @param id the message id
     * @param replacements the replacements
     */
    public static void successLocale(final CommandSender sender, final String id, String... replacements) {
        tell(sender, successPrefix, SMReplacer.replaceVariables(SMLocale.get(sender, id), replacements));
    }

    /**
     * Send success message to player.
     * 
     * @param player the player
     * @param message the message
     */
    public static void success(final Player player, final String message) {
        tell(player, successPrefix, message);
    }

    /**
     * Send success messages to player.
     * 
     * @param player the player
     * @param messages the messages
     */
    public static void success(final Player player, final List<String> messages) {
        tell(player, successPrefix, messages);
    }

    /**
     * Send success message to sender.
     * 
     * @param sender the sender
     * @param message the message
     */
    public static void success(final CommandSender sender, final String message) {
        tell(sender, successPrefix, message);
    }

    /**
     * Send success messages to sender.
     * 
     * @param sender the sender
     * @param messages the messages
     */
    public static void success(final CommandSender sender, final List<String> messages) {
        tell(sender, successPrefix, messages);
    }

    /**
     * Send success message to player with locale support.
     * 
     * @param player the player
     * @param id the message id
     */
    public static void successLocale(final Player player, final String id) {
        tell(player, successPrefix, SMLocale.get(player, id));
    }

    /**
     * Send success message to player with locale and replacement support.
     * 
     * @param player the player
     * @param id the message id
     * @param replacements the replacements
     */
    public static void successLocale(final Player player, final String id, String... replacements) {
        tell(player, successPrefix, SMReplacer.replaceVariables(SMLocale.get(player, id), replacements));
    }

    // For warn
    /**
     * Send warning message to sender with locale support.
     * 
     * @param sender the command sender
     * @param id the message id
     */
    public static void warnLocale(final CommandSender sender, final String id) {
        tell(sender, warnPrefix, SMLocale.get(sender, id));
    }

    /**
     * Send warning message to sender with locale and replacement support.
     * 
     * @param sender the command sender
     * @param id the message id
     * @param replacements the replacements
     */
    public static void warnLocale(final CommandSender sender, final String id, String... replacements) {
        tell(sender, warnPrefix, SMReplacer.replaceVariables(SMLocale.get(sender, id), replacements));
    }

    /**
     * Send warning message to player.
     * 
     * @param sender the player
     * @param message the message
     */
    public static void warn(final CommandSender sender, final String message) {
        tell(sender, warnPrefix, message);
    }

    /**
     * Send warning messages to player.
     * 
     * @param sender the player
     * @param messages the messages
     */
    public static void warn(final CommandSender sender, final List<String> messages) {
        tell(sender, warnPrefix, messages);
    }

    /**
     * Send warning message to player.
     * 
     * @param player the player
     * @param message the message
     */
    public static void warn(final Player player, final String message) {
        tell(player, warnPrefix, message);
    }

    /**
     * Send warning messages to player.
     * 
     * @param player the player
     * @param messages the messages
     */
    public static void warn(final Player player, final List<String> messages) {
        tell(player, warnPrefix, messages);
    }

    /**
     * Send warning message to player with locale support.
     * 
     * @param player the player
     * @param id the message id
     */
    public static void warnLocale(final Player player, final String id) {
        tell(player, warnPrefix, SMLocale.get(player, id));
    }

    /**
     * Send warning message to player with locale and replacement support.
     * 
     * @param player the player
     * @param id the message id
     * @param replacements the replacements
     */
    public static void warnLocale(final Player player, final String id, String... replacements) {
        tell(player, warnPrefix, SMReplacer.replaceVariables(SMLocale.get(player, id), replacements));
    }

    // For error
    /**
     * Send error message to sender with locale support.
     * 
     * @param sender the command sender
     * @param id the message id
     */
    public static void errorLocale(final CommandSender sender, final String id) {
        tell(sender, errorPrefix, SMLocale.get(sender, id));
    }

    /**
     * Send error message to sender with locale and replacement support.
     * 
     * @param sender the command sender
     * @param id the message id
     * @param replacements the replacements
     */
    public static void errorLocale(final CommandSender sender, final String id, String... replacements) {
        tell(sender, errorPrefix, SMReplacer.replaceVariables(SMLocale.get(sender, id), replacements));
    }

    /**
     * Send error message to sender.
     * 
     * @param sender the sender
     * @param message the message
     */
    public static void error(final CommandSender sender, final String message) {
        tell(sender, errorPrefix, message);
    }

    /**
     * Send error messages to sender.
     * 
     * @param sender the sender
     * @param messages the messages
     */
    public static void error(final CommandSender sender, final List<String> messages) {
        tell(sender, errorPrefix, messages);
    }

    /**
     * Send error message to player.
     * 
     * @param player the player
     * @param message the message
     */
    public static void error(final Player player, final String message) {
        tell(player, errorPrefix, message);
    }

    /**
     * Send error messages to player.
     * 
     * @param player the player
     * @param messages the messages
     */
    public static void error(final Player player, final List<String> messages) {
        tell(player, errorPrefix, messages);
    }

    /**
     * Send error message to player with locale support.
     * 
     * @param player the player
     * @param id the message id
     */
    public static void errorLocale(final Player player, final String id) {
        tell(player, errorPrefix, SMLocale.get(player, id));
    }

    /**
     * Send error message to player with locale and replacement support.
     * 
     * @param player the player
     * @param id the message id
     * @param replacements the replacements
     */
    public static void errorLocale(final Player player, final String id, String... replacements) {
        tell(player, errorPrefix, SMReplacer.replaceVariables(SMLocale.get(player, id), replacements));
    }


    /**
     * Send announcement message to sender.
     * 
     * @param player
     * @param message
     */
    public static void announce(final CommandSender player, final String message) {
        tell(player, announcePrefix, message);
    }

    /**
     * Send a blank line to the sender.
     * 
     * @param player
     */
    public static void blankLine(final CommandSender player) {
        player.sendMessage("   ");
    }

    /*
     * Internal function to send message to sender.
     */
    private static void tell(final CommandSender player, final String prefix, String message) {
        String coloredPrefix = SMCommon.colorize(prefix);
        final String colorless = SMCommon.stripColors(message);

        // Remove prefix for console
        if (!(player instanceof Player)) {
            coloredPrefix = coloredPrefix.replaceAll(".*(?=(§[0-9a-fr])).*", "$1");
        }

        String parsedMessage =
            SMBridge.parse(coloredPrefix + colorless, player instanceof Player ? (Player) player : null);
        player.sendMessage(parsedMessage);
    }

    /*
     * Internal function to send message to sender.
     */
    private static void tell(final CommandSender player, final String prefix, List<String> messages) {
        String coloredPrefix = SMCommon.colorize(prefix);
        final String transformedPrefix =
            player instanceof Player ? coloredPrefix : coloredPrefix.replaceAll(".*(?=(§[0-9a-fr])).*", "$1");

        messages.forEach(message -> {
            final String colorless = SMCommon.stripColors(message);


            String parsedMessage =
                SMBridge.parse(transformedPrefix + colorless, player instanceof Player ? (Player) player : null);
            player.sendMessage(parsedMessage);
        });
    }

    public static void seperatorLine(final CommandSender player) {
        seperatorLine(player, null);
    }

    public static void seperatorLine(final CommandSender player, ChatColor color) {
        player.sendMessage(color == null ? "" : color + (ChatColor.STRIKETHROUGH + StringUtils.repeat(" ", 80)));
    }

    public static void seperatorLine(final Player player) {
        seperatorLine(player, null);
    }

    public static void seperatorLine(final Player player, ChatColor color) {
        player.sendMessage(color == null ? "" : color + (ChatColor.STRIKETHROUGH + StringUtils.repeat(" ", 80)));
    }
}
