/**
 * Component: NightVision
 * 
 * This component gives the player unlimited Night Vision when using the /nightvision command.
 */
package stemcraft.components;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import stemcraft.objects.SMComponent;
import java.util.ArrayList;
// import stemcraft.utils.SMTabCompletion;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NightVision extends SMComponent {

    /**
     * The permission required to use the command.
     */
    private static final String PERMISSION_NIGHTVISION = "stemcraft.nightvision";

    /**
     * The night vision potion effect to apply.
     */
    private static final PotionEffect NIGHTVISION_EFFECT =
            new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false);

    /**
     * Run when the component is enabled.
     * 
     * @return boolean if the component enabled successfully.
     */
    public Boolean onEnable() {
        ArrayList<String> aliases = new ArrayList<>();

        aliases.add("nightvision");
        aliases.add("nv");
        String usage = "/nightvision";
        String description = "Toggles nightvision";
        String permission = NightVision.PERMISSION_NIGHTVISION;

        registerCommand(aliases, permission, description, usage);
        return true;
    }

    public Boolean onCommand(CommandSender sender, String name, String[] args) {
        if (!this.commandOnlyPlayers(sender)) {
            return true;
        }

        if (name.equalsIgnoreCase("nightvision")) {
            if (this.commandRequiresPermission(sender, PERMISSION_NIGHTVISION)) {
                if (args.length == 0) {
                    toggleNightVision((Player) sender);
                } else if (args.length == 1) {
                    String option = args[0].toLowerCase();
                    if (option.equals("on")) {
                        enableNightVision((Player) sender);
                    } else if (option.equals("off")) {
                        disableNightVision((Player) sender);
                    } else if (option.equals("toggle")) {
                        toggleNightVision((Player) sender);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Invalid option. Available options: on, off, toggle");
                    }
                } else if (args.length == 2 && (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("off")
                        || args[0].equalsIgnoreCase("toggle"))) {
                    Player targetPlayer = Bukkit.getPlayer(args[1]);
                    if (targetPlayer != null) {
                        toggleNightVision(targetPlayer);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Invalid player name.");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED
                            + "Invalid command usage. Available options: /nightvision [on/off/toggle] [playerName]");
                }
            }
        }

        return true;
    }

    // @Override
    // public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
    // if (command.getName().equalsIgnoreCase("nightvision") || command.getName().equalsIgnoreCase("nv")) {
    // List<String[]> options = Arrays.asList(new String[] {"on", "_*onlineplayers"},
    // new String[] {"off", "_*onlineplayers"}, new String[] {"toggle", "_*onlineplayers"});

    // return SMTabCompletion.generateTabCompletions(sender, command, label, args, options);
    // }

    // return Collections.emptyList();
    // }

    private void enableNightVision(Player player) {
        player.addPotionEffect(NIGHTVISION_EFFECT);
        player.sendMessage(ChatColor.YELLOW + "Night Vision enabled.");
    }

    private void disableNightVision(Player player) {
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        player.sendMessage(ChatColor.YELLOW + "Night Vision disabled.");
    }

    private void toggleNightVision(Player player) {
        if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
            disableNightVision(player);
        } else {
            enableNightVision(player);
        }
    }
}
