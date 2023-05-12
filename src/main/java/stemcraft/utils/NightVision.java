package stemcraft.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class NightVision extends JavaPlugin {

    JavaPlugin plugin;

    private static final String PERMISSION_NIGHTVISION = "stemcraft.nightvision";
    private static final PotionEffect NIGHTVISION_EFFECT = new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false);

    public NightVision(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by players.");
            return true;
        }

        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("nightvision") || command.getName().equalsIgnoreCase("nv")) {
            if (player.hasPermission(PERMISSION_NIGHTVISION)) {
                toggleNightVision(player);
                return true;
            } else {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
        }

        return false;
    }

    private void toggleNightVision(Player player) {
        if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            player.sendMessage(ChatColor.YELLOW + "Night Vision disabled.");
        } else {
            player.addPotionEffect(NIGHTVISION_EFFECT);
            player.sendMessage(ChatColor.YELLOW + "Night Vision enabled.");
        }
    }
}
