package com.stemcraft.utility;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class ChatPaginator {

    public static void display(CommandSender sender, String title, List<BaseComponent[]> content, int page, int maxPages, String command) {
        // Title
        sender.spigot().sendMessage(createSeperatorString(ChatColor.AQUA + title));
        
        // Display the content for the current page
        for (int i = 0; i < content.size(); i++) {
            sender.spigot().sendMessage(content.get(i));
        }

        // Pagination
        BaseComponent prev = new TextComponent((page <= 1 ? ChatColor.GRAY : ChatColor.GOLD) + "<<< ");
        if(page > 1) {
            prev.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command + " " + (page - 1)));
            prev.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Previous page")));
        }

        BaseComponent pageInfo = new TextComponent(ChatColor.YELLOW + "Page " + ChatColor.GOLD + page + ChatColor.YELLOW + " of " + maxPages);

        BaseComponent next = new TextComponent((page >= maxPages ? ChatColor.GRAY : ChatColor.GOLD) + " >>>");
        if(page < maxPages) {
            next.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command + " " + (page + 1)));
            next.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Next page")));
        }

        BaseComponent components[] = { prev, pageInfo, next };
        sender.spigot().sendMessage(createSeperatorString(components));
    }

    public static BaseComponent[] createSeperatorString(String title) {
        TextComponent textComponent = new TextComponent(title);
        return createSeperatorString(new BaseComponent[]{textComponent});
    }

    public static BaseComponent[] createSeperatorString(BaseComponent title) {
        return createSeperatorString(new BaseComponent[]{title});
    }

    public static BaseComponent[] createSeperatorString(BaseComponent[] titles) {
        StringBuilder plainTitleBuilder = new StringBuilder();
        for (BaseComponent title : titles) {
            plainTitleBuilder.append(title.toPlainText());
        }
        String plainTitle = plainTitleBuilder.toString();
        String separator = "-";

        int maxLength = 58;
        int titleLength = ChatColor.stripColor(plainTitle).length();
        int separatorLength = (maxLength - titleLength - 4) / 2;

        String separatorStr = ChatColor.YELLOW + separator.repeat(separatorLength) + ChatColor.RESET;

        BaseComponent[] components = {
                new TextComponent(separatorStr + " "),
                new TextComponent(titles),
                new TextComponent(" " + separatorStr)
        };

        return components;
    }
}
