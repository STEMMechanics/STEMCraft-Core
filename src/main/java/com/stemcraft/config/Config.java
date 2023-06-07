package com.stemcraft.config;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.util.*;

public class Config {
    private static final String COMMENT_PREFIX = "# ";
    private static final String ARRAY_INDICATOR = "[]";

    private File configFile;
    private Map<String, Object> configData;
    private Map<String, String> headers;

    public Config(File configFile) {
        this.configFile = configFile;
        this.configData = loadConfigData();
        this.headers = new HashMap<>();
    }

    public void addMissingOptions() {
        Map<String, Object> defaultValues = getDefaultValues();
        for (String key : defaultValues.keySet()) {
            if (!configData.containsKey(key)) {
                configData.put(key, defaultValues.get(key));
            }
        }
    }

    public void saveConfigData() {
        try {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Representer representer = new CustomRepresenter();

            Yaml yaml = new Yaml(representer, options);
            String configString = yaml.dump(configData);

            BufferedWriter writer = new BufferedWriter(new FileWriter(configFile));
            writer.write(configString);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getString(String key) {
        Object value = getValue(key);
        return (value != null) ? value.toString() : null;
    }

    public void setString(String key, String value) {
        setValue(key, value);
    }

    public Integer getInteger(String key) {
        Object value = getValue(key);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return null;
    }

    public void setInteger(String key, int value) {
        setValue(key, value);
    }

    public Double getDouble(String key) {
        Object value = getValue(key);
        if (value instanceof Double) {
            return (Double) value;
        }
        return null;
    }

    public void setDouble(String key, double value) {
        setValue(key, value);
    }

    public Boolean getBoolean(String key) {
        Object value = getValue(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return null;
    }

    public void setBoolean(String key, boolean value) {
        setValue(key, value);
    }

    public List<Object> getList(String key) {
        Object value = getValue(key);
        if (value instanceof List) {
            return (List<Object>) value;
        }
        return null;
    }

    public void setList(String key, List<Object> value) {
        setValue(key, value);
    }

    public boolean exists(String key) {
        return configData.containsKey(key);
    }

    public void delete(String key) {
        configData.remove(key);
    }

    private Object getValue(String key) {
        String[] path = key.split("\\.");
        Map<String, Object> current = configData;

        for (int i = 0; i < path.length - 1; i++) {
            Object value = current.get(path[i]);
            if (value instanceof Map) {
                current = (Map<String, Object>) value;
            } else {
                return null;
            }
        }

        return current.get(path[path.length - 1]);
    }

    private void setValue(String key, Object value) {
        String[] path = key.split("\\.");
        Map<String, Object> current = configData;

        for (int i = 0; i < path.length - 1; i++) {
            String pathSegment = path[i];
            if (!current.containsKey(pathSegment)) {
                current.put(pathSegment, new HashMap<>());
            }
            Object currentValue = current.get(pathSegment);
            if (currentValue instanceof Map) {
                current = (Map<String, Object>) currentValue;
            } else {
                throw new IllegalArgumentException("Invalid path: " + key);
            }
        }

        current.put(path[path.length - 1], value);
    }

    private Map<String, Object> loadConfigData() {
        Map<String, Object> loadedData = new LinkedHashMap<>();

        if (configFile.exists()) {
            try {
                Yaml yaml = new Yaml();
                InputStream inputStream = new FileInputStream(configFile);
                loadedData = yaml.load(inputStream);
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return loadedData;
    }

    protected Map<String, Object> getDefaultValues() {
        return new HashMap<>();  // Override this method in subclasses to provide default values
    }

    public void setHeader(String key, String comment) {
        headers.put(key, comment);
    }

    private class CustomRepresenter extends Representer {
        @Override
        protected Tag getTag(Class<?> clazz) {
            if (List.class.isAssignableFrom(clazz)) {
                return Tag.SEQ;
            }
            return super.getTag(clazz);
        }

        @Override
        protected void representScalar(Tag tag, String value, Character style) {
            if (style == null && tag != Tag.NULL) {
                if (value.startsWith(COMMENT_PREFIX)) {
                    String comment = value.substring(COMMENT_PREFIX.length());
                    representScalar(Tag.COMMENT, comment, style);
                } else {
                    if (value.endsWith(ARRAY_INDICATOR)) {
                        value = "[" + value.substring(0, value.length() - ARRAY_INDICATOR.length()) + "]";
                        super.representScalar(Tag.FLOW, value, style);
                    } else {
                        super.representScalar(tag, value, style);
                    }
                }
            } else {
                super.representScalar(tag, value, style);
            }
        }

        @Override
        protected void representMapping(Tag tag, Map<?, ?> mapping, Boolean flowStyle) {
            List<Object> merged = new ArrayList<>();
            for (Map.Entry<?, ?> entry : mapping.entrySet()) {
                String key = entry.getKey().toString();
                String header = headers.get(key);
                if (header != null) {
                    merged.add(COMMENT_PREFIX + header);
                }
                merged.add(entry.getKey());
                merged.add(entry.getValue());
            }
            super.representMapping(tag, merged.toArray(), flowStyle);
        }
    }
}
