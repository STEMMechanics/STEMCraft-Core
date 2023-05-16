package stemcraft.components;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import stemcraft.objects.SMComponent;

public class DropPlayerHeads extends SMComponent {

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
        Player killer = player.getKiller();

        // Check if the killer is a player and not in survival gamemode
        if (killer instanceof Player && killer.getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        if (player.getGameMode() == GameMode.SURVIVAL) {
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
            skullMeta.setOwningPlayer(player);
            playerHead.setItemMeta(skullMeta);

            event.getDrops().add(playerHead);
        }
    }
}
