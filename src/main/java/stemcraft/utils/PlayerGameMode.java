package stemcraft.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerGameMode extends JavaPlugin {

    JavaPlugin plugin;

    private static final String PERMISSION_CHANGE_SELF = "stemcraft.gamemode.change.self";
    private static final String PERMISSION_CHANGE_OTHERS = "stemcraft.gamemode.change.others";

    public PlayerGameMode(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by players.");
            return true;
        }

        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("gmc")) {
            if (hasPermission(player, PERMISSION_CHANGE_SELF)) {
                setGameMode(player, GameMode.CREATIVE);
                return true;
            }
        } else if (command.getName().equalsIgnoreCase("gms")) {
            if (hasPermission(player, PERMISSION_CHANGE_SELF)) {
                setGameMode(player, GameMode.SURVIVAL);
                return true;
            }
        } else if (command.getName().equalsIgnoreCase("gmsp")) {
            if (hasPermission(player, PERMISSION_CHANGE_SELF)) {
                setGameMode(player, GameMode.SPECTATOR);
                return true;
            }
        } else if (command.getName().equalsIgnoreCase("gma")) {
            if (hasPermission(player, PERMISSION_CHANGE_SELF)) {
                setGameMode(player, GameMode.ADVENTURE);
                return true;
            }
        }

        if (command.getName().equalsIgnoreCase("gamemode")) {
            if (args.length > 0) {
                if (hasPermission(player, PERMISSION_CHANGE_OTHERS)) {
                    Player targetPlayer = Bukkit.getPlayer(args[0]);

                    if (targetPlayer != null) {
                        GameMode gameMode = parseGameMode(args[1]);

                        if (gameMode != null) {
                            setGameMode(targetPlayer, gameMode);
                            player.sendMessage(ChatColor.YELLOW + "Changed " + targetPlayer.getName() + "'s game mode to " + gameMode.name());
                        } else {
                            player.sendMessage(ChatColor.RED + "Invalid game mode specified.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "Player not found.");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to change other players' game mode.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "Usage: /gamemode <player> <gamemode>");
            }
            return true;
        }

        return false;
    }

    private boolean hasPermission(Player player, String permission) {
        return player.hasPermission(permission) || player.hasPermission(permission + ".other");
    }

    private void setGameMode(Player player, GameMode gameMode) {
        player.setGameMode(gameMode);
        player.sendMessage(ChatColor.YELLOW + "Changed game mode to " + gameMode.name());
    }

    private GameMode parseGameMode(String arg) {
        if (arg.equalsIgnoreCase("0") || arg.equalsIgnoreCase("s") || arg.equalsIgnoreCase("survival")) {
            return GameMode.SURVIVAL;
        } else if (arg.equalsIgnoreCase("1") || arg.equalsIgnoreCase("c") || arg.equalsIgnoreCase("creative")) {
            return GameMode.CREATIVE;
        } else if (arg.equalsIgnoreCase("2") || arg.equalsIgnoreCase("a") || arg.equalsIgnoreCase("adventure")) {
            return GameMode.ADVENTURE;
        } else if (arg.equalsIgnoreCase("3") || arg.equalsIgnoreCase("sp") || arg.equalsIgnoreCase("spectator")) {
            return GameMode.SPECTATOR;
        }
        return null;
    }
}
