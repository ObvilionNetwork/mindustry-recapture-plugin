package ru.obvilion;

import arc.files.Fi;
import arc.util.Log;
import arc.util.CommandHandler;
import mindustry.Vars;
import mindustry.core.NetClient;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.mod.Plugin;

import ru.obvilion.config.Lang;
import ru.obvilion.utils.Loader;

public class Recapture extends Plugin {
    public static final Fi pluginDir = new Fi("./config/mods/ObvilionRecapture");
    public static final String VERSION = "0.1";

    @Override
    public void init() {
        Loader.init();
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {

    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.register("recapture", "[args...]", "ObvilionRecapture settings", arg -> {
            if (arg.length == 0) {
                Log.info("Too few command arguments. Usage:");
                Log.info("> recaprure version - Show version ObvilionRecapture plugin.");
                Log.info("> recaprure reload - Reload all ObvilionRecapture config files");
                return;
            }

            if (arg[0].equals("version")) {
                Log.info("ObvilionRecapture v@ by Fatonn", VERSION);
                Log.info("> Github link: https://github.com/ObvilionNetwork/mindustry-hub-plugin");
                Log.info("> Discord server link: https://discord.gg/cg82mjh");
                return;
            }

            if (arg[0].equals("reload")) {
                Loader.init();
                Log.info("Plugin ObvilionRecapture reloaded!");
                return;
            }

            Log.info("Command not found!");
        });
    }
}
