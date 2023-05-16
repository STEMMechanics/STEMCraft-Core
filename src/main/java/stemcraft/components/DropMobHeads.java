package stemcraft.components;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import stemcraft.objects.SMComponent;
import stemcraft.utilities.SMStringUtils;

public class DropMobHeads extends SMComponent {

    /**
     * Called when the Component is enabled.
     */
    public Boolean onEnable() {
        registerEvents();
        return true;
    }

    /**
     * When an entity dies.
     * 
     * @param event Information about the event.
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {

        // Drop mob heads if killed by a player
        if (event.getEntity().getKiller() instanceof Player) {
            Player player = event.getEntity().getKiller();
            if (player.getGameMode() == GameMode.SURVIVAL) {
                EntityType entityType = event.getEntityType();

                if (entityType != EntityType.PLAYER) {
                    ItemStack mobHead = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta skullMeta = (SkullMeta) mobHead.getItemMeta();
                    skullMeta.setOwner("MHF_" + entityType.toString());
                    skullMeta.setDisplayName(SMStringUtils.capitalize(entityType.getKey().getKey() + " Head"));
                    mobHead.setItemMeta(skullMeta);

                    event.getDrops().add(mobHead);
                }
            }
        }
    }
}
