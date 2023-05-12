import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class EndRegeneration extends JavaPlugin implements Listener {

    private static final int REGENERATION_DELAY = 4 * 60 * 60 * 20; // 4 hours in ticks

    private boolean isDragonAlive;
    private long dragonDeathTime;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Cleanup resources, if necessary
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof EnderDragon) {
            isDragonAlive = false;
            dragonDeathTime = System.currentTimeMillis();
            scheduleEndRegeneration();
        }
    }

    private void regenerateEnd() {
        World world = Bukkit.getWorld("world_the_end");
        if (world == null) {
            getLogger().warning("The End world is not loaded or does not exist.");
            return;
        }

        // Regenerate the End Island using WorldEdit API
        File schematicFile = new File(getDataFolder(), "end_island.schematic");
        if (!schematicFile.exists()) {
            getLogger().warning("Schematic file 'end_island.schematic' not found in plugin folder.");
            return;
        }

        try (FileInputStream fis = new FileInputStream(schematicFile)) {
            Clipboard clipboard = ClipboardFormats.findByFile(schematicFile).load(fis);
            Extent extent = BukkitAdapter.adapt(world);
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(extent)
                    .to(BlockVector3.at(0, 75, 0))
                    .ignoreAirBlocks(true)
                    .build();
            Operations.complete(operation);
        } catch (IOException e) {
            getLogger().warning("Failed to regenerate End Island: " + e.getMessage());
            return;
        }

        // Respawn the Ender Dragon
        EnderDragon dragon = world.spawn(0, 75, 0, EnderDragon.class);
        dragon.setAI(true);
        dragon.setHealth(dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());

        isDragonAlive = true;
        dragonDeathTime = System.currentTimeMillis();
        scheduleEndRegeneration();

        getLogger().info("The End Island has been regenerated, and the Ender Dragon has respawned.");
    }

    private void scheduleEndRegeneration() {
        Bukkit.getScheduler().runTaskLater(this, this::regenerateEnd, REGENERATION_DELAY);
    }
}
