package stemcraft.objects;

import java.lang.reflect.Field;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.jetbrains.annotations.NotNull;

/**
 * The base class for components in the SM plugin. Components provide specific functionality and can register events.
 */
public class SMCommand extends BukkitCommand {
    /**
     * The linked component for this command.
     */
    SMComponent component = null;

    /**
     * Class Constructor.
     * 
     * @param permission
     * @param name
     * @param description
     * @param usage
     * @param aliases
     */
    public SMCommand(@NotNull String permission, @NotNull String name, @NotNull String description,
            @NotNull String usage, @NotNull List<String> aliases, SMComponent component) {
        super(name, description, usage, aliases);
        this.setName(name);
        this.setDescription(description);
        this.setUsage(usage);
        this.setAliases(aliases);
        this.setPermission(permission);
        this.component = component;
        try {
            Field f;
            f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            f.setAccessible(true);
            CommandMap commandMap = (CommandMap) f.get(Bukkit.getServer());
            commandMap.register("stemcraft", this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean execute(@NotNull CommandSender commandSender, @NotNull String name, @NotNull String[] args) {
        if (this.getName().equals(name) || this.getAliases().contains(name)) {
            component.onCommand(commandSender, this.getName(), args);
        }

        return true;
    }
}
