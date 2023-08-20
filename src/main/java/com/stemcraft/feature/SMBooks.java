package com.stemcraft.feature;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;

public class SMBooks extends SMFeature {
    private List<String> cacheList = new ArrayList<>();

    @Override
    protected Boolean onEnable() {
        this.plugin.getLanguageManager().registerPhrase("BOOK_USAGE", "Usage: /book <option>");
        this.plugin.getLanguageManager().registerPhrase("BOOK_USAGE_SAVE", "Usage: /book save <author> <title>");
        this.plugin.getLanguageManager().registerPhrase("BOOK_USAGE_GET", "Usage: /book get <name>");
        this.plugin.getLanguageManager().registerPhrase("BOOK_USAGE_SHOW", "Usage: /book show <name> (player)");
        this.plugin.getLanguageManager().registerPhrase("BOOK_USAGE_DEL", "Usage: /book del <name>");
        this.plugin.getLanguageManager().registerPhrase("BOOK_USAGE_UNLOCK", "Usage: /book unlock <name>");
        this.plugin.getLanguageManager().registerPhrase("BOOK_UNKNOWN_OPTION", ":warning_red: Unknown book option");
        this.plugin.getLanguageManager().registerPhrase("BOOK_INVENTORY_FULL", ":warning_red: &cYour inventory is full");
        this.plugin.getLanguageManager().registerPhrase("BOOK_GIVEN_NEW", ":info_blue: You have been given a new book. Use /book save <name> to save it");
        this.plugin.getLanguageManager().registerPhrase("BOOK_GIVEN", ":info_blue: You have received the book");
        this.plugin.getLanguageManager().registerPhrase("BOOK_SAVE_NOT_WRITABLE", ":warning_red: &cYou are not holding a writable book");
        this.plugin.getLanguageManager().registerPhrase("BOOK_SAVE_FAILED", ":warning_red: &cCould not save the book to the server");
        this.plugin.getLanguageManager().registerPhrase("BOOK_SAVE_NEW", ":info_blue: &bBook saved as %NAME%");
        this.plugin.getLanguageManager().registerPhrase("BOOK_SAVE_UPDATED", ":info_blue: &bBook updated");
        this.plugin.getLanguageManager().registerPhrase("BOOK_NOT_FOUND", ":warning_red: &cBook not found");
        this.plugin.getLanguageManager().registerPhrase("BOOK_UNLOCK_NOT_BOOK", ":warning_red: &cThe book you are holding is not unlockable");
        this.plugin.getLanguageManager().registerPhrase("BOOK_UNLOCK_SUCCESSFUL", ":info_blue: &bBook unlocked");
        this.plugin.getLanguageManager().registerPhrase("BOOK_DELETE_SUCCESSFUL", ":info_blue: &bBook deleted");

        this.plugin.getDatabaseManager().addMigration("230818073300_CreateBookTable", (databaseManager) -> {
            databaseManager.prepareStatement(
            "CREATE TABLE IF NOT EXISTS books (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL UNIQUE," +
                "content TEXT NOT NULL," +
                "created TIMESTAMP DEFAULT CURRENT_TIMESTAMP)").executeUpdate();
        });

        this.plugin.getDatabaseManager().addMigration("230818131500_UpdateBookTable", (databaseManager) -> {
            databaseManager.prepareStatement(
            "ALTER TABLE books " +
                "ADD COLUMN author TEXT DEFAULT ''").executeUpdate();
            databaseManager.prepareStatement(
            "ALTER TABLE books " +
                "ADD COLUMN title TEXT DEFAULT ''").executeUpdate();
        });

        this.buildCacheList();

        this.plugin.getCommandManager().registerTabPlaceholder("booknames", (Server server, String match) -> {
            return this.cacheList;
        });

        String commandName = "book";
        String[] aliases = new String[]{};
        String[][] tabCompletions = new String[][]{
            {commandName, "new"},
            {commandName, "save"},
            {commandName, "get", "%booknames%"},
            {commandName, "show", "%booknames%", "%player%"},
            {commandName, "del", "%booknames%"},
            {commandName, "unlock"},
        };

        // Book Command
        this.plugin.getCommandManager().registerCommand(commandName, (sender, command, label, args) -> {
            if(args.length < 1) {
                this.plugin.getLanguageManager().sendPhrase(sender, "BOOK_USAGE");
                return true;
            }

            if (!(sender instanceof Player)) {
                if(args[0].equalsIgnoreCase("del") == false && args[0].equalsIgnoreCase("show") == false) {
                    this.plugin.getLanguageManager().sendPhrase(sender, "CMD_ONLY_PLAYERS");
                    return true;
                }
            } else {
                if (!sender.hasPermission("stemcraft.book")) {
                    this.plugin.getLanguageManager().sendPhrase(sender, "CMD_NO_PERMISSION");
                    return true;
                }
            }

            if(args[0].equalsIgnoreCase("new")) {
                Player player = (Player)sender;

                Material material = Material.getMaterial("BOOK_AND_QUILL");
                if (material == null)
                    material = Material.getMaterial("WRITABLE_BOOK");
                if (material == null)
                    throw new UnsupportedOperationException("Something went wrong with Bukkit Material!");

                ItemStack item = new ItemStack(material);
                PlayerInventory inventory = player.getInventory();
                Map<Integer, ItemStack> result = inventory.addItem(item);
                if (!result.isEmpty()) {
                    this.plugin.getLanguageManager().sendPhrase(sender, "BOOK_INVENTORY_FULL");
                } else {
                    this.plugin.getLanguageManager().sendPhrase(sender, "BOOK_GIVEN_NEW");
                }
            } else if(args[0].equalsIgnoreCase("save")) {
                Player player = (Player)sender;

                if(args.length < 3) {
                    this.plugin.getLanguageManager().sendPhrase(sender, "BOOK_USAGE_SAVE");
                    return true;
                }

                ItemStack item = player.getInventory().getItemInMainHand();
                if(item.getType().toString() == "BOOK_AND_QUILL" || item.getType().toString() == "WRITABLE_BOOK") {
                    String author = args[1];
                    String title  = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                    String name = this.generateName(title);
                    BookMeta meta = (BookMeta) item.getItemMeta();
                    List<String> bookPages = meta.getPages();
                    List<String> newPages = new ArrayList<>(bookPages);
                    
                    for (int i = 0; i < newPages.size(); i++) {
                        String originalPage = newPages.get(i);

                        String parsedPage = this.plugin.getLanguageManager().parseString(originalPage);
                        newPages.set(i, parsedPage);
                    }

                    meta.setAuthor(this.plugin.getLanguageManager().parseString(author));
                    meta.setTitle(this.plugin.getLanguageManager().parseString(title));
                    meta.setPages(newPages);

                    ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
                    book.setItemMeta(meta);

                    player.getInventory().setItemInMainHand(book);
                    String content = String.join("<n>", newPages);

                    try {
                        PreparedStatement selectStatement = this.plugin.getDatabaseManager().prepareStatement(
                                "SELECT COUNT(*) FROM books WHERE name = ?");
                        selectStatement.setString(1, name);
                        ResultSet selectResult = selectStatement.executeQuery();
                        selectResult.next();

                        int rowCount = selectResult.getInt(1);

                        PreparedStatement statement;
                        if (rowCount > 0) {
                            // Update existing row
                            statement = this.plugin.getDatabaseManager().prepareStatement(
                                    "UPDATE books SET author = ?, title = ?, content = ? WHERE name = ?");

                            statement.setString(1, author);
                            statement.setString(2, title);
                            statement.setString(3, content);
                            statement.setString(4, name);
                        } else {
                            // Insert new row
                            statement = this.plugin.getDatabaseManager().prepareStatement(
                                    "INSERT INTO books (name, author, title, content) VALUES (?, ?, ?, ?)");
                            statement.setString(1, name);
                            statement.setString(2, author);
                            statement.setString(3, title);
                            statement.setString(4, content);
                        }

                        int rowsAffected = statement.executeUpdate();

                        HashMap<String, String> replacements = new HashMap<>();
                        replacements.put("NAME", name);

                        if (rowsAffected > 0) {
                            if(rowCount > 0) {
                                this.plugin.getLanguageManager().sendPhrase(player, "BOOK_SAVE_UPDATED", replacements);
                            } else {
                                this.plugin.getLanguageManager().sendPhrase(player, "BOOK_SAVE_NEW", replacements);
                            }
                        } else {
                            this.plugin.getLanguageManager().sendPhrase(player, "BOOK_SAVE_FAILED");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    this.plugin.getLanguageManager().sendPhrase(sender, "BOOK_SAVE_NOT_WRITABLE");
                }
            } else if(args[0].equalsIgnoreCase("get")) {
                Player player = (Player)sender;

                if(args.length < 2) {
                    this.plugin.getLanguageManager().sendPhrase(sender, "BOOK_USAGE_GET");
                    return true;
                }

                ItemStack book = this.getBook(args[1]);
                if(book != null) {
                    Map<Integer, ItemStack> result = player.getInventory().addItem(book);
                    if (!result.isEmpty()) {
                        this.plugin.getLanguageManager().sendPhrase(sender, "BOOK_INVENTORY_FULL");
                    } else {
                        this.plugin.getLanguageManager().sendPhrase(sender, "BOOK_GIVEN");
                    }
                } else {
                    this.plugin.getLanguageManager().sendPhrase(player, "BOOK_NOT_FOUND");
                }

            } else if(args[0].equalsIgnoreCase("show")) {
                Player targetPlayer = null;

                if(args.length < 2) {
                    this.plugin.getLanguageManager().sendPhrase(sender, "BOOK_USAGE_SHOW");
                    return true;
                }

                if(args.length > 2) {
                    targetPlayer = Bukkit.getPlayer(args[2]);
                    if (targetPlayer == null) {
                        this.plugin.getLanguageManager().sendPhrase(sender, "CMD_PLAYER_NOT_FOUND");
                        return true;
                    }
                } else if (!(sender instanceof Player)) {
                    this.plugin.getLanguageManager().sendPhrase(sender, "CMD_PLAYER_REQ_FROM_CONSOLE");
                    return true;
                } else {
                    targetPlayer = (Player)sender;
                }

                ItemStack book = this.getBook(args[1]);
                if(book != null) {
                    targetPlayer.openBook(book);
                } else {
                    this.plugin.getLanguageManager().sendPhrase(sender, "BOOK_NOT_FOUND");
                }
            } else if(args[0].equalsIgnoreCase("del")) {
                if(args.length < 2) {
                    this.plugin.getLanguageManager().sendPhrase(sender, "BOOK_USAGE_DEL");
                    return true;
                }

                try {
                    PreparedStatement selectStatement = this.plugin.getDatabaseManager().prepareStatement(
                            "SELECT COUNT(*) FROM books WHERE name = ?");
                    selectStatement.setString(1, args[1]);
                    ResultSet selectResult = selectStatement.executeQuery();
                    selectResult.next();

                    int rowCount = selectResult.getInt(1);

                    PreparedStatement statement;
                    if (rowCount > 0) {
                        statement = this.plugin.getDatabaseManager().prepareStatement(
                                "DELETE FROM books WHERE name = ?");

                        statement.setString(1, args[1]);
                        statement.executeUpdate();
                        this.plugin.getLanguageManager().sendPhrase(sender, "BOOK_DELETE_SUCCESSFUL");
                    } else {
                        this.plugin.getLanguageManager().sendPhrase(sender, "BOOK_NOT_FOUND");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else if(args[0].equalsIgnoreCase("unlock")) {
                Player player = (Player)sender;

                ItemStack item = player.getInventory().getItemInMainHand();
                if(item.getType() == Material.WRITTEN_BOOK) {
                    Material material = Material.getMaterial("BOOK_AND_QUILL");
                    if (material == null)
                        material = Material.getMaterial("WRITABLE_BOOK");
                    if (material == null)
                        throw new UnsupportedOperationException("Something went wrong with Bukkit Material!");

                    ItemStack book = new ItemStack(material);
                    BookMeta meta = (BookMeta) item.getItemMeta();
                    List<String> bookPages = meta.getPages();
                    List<String> newPages = new ArrayList<>(bookPages);
                    
                    for (int i = 0; i < newPages.size(); i++) {
                        String originalPage = newPages.get(i);

                        String parsedPage = this.untranslateColorCodes(originalPage);
                        newPages.set(i, parsedPage);
                    }

                    meta.setPages(newPages);
                    book.setItemMeta(meta);
                    player.getInventory().setItemInMainHand(book);
                    this.plugin.getLanguageManager().sendPhrase(sender, "BOOK_UNLOCK_SUCCESSFUL");
                } else {
                    this.plugin.getLanguageManager().sendPhrase(sender, "BOOK_UNLOCK_NOT_BOOK");
                }
            } else {
                this.plugin.getLanguageManager().sendPhrase(sender, "BOOK_UNKNOWN_OPTION");
            }

            this.buildCacheList();

            return true;
        }, aliases, tabCompletions);

        return true;
    }

    private ItemStack getBook(String name) {
        try {
            PreparedStatement statement = this.plugin.getDatabaseManager().prepareStatement(
                    "SELECT * FROM books WHERE name = ? LIMIT 1"
            );
            statement.setString(1, name);
            
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String author = resultSet.getString("author");
                String title = resultSet.getString("title");
                String content = resultSet.getString("content");

                ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
                BookMeta meta = (BookMeta)book.getItemMeta();

                meta.setAuthor(author);
                meta.setTitle(title);
                meta.setPages(content.split("<n>"));
                book.setItemMeta(meta);

                return book;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private String generateName(String title) {
        // Remove non-alpha characters
        title = title.replaceAll("[^a-zA-Z0-9\\s]", "");

        // Replace spaces with dashes
        title = title.replace(" ", "-");

        // Convert to lowercase
        title = title.toLowerCase();
        
        return title;
    }

    private void buildCacheList() {
        try {
            PreparedStatement statement = this.plugin.getDatabaseManager().prepareStatement(
                "SELECT name FROM books");
            ResultSet resultSet = statement.executeQuery();

            this.cacheList.clear();
            while(resultSet.next()) {
                String name = resultSet.getString("name");
                this.cacheList.add(name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String untranslateColorCodes(String input) {
        return input.replaceAll(ChatColor.COLOR_CHAR + "([0-9a-fk-or])", "&$1");
    }

    public void showBook(Player player, String name) {
        ItemStack book = this.getBook(name);
        if(book != null) {
            player.openBook(book);
        } else {
            this.plugin.getLanguageManager().sendPhrase(player, "BOOK_NOT_FOUND");
        }
    }
}
