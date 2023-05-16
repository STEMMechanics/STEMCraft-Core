package stemcraft.components;

import org.bukkit.entity.Enderman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import stemcraft.objects.SMComponent;

public class PreventEndermanPickups extends SMComponent {
    /**
     * Called when the Component is enabled.
     */
    public Boolean onEnable() {
        registerEvents();
        return true;
    }

    /**
     * When an entity changes a block.
     * 
     * @param event Information about the event.
     */
    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {

        // Prevent Enderman picking up blocks
        if (event.getEntity() instanceof Enderman) {
            event.setCancelled(true);
        }
    }
}
