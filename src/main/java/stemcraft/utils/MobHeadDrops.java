package stemcraft.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class MobHeadDrops extends JavaPlugin implements Listener {
    private JavaPlugin plugin;

    public MobHeadDrops(JavaPlugin plugin) {
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player) {
            Player player = event.getEntity().getKiller();
            EntityType entityType = event.getEntityType();
            
            if (entityType != EntityType.PLAYER) {
                // Drop mob head
                ItemStack mobHead = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta skullMeta = (SkullMeta) mobHead.getItemMeta();
                skullMeta.setOwningPlayer(player);
                skullMeta.setDisplayName(entityType.getKey().getKey() + " Head");
                mobHead.setItemMeta(skullMeta);
                
                event.getDrops().add(mobHead);
            }
        }
    }
}
