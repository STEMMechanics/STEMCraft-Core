package stemcraft.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.GameMode;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import stemcraft.objects.SMComponent;

public class NoDespawnOnDeath extends SMComponent {

    /**
     * Called when the Component is enabled.
     */
    public Boolean onEnable() {
        registerEvents();
        return true;
    }

    /**
     * When an player dies.
     * 
     * @param event Information about the event.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (player.getGameMode() == GameMode.SURVIVAL) {
            final List<ItemStack> drops = new ArrayList<>(event.getDrops());

            event.getDrops().clear();

            // Schedule the delayed drop
            smPlugin.getServer().getScheduler().runTaskLater(smPlugin, () -> {
                // Drop the preserved items at the death location
                for (ItemStack item : drops) {
                    if (item != null) {
                        Item droppedItem = player.getWorld().dropItemNaturally(player.getLocation(), item);
                        droppedItem.setPickupDelay(Integer.MAX_VALUE);
                    }
                }
            }, 1L);
        }
    }
}
