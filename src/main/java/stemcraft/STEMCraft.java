package stemcraft;

import org.bukkit.plugin.java.JavaPlugin;
import stemcraft.utils.AFK;
import stemcraft.utils.AntiEndermanGrief;
import stemcraft.utils.CoordsHUD;
import stemcraft.utils.MobHeadDrops;
import stemcraft.utils.PlayerHeadDrops;

public class STEMCraft extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        new AFK(this);
        new AntiEndermanGrief(this);
        new CoordsHUD(this);
        new MobHeadDrops(this);
        new PlayerHeadDrops(this);
    }

    @Override
    public void onDisable() {
        /* empty */
    }
}