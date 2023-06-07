package stemcraft.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import stemcraft.STEMCraft;

/**
 * The base class for components in the SM plugin. Components provide specific functionality and can register events.
 */
public class SMComponent implements Listener, TabCompleter {
    protected STEMCraft smPlugin = null;
    protected FileConfiguration smConfig = null;
    protected Boolean enabled = false;

    // This will be set to the components name based on the class name. The subclass can override it directly.
    protected String componentName = "";

    /**
     * Initializes the component with the specified STEMCraft instance.
     *
     * @param sm The STEMCraft instance to associate with the component.
     * @return Boolean If initialization was successful.
     */
    public SMComponent initialize(STEMCraft smPlugin) {
        this.smPlugin = smPlugin;
        this.smConfig = smPlugin.getConfig();

        if (this.componentName.equals("")) {
            this.componentName = this.getClass().getSimpleName();
        }

        return this;
    }

    /**
     * Enable the component.
     * 
     * @return Boolean If the component was enabled
     */
    public final Boolean enable() {
        if (this.enabled) {
            return true;
        }

        this.enabled = this.onEnable();
        if (this.enabled == true) {
            registerEvents();
        }
        return this.enabled;
    }

    /**
     * Disable the component.
     */
    public final void disable() {
        if (!this.enabled) {
            this.onDisable();
            this.enabled = false;
        }
    }

    /**
     * Register Events.
     */
    protected void registerEvents() {
        smPlugin.getServer().getPluginManager().registerEvents(this, smPlugin);
    }

    /**
     * Register Command.
     * 
     * @param command
     * @param description
     * @param usage
     * @param permission
     */
    protected void registerCommand(ArrayList<String> aliases, String description, String usage, String permission) {
        smPlugin.registerCommand(aliases, description, usage, permission, this);
    }

    protected void registerCommand(String command, String description, String usage, String permission) {
        smPlugin.registerCommand(command, description, usage, permission, this);
    }

    /**
     * Return if the component is enabled.
     * 
     * @return Boolean If the component is enabled.
     */
    public final Boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Called when the component is enabled. Subclasses can override this method to perform custom logic on enable.
     */
    public Boolean onEnable() {
        // Empty method, to be overridden by subclasses

        this.enabled = true;
        return this.enabled;
    }

    /**
     * Called when the component is disabled. Subclasses can override this method to perform custom logic on disable.
     */
    public void onDisable() {
        // Empty method, to be overridden by subclasses

        this.enabled = false;
    }

    /**
     * Called when the component is reloaded. Subclasses can override this method to perform custom logic on reload.
     */
    public void onReload() {
        // Empty method, to be overridden by subclasses
    }

    public Boolean onCommand(CommandSender sender, String name, String[] args) {
        // Empty method, to be overridden by subclasses

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();
    }

    /**
     * Check if the command sender is a player, if not, return an error message.
     * 
     * @param sender The command sender.
     * @return Boolean If the check was successful.
     */
    public boolean commandOnlyPlayers(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by players.");
            return false;
        }

        return true;
    }

    /**
     * Check if the command sender has a permission, if not, return an error message.
     * 
     * @param sender The command sender.
     * @return Boolean If the check was successful.
     */
    public boolean commandRequiresPermission(CommandSender sender, String permission) {
        if (sender instanceof Player && !sender.hasPermission(permission)) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return false;
        }

        return true;
    }
}
