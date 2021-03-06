package ru.obvilion.config;

import arc.files.Fi;
import arc.struct.ObjectMap;
import arc.util.Log;
import arc.util.io.PropertiesUtils;
import ru.obvilion.Recapture;
import ru.obvilion.utils.ResourceUtil;

public class Config {
    public static final String fileName = "config.properties";
    public static final Fi file = Recapture.pluginDir.child(fileName);

    private static ObjectMap<String, String> config;

    public static void init() {
        if (!file.exists()) {
            Recapture.pluginDir.mkdirs();
            ResourceUtil.copy(fileName, file);

            Log.info("The config file for ObvilionRecapture was successfully generated.");
            Log.info("Configure it in " + file.path());
        }

        config = new ObjectMap<>();
        PropertiesUtils.load(
            config, file.reader()
        );
    }

    public static String get(String key) {
        return config.get(key);
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public static float getFloat(String key) {
        return Float.parseFloat(get(key));
    }
}
