package ru.obvilion.effects;

import arc.files.Fi;
import arc.graphics.Color;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.io.PropertiesUtils;
import mindustry.content.Fx;
import mindustry.entities.Effect;

import ru.obvilion.Recapture;
import ru.obvilion.utils.ResourceUtil;
import java.lang.reflect.Field;

public class EffectHelper {
    public static Seq<EffectObject> effects;
    public static ObjectMap<String, String> properties;

    public static void init() {
        effects = new Seq<>();

        final Fi effect = Recapture.pluginDir.child("effects.properties");
        if (!effect.exists()) ResourceUtil.copy("effects.properties", effect);

        properties = new ObjectMap<>();
        PropertiesUtils.load(
            properties, Recapture.pluginDir.child("effects.properties").reader()
        );
    }

    public static void on(String key, float x, float y) {
        final String name = properties.get(key + ".name", "none");
        if (name.equals("none")) return;

        final String color = properties.get(key + ".color", "#ffffff");
        final int rotation = Integer.parseInt(
            properties.get(key + ".rotation", "0")
        );

        final Effect eff = getEffect(name);
        final EffectObject place = new EffectObject(eff, x, y, rotation, Color.valueOf(color));

        place.draw();
    }

    public static Effect getEffect(String name) {
        Effect eff = null;
        for (Field field : Fx.class.getFields()) {
            try {
                final String effName = field.getName();
                if (!effName.equals(name)) continue;

                eff = (Effect) field.get(null);
            } catch (Exception ignored) {

            }
        }

        return eff;
    }
}
