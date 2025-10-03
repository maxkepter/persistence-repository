package com.example.persistence_repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads properties from META-INF/application.properties once and provides typed
 * accessors.
 */
public final class PropertyLoader {
    private static final String DEFAULT_LOCATION = "/application.properties"; // on classpath
    private static final Properties PROPERTIES = new Properties();
    private static volatile boolean initialized = false;

    private PropertyLoader() {
    }

    private static void init() {
        if (initialized)
            return;
        synchronized (PROPERTIES) {
            if (initialized)
                return;
            try (InputStream in = PropertyLoader.class.getResourceAsStream(DEFAULT_LOCATION)) {
                if (in != null) {
                    PROPERTIES.load(in);
                } else {
                    System.err.println("[PropertyLoader] application.properties not found at " + DEFAULT_LOCATION);
                }
            } catch (IOException e) {
                System.err.println("[PropertyLoader] Failed to load properties: " + e.getMessage());
            }
            initialized = true;
        }
    }

    public static String get(String key) {
        init();
        return PROPERTIES.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        String val = get(key);
        return val != null ? val : defaultValue;
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String val = get(key);
        if (val == null)
            return defaultValue;
        return val.equalsIgnoreCase("true") || val.equalsIgnoreCase("yes") || val.equals("1");
    }

    public static int getInt(String key, int defaultValue) {
        String val = get(key);
        if (val == null)
            return defaultValue;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static Properties all() {
        init();
        Properties copy = new Properties();
        copy.putAll(PROPERTIES);
        return copy;
    }
}
