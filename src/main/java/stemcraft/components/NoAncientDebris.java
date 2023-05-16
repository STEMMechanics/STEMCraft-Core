package stemcraft.components;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.comphenix.protocol.wrappers.codecs.WrappedCodec;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import com.comphenix.protocol.PacketType.Play.Server;

import org.bukkit.plugin.java.JavaPlugin;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import stemcraft.objects.SMComponent;

public class NoAncientDebris extends SMComponent {
    /**
     * ProtocolManager API
     */
    private ProtocolManager protocolManager;

    /**
     * Called when the Component is enabled.
     */
    @Override
    public Boolean onEnable() {
        // Check if ProtocolLib is available
        if (smPlugin.getServer().getPluginManager().getPlugin("ProtocolLib") == null) {
            smPlugin.getLogger().severe("ProtocolLib not found! Disabling component.");
            return false;
        }

        // Initialize ProtocolManager
        protocolManager = ProtocolLibrary.getProtocolManager();

        // Register PacketAdapter to handle block changes
        protocolManager.addPacketListener(
                new PacketAdapter(this.smPlugin, ListenerPriority.NORMAL, PacketType.Play.Server.BLOCK_CHANGE) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        Player player = event.getPlayer();
                        Block block = event.getPacket().getBlockPositionModifier().readSafely(0)
                                .toLocation(player.getWorld()).getBlock();

                        smPlugin.getLogger().info(block.getType().toString());

                        // Check if the block is ancient debris and the player doesn't have permission
                        if (block.getType() == Material.ANCIENT_DEBRIS) {
                            // Change the block to Nether Gold Ore
                            block.setType(Material.NETHER_GOLD_ORE);
                        }
                    }
                });

        protocolManager.addPacketListener(
                new PacketAdapter(this.smPlugin, ListenerPriority.NORMAL, PacketType.Play.Server.MAP_CHUNK) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        Player player = event.getPlayer();
                        PacketContainer packet = event.getPacket();

                        int chunkX = packet.getIntegers().read(0);
                        int chunkZ = packet.getIntegers().read(1);

                        // Modify ANCIENT_DEBRIS blocks within the chunk
                        for (int x = 0; x < 16; x++) {
                            for (int y = 0; y < 256; y++) {
                                for (int z = 0; z < 16; z++) {
                                    BlockPosition blockPosition =
                                            new BlockPosition(chunkX * 16 + x, y, chunkZ * 16 + z);

                                    // Check if the block is ANCIENT_DEBRIS
                                    if (isAncientDebris(packet, blockPosition)) {
                                        // Change the block to NETHER_GOLD_ORE
                                        setBlockData(packet, blockPosition, Material.NETHER_GOLD_ORE.createBlockData());
                                    }
                                }
                            }
                        }
                    }
                });
        registerEvents();
        return true;
    }

    private boolean isAncientDebris(PacketContainer packet, BlockPosition blockPosition) {
        List<WrappedBlockData> blockDataList = packet.getBlockData().getValues();
        for (WrappedBlockData blockData : blockDataList) {
            BlockPosition currentBlockPosition = blockData.getType();

            getBlockPosition();
            if (currentBlockPosition.equals(blockPosition)) {
                Material blockMaterial = blockData.getType().getMaterial();
                return blockMaterial == Material.ANCIENT_DEBRIS;
            }
        }
        return false;
    }

    private void setBlockData(PacketContainer packet, BlockPosition blockPosition, WrappedBlockData blockData) {
        try {
            packet.getBlockData().write(blockPosition, blockData);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * When a block is broken.
     * 
     * @param event Information about the event.
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // Check if the block is ancient debris and the player doesn't have permission
        if (block.getType() == Material.ANCIENT_DEBRIS && !player.hasPermission("stemcraft.ancientdebris")) {
            // Modify drops for Nether Gold Ore
            event.setDropItems(false); // Disable default drops

            // Get player's tool and Fortune level
            ItemStack tool = player.getInventory().getItemInMainHand();
            int fortuneLevel = tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);

            // Calculate number of gold nuggets to drop
            int baseDrop = ThreadLocalRandom.current().nextInt(2, 7); // Base drop of 2-6 nuggets
            int fortuneModifier = calculateFortuneModifier(fortuneLevel); // Fortune modifier based
                                                                          // on player's tool

            // Apply fortune modifier to the base drop
            int finalDrop = baseDrop + fortuneModifier;

            // Drop the gold nuggets
            if (finalDrop > 0) {
                block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.GOLD_NUGGET, finalDrop));
            }

            // Change block type to Nether Gold Ore
            block.setType(Material.NETHER_GOLD_ORE);
        }
    }

    /**
     * Calculate the fortune modifier based on the fortune level.
     * 
     * @param fortuneLevel The fortune level to use in the calculation.
     * @returns The fortune modifier.
     */
    private int calculateFortuneModifier(int fortuneLevel) {
        if (fortuneLevel <= 0) {
            return 0; // No Fortune enchantment
        }

        // Calculate Fortune modifier based on Fortune level
        double chanceMultiplier = 1.0;

        switch (fortuneLevel) {
            case 1:
                chanceMultiplier = 0.3333; // 33.3% chance to multiply drops by 2
                break;
            case 2:
                chanceMultiplier = 0.25; // 25% chance to multiply drops by 2 or 3
                break;
            case 3:
                chanceMultiplier = 0.2; // 20% chance to multiply drops by 2, 3, or 4
                break;
        }

        int fortuneModifier = 0;

        for (int i = 0; i < 3; i++) {
            if (Math.random() < chanceMultiplier) {
                fortuneModifier++;
            }
        }

        return fortuneModifier;
    }
}
