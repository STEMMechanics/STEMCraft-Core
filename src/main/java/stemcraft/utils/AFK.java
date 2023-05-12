package stemcraft.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AFK implements Listener, CommandExecutor {

    private JavaPlugin plugin;
    private Map<UUID, Integer> afkTimers;
    private Map<UUID, Boolean> afkStatus;

    public AFK(JavaPlugin plugin) {
        this.plugin = plugin;
        afkTimers = new HashMap<>();
        afkStatus = new HashMap<>();

        int afk_timeout = plugin.getConfig().getInt("afk-timeout", 300); // Default timeout: 5 mins

        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.getCommand("afk").setExecutor(this);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getGameMode() == GameMode.SURVIVAL) {
                        UUID playerId = player.getUniqueId();

                        // Check if player is already AFK
                        if (afkStatus.getOrDefault(playerId, false)) {
                            int afkTime = afkTimers.getOrDefault(playerId, 0);
                            afkTime++;

                            if (afkTime >= afk_timeout) {
                                AFK.this.setPlayerAFK(player, true);
                            }

                            afkTimers.put(playerId, afkTime);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20, 20); // Run every second (20 ticks)
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Reset AFK timer and status
        afkTimers.put(playerId, 0);
        if(this.isPlayerAFK(player)) {
            this.setPlayerAFK(player, false);
        }
    }

    @EventHandler
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        afkTimers.put(playerId, 0);
        if (afkStatus.getOrDefault(playerId, false)) {
            this.setPlayerAFK(player, false, false);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Reset AFK status and timer
        afkStatus.put(playerId, false);
        afkTimers.put(playerId, 0);

        player.setPlayerListName(player.getName());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Remove AFK status and timer entries
        afkStatus.remove(playerId);
        afkTimers.remove(playerId);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return true;
        }

        Player player = (Player) sender;

        // Check permission
        if (!player.hasPermission("stemcraft.afk")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        // Check player gamemode
        if (player.getGameMode() != GameMode.SURVIVAL) {
            player.sendMessage(ChatColor.RED + "You require to be in SURVIVAL gamemode to use this command.");
            return true;
        }

        // Toggle AFK status
        this.setPlayerAFK(player, !this.isPlayerAFK(player), false);

        return true;
    }

    public void setPlayerAFK(Player player, boolean afk) {
        setPlayerAFK(player, afk, false);
    }

    public void setPlayerAFK(Player player, boolean afk, boolean silent) {
        String playerName = player.getName();

        afkStatus.put(player.getUniqueId(), afk);

        if(afk) {
            player.setPlayerListName(ChatColor.GRAY + ChatColor.ITALIC.toString() + player.getName());
        } else {
            player.setPlayerListName(player.getName());
        }

        if(!silent) {
            String statusMessage = afk ? "is now AFK." : "is no longer AFK.";
            Bukkit.broadcastMessage(ChatColor.GRAY + ChatColor.ITALIC.toString() + playerName + " " + statusMessage);
        }
    }

    public boolean isPlayerAFK(Player player) {
        UUID playerId = player.getUniqueId();
        return afkStatus.getOrDefault(playerId, false);
    }
}
