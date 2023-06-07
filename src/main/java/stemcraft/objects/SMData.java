package stemcraft.objects;

import java.util.HashMap;
import java.util.Map;

public class SMData {
    private final Map<String, String> data;

    public SMData() {
        this.data = new HashMap<>();
    }

    public void put(String key, String value) {
        data.put(key, value);
    }

    public String get(String key) {
        return data.get(key);
    }

    public boolean containsKey(String key) {
        return data.containsKey(key);
    }
}
