package stemcraft.objects;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import stemcraft.STEMCraft;

public class SMPlayer {
    private STEMCraft sm;
    private Player player;
    private File dataFile;
    private Boolean dirty;

    private YamlConfiguration data;

    private List<String> nonPersistentKeys;

    public SMPlayer(STEMCraft sm, Player player) {
        this.sm = sm;
        this.player = player;
        this.nonPersistentKeys = new ArrayList<>();
        this.loadData();
        this.dirty = false;
    }

    public void loadData() {
        this.dataFile = new File(sm.getDataFolder() + "/player_data", this.player.getUniqueId() + ".yml");
        this.data = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void saveData() {
        if (this.dirty) {
            // Create a copy of the original data for saving
            YamlConfiguration dataToSave = new YamlConfiguration();

            for (String key : this.data.getKeys(true)) {
                if (!this.data.contains(key)) {
                    dataToSave.set(key, this.data.get(key));
                }
            }

            try {
                dataToSave.save(dataFile);
                this.dirty = false;
            } catch (IOException e) {
                sm.getLogger().warning("Failed to save player data for player " + this.player.getUniqueId());
            }
        }
    }

    public Boolean hasData(String key) {
        return this.data.contains(key);
    }

    public Boolean isPersistent(String key) {
        return this.nonPersistentKeys.contains(key);
    }

    public Object getData(String key, Object defaultValue) {
        if (this.data.contains(key)) {
            return this.data.get(key);
        }
        return defaultValue;
    }

    public void setData(String key, Object value, boolean persistent) {
        if (persistent) {
            if (!this.dirty && (!this.data.contains(key) || !this.data.get(key).equals(value))) {
                this.dirty = true;
            }
        } else if (!this.nonPersistentKeys.contains(key)) {
            this.nonPersistentKeys.add(key);
        }

        this.data.set(key, value);
    }

    public void deleteData(String key) {
        if (this.data.contains(key)) {
            this.data.set(key, null);

            if (!this.nonPersistentKeys.contains(key)) {
                this.dirty = true;
            } else {
                this.nonPersistentKeys.remove(key);
            }
        }
    }
}
