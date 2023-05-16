package stemcraft.components;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import stemcraft.objects.SMComponent;

public class ConsolePlayerDeathLocation extends SMComponent {

    /**
     * Called when the Component is enabled.
     */
    public Boolean onEnable() {
        registerEvents();
        return true;
    }

    /**
     * When a player dies.
     * 
     * @param event Information about the event.
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location deathLocation = player.getLocation();

        String worldName = deathLocation.getWorld().getName();
        int x = deathLocation.getBlockX();
        int y = deathLocation.getBlockY();
        int z = deathLocation.getBlockZ();

        smPlugin.getLogger().info(player.getName() + " died at " + x + ", " + y + ", " + z + ", " + worldName);
    }
}
