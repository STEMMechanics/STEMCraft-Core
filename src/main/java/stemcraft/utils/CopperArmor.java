import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class CopperArmorPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Register copper armor recipes
        registerCopperArmorRecipes();
    }

    @Override
    public void onDisable() {
        // Cleanup resources, if necessary
    }

    private void registerCopperArmorRecipes() {
        // Helmet Recipe
        ItemStack copperHelmet = createCopperArmorPiece("Copper Helmet", Color.ORANGE);
        ShapedRecipe helmetRecipe = new ShapedRecipe(copperHelmet);
        helmetRecipe.shape("###", "# #");
        helmetRecipe.setIngredient('#', Material.COPPER_INGOT);
        getServer().addRecipe(helmetRecipe);

        // Chestplate Recipe
        ItemStack copperChestplate = createCopperArmorPiece("Copper Chestplate", Color.ORANGE);
        ShapedRecipe chestplateRecipe = new ShapedRecipe(copperChestplate);
        chestplateRecipe.shape("# #", "###", "###");
        chestplateRecipe.setIngredient('#', Material.COPPER_INGOT);
        getServer().addRecipe(chestplateRecipe);

        // Leggings Recipe
        ItemStack copperLeggings = createCopperArmorPiece("Copper Leggings", Color.ORANGE);
        ShapedRecipe leggingsRecipe = new ShapedRecipe(copperLeggings);
        leggingsRecipe.shape("###", "# #", "# #");
        leggingsRecipe.setIngredient('#', Material.COPPER_INGOT);
        getServer().addRecipe(leggingsRecipe);

        // Boots Recipe
        ItemStack copperBoots = createCopperArmorPiece("Copper Boots", Color.ORANGE);
        ShapedRecipe bootsRecipe = new ShapedRecipe(copperBoots);
        bootsRecipe.shape("# #", "# #");
        bootsRecipe.setIngredient('#', Material.COPPER_INGOT);
        getServer().addRecipe(bootsRecipe);
    }

    private ItemStack createCopperArmorPiece(String name, Color color) {
        ItemStack itemStack = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta armorMeta = (LeatherArmorMeta) itemStack.getItemMeta();
        armorMeta.setDisplayName(name);
        armorMeta.setColor(color);
        itemStack.setItemMeta(armorMeta);
        return itemStack;
    }
}
