package stemcraft.components;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemStack;
import java.util.Random;
import stemcraft.objects.SMComponent;

public class ReduceNetheriteChance extends SMComponent {
    private final double DEFAULT_NETHERITE_CHANCE = 0.25;
    private final Random random = new Random();

    private double netheriteChance = 0.25;

    public Boolean onEnable() {
        loadConfig();
        registerEvents();

        return true;
    }

    public void onReload() {
        loadConfig();
    }

    private void loadConfig() {
        netheriteChance = smConfig.getDouble(componentName + ".chance", DEFAULT_NETHERITE_CHANCE);
    }

    @EventHandler
    public void onFurnaceSmelt(FurnaceSmeltEvent event) {
        ItemStack sourceItem = event.getSource();
        ItemStack resultItem = event.getResult();

        if (sourceItem.getType() == Material.ANCIENT_DEBRIS && resultItem.getType() == Material.NETHERITE_SCRAP) {
            event.setCancelled(true); // Cancel the default Ancient Debris smelting

            if (random.nextDouble() <= netheriteChance) {
                event.setResult(new ItemStack(Material.NETHERITE_SCRAP, 1));
            } else {
                event.setResult(new ItemStack(Material.COAL, 1));
            }
        }
    }
}
