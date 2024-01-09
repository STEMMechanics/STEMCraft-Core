package com.stemcraft.feature;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMCommon;
import com.stemcraft.core.SMDatabase;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.SMLocale;
import com.stemcraft.core.SMMessenger;
import com.stemcraft.core.command.SMCommand;
import com.stemcraft.core.tabcomplete.SMTabComplete;

/**
 * Allows the creation of custom books that can be saved and shown to the player using a command
 */
public class SMBooks extends SMFeature {
    private List<String> cacheList = new ArrayList<>();

    /**
     * When feature is enabled
     */
    @Override
    protected Boolean onEnable() {

        SMDatabase.runMigration("230818073300_CreateBookTable", () -> {
            SMDatabase.prepareStatement(
                "CREATE TABLE IF NOT EXISTS books (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL UNIQUE," +
                    "content TEXT NOT NULL," +
                    "created TIMESTAMP DEFAULT CURRENT_TIMESTAMP)")
                .executeUpdate();
        });

        SMDatabase.runMigration("230818131500_UpdateBookTable", () -> {
            SMDatabase.prepareStatement(
                "ALTER TABLE books " +
                    "ADD COLUMN author TEXT DEFAULT ''")
                .executeUpdate();
            SMDatabase.prepareStatement(
                "ALTER TABLE books " +
                    "ADD COLUMN title TEXT DEFAULT ''")
                .executeUpdate();
        });

        this.buildCacheList();

        SMTabComplete.register("book", () -> {
            return this.cacheList;
        });

        new SMCommand("book")
            .tabComplete("new")
            .tabComplete("save")
            .tabComplete("get", "{book}")
            .tabComplete("show", "{book}", "{player}")
            .tabComplete("del", "{book}")
            .tabComplete("unlock")
            .permission("stemcraft.book")
            .action(ctx -> {
                // Check there are args
                ctx.checkArgsLocale(1, "BOOK_USAGE");

                String sub = ctx.args.get(0).toLowerCase();

                // Sub command - new
                if ("new".equals(sub)) {
                    ctx.checkNotConsole();
                    ctx.checkPermission("stemcraft.book.edit");

                    Material material = Material.getMaterial("BOOK_AND_QUILL");
                    if (material == null)
                        material = Material.getMaterial("WRITABLE_BOOK");
                    if (material == null)
                        throw new UnsupportedOperationException("Something went wrong with Bukkit Material!");

                    ItemStack item = new ItemStack(material);
                    PlayerInventory inventory = ctx.player.getInventory();
                    Map<Integer, ItemStack> result = inventory.addItem(item);
                    if (!result.isEmpty()) {
                        ctx.returnErrorLocale("BOOK_INVENTORY_FULL");
                    } else {
                        ctx.returnInfoLocale("BOOK_GIVEN_NEW");
                    }

                    // Sub command - save
                } else if ("save".equals(sub)) {
                    ctx.checkNotConsole();
                    ctx.checkPermission("stemcraft.book.edit");
                    ctx.checkArgsLocale(3, "BOOK_USAGE_SAVE");

                    // Player holding a writable book?
                    ItemStack item = ctx.player.getInventory().getItemInMainHand();
                    if (item.getType().toString() != "BOOK_AND_QUILL" && item.getType().toString() != "WRITABLE_BOOK") {
                        ctx.returnErrorLocale("BOOK_SAVE_NOT_WRITABLE");
                    }

                    String author = ctx.args.get(1);
                    String title = String.join(" ", ctx.args.subList(2, ctx.args.size()));
                    String name = this.generateName(title);
                    BookMeta meta = (BookMeta) item.getItemMeta();
                    List<String> bookPages = meta.getPages();
                    List<String> newPages = new ArrayList<>(bookPages);

                    for (int i = 0; i < newPages.size(); i++) {
                        String originalPage = newPages.get(i);

                        String parsedPage = SMCommon.format(originalPage);
                        newPages.set(i, parsedPage);
                    }

                    meta.setAuthor(SMCommon.format(author));
                    meta.setTitle(SMCommon.format(title));
                    meta.setPages(newPages);

                    ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
                    book.setItemMeta(meta);

                    ctx.player.getInventory().setItemInMainHand(book);
                    String content = String.join("<n>", newPages);

                    try {
                        PreparedStatement selectStatement = SMDatabase.prepareStatement(
                            "SELECT COUNT(*) FROM books WHERE name = ?");
                        selectStatement.setString(1, name);
                        ResultSet selectResult = selectStatement.executeQuery();
                        selectResult.next();

                        int rowCount = selectResult.getInt(1);

                        PreparedStatement statement;
                        if (rowCount > 0) {
                            // Update existing row
                            statement = SMDatabase.prepareStatement(
                                "UPDATE books SET author = ?, title = ?, content = ? WHERE name = ?");

                            statement.setString(1, author);
                            statement.setString(2, title);
                            statement.setString(3, content);
                            statement.setString(4, name);
                        } else {
                            // Insert new row
                            statement = SMDatabase.prepareStatement(
                                "INSERT INTO books (name, author, title, content) VALUES (?, ?, ?, ?)");
                            statement.setString(1, name);
                            statement.setString(2, author);
                            statement.setString(3, title);
                            statement.setString(4, content);
                        }

                        int rowsAffected = statement.executeUpdate();

                        if (rowsAffected > 0) {
                            if (rowCount > 0) {
                                SMMessenger.successLocale(ctx.sender, "BOOK_SAVE_UPDATED", "name", name);
                            } else {
                                SMMessenger.successLocale(ctx.sender, "BOOK_SAVE_NEW", "name", name);
                            }
                        } else {
                            SMMessenger.errorLocale(ctx.sender, "BOOK_SAVE_FAILED", "name", name);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    // Sub command - get
                } else if ("get".equals(sub)) {
                    ctx.checkNotConsole();
                    ctx.checkArgsLocale(2, "BOOK_USAGE_GET");

                    ItemStack book = this.getBook(ctx.args.get(1));
                    if (book != null) {
                        Map<Integer, ItemStack> result = ctx.player.getInventory().addItem(book);
                        if (!result.isEmpty()) {
                            ctx.returnErrorLocale("BOOK_INVENTORY_FULL");
                        } else {
                            ctx.returnSuccessLocale("BOOK_GIVEN");
                        }
                    } else {
                        ctx.returnErrorLocale("BOOK_NOT_FOUND");
                    }

                    // Sub command - show
                } else if ("show".equals(sub)) {
                    ctx.checkArgsLocale(2, "BOOK_USAGE_SHOW");
                    ctx.checkBooleanLocale(!(ctx.fromConsole() && ctx.args.size() < 3), "CMD_PLAYER_REQ_FROM_CONSOLE");

                    Player targetPlayer = ctx.getArgAsPlayer(3, ctx.player);
                    ctx.checkNotNullLocale(targetPlayer, "CMD_PLAYER_NOT_FOUND");

                    ItemStack book = this.getBook(ctx.args.get(1));
                    if (book != null) {
                        if (STEMCraft.featureEnabled("SMGeyser") && SMGeyser.isBedrockPlayer(targetPlayer)) {
                            if (STEMCraft.featureEnabled("SMItemAttribs")) {
                                SMItemAttribs.addAttrib(book, "destroy-on-drop", 1);
                            }

                            SMCommon.givePlayerItem(targetPlayer, book);

                            String title = getBookTitle(book);
                            SMMessenger.infoLocale(targetPlayer, "BOOK_GIVEN_WITH_TITLE", "title", title);
                        } else {
                            targetPlayer.openBook(book);
                        }
                    } else {
                        ctx.returnErrorLocale("BOOK_NOT_FOUND");
                    }

                    // Sub command - del
                } else if ("del".equals(sub)) {
                    ctx.checkPermission("stemcraft.book.edit");
                    ctx.checkArgsLocale(2, "BOOK_USAGE_DEL");

                    try {
                        PreparedStatement selectStatement = SMDatabase.prepareStatement(
                            "SELECT COUNT(*) FROM books WHERE name = ?");
                        selectStatement.setString(1, ctx.args.get(1));
                        ResultSet selectResult = selectStatement.executeQuery();
                        selectResult.next();

                        int rowCount = selectResult.getInt(1);

                        PreparedStatement statement;
                        if (rowCount > 0) {
                            statement = SMDatabase.prepareStatement(
                                "DELETE FROM books WHERE name = ?");

                            statement.setString(1, ctx.args.get(1));
                            statement.executeUpdate();
                            ctx.returnSuccessLocale("BOOK_DELETE_SUCCESSFUL");
                        } else {
                            ctx.returnErrorLocale("BOOK_NOT_FOUND");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    // Sub command - unlock
                } else if ("unlock".equals(sub)) {
                    ctx.checkNotConsole();
                    ctx.checkPermission("stemcraft.book.edit");

                    ItemStack item = ctx.player.getInventory().getItemInMainHand();
                    if (item.getType() == Material.WRITTEN_BOOK) {
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
                        ctx.player.getInventory().setItemInMainHand(book);
                        ctx.returnSuccessLocale("BOOK_UNLOCK_SUCCESSFUL");
                    } else {
                        ctx.returnErrorLocale("BOOK_UNLOCK_NOT_BOOK");
                    }
                } else {
                    ctx.returnErrorLocale("BOOK_UNKNOWN_OPTION");
                }

                this.buildCacheList();
            })
            .register();

        return true;
    }

    /**
     * Get Book item from database.
     * 
     * @param name
     * @return
     */
    private ItemStack getBook(String name) {
        try {
            PreparedStatement statement = SMDatabase.prepareStatement(
                "SELECT * FROM books WHERE name = ? LIMIT 1");
            statement.setString(1, name);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String author = resultSet.getString("author");
                String title = resultSet.getString("title");
                String content = resultSet.getString("content");

                ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
                BookMeta meta = (BookMeta) book.getItemMeta();

                meta.setAuthor(author);
                meta.setTitle(title);
                meta.setPages(content.split("<n>"));
                book.setItemMeta(meta);

                return book;
            }
        } catch (Exception e) {
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

    /**
     * Build a book name cache list.
     */
    private void buildCacheList() {
        try {
            PreparedStatement statement = SMDatabase.prepareStatement(
                "SELECT name FROM books");
            ResultSet resultSet = statement.executeQuery();

            this.cacheList.clear();
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                this.cacheList.add(name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Untranslate the Minecraft color char to & symbol.
     * 
     * @param input
     * @return
     */
    public String untranslateColorCodes(String input) {
        return input.replaceAll(ChatColor.COLOR_CHAR + "([0-9a-fk-or])", "&$1");
    }

    /**
     * Present a book to a player.
     * 
     * @param player
     * @param name
     */
    public void showBook(Player player, String name) {
        ItemStack book = this.getBook(name);
        if (book != null) {
            if (STEMCraft.featureEnabled("SMGeyser") && SMGeyser.isBedrockPlayer(player)) {
                if (STEMCraft.featureEnabled("SMItemAttribs")) {
                    SMItemAttribs.addAttrib(book, "destroy-on-drop", 1);
                }

                if (SMCommon.givePlayerItem(player, book)) {
                    String title = getBookTitle(book);
                    SMMessenger.infoLocale(player, "BOOK_GIVEN_WITH_TITLE", "title", title);
                }
            } else {
                player.openBook(book);
            }
        } else {
            SMMessenger.errorLocale(player, "BOOK_NOT_FOUND");
        }
    }

    /**
     * Check a book exists.
     * 
     * @param name
     * @return
     */
    public Boolean bookExists(String name) {
        try {
            PreparedStatement statement = SMDatabase.prepareStatement(
                "SELECT * FROM books WHERE name = ? LIMIT 1");
            statement.setString(1, name);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Get a Books title or the no title locale.
     * 
     * @param item The book item.
     * @return The book title.
     */
    public static String getBookTitle(ItemStack item) {
        if (item.getType() == Material.WRITTEN_BOOK || item.getType() == Material.WRITABLE_BOOK) {
            BookMeta bookMeta = (BookMeta) item.getItemMeta();

            if (bookMeta != null && bookMeta.hasTitle()) {
                return bookMeta.getTitle();
            }
        }

        return SMLocale.get("BOOK_NO_TITLE");
    }
}
