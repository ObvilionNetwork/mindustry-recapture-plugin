package ru.obvilion.events;

import arc.Events;
import arc.util.Timer;
import mindustry.content.Blocks;
import mindustry.game.EventType;

import static mindustry.Vars.state;

public class EventsHelper {
    private static Timer.Task task;

    public static void init() {
        if (task != null) {
            task.cancel();
        }

        task = Timer.schedule(CoreCaptureEvent::update, 0, 0.1f);

        Events.on(EventType.PlayEvent.class, e -> {
            state.rules.canGameOver = false;
            state.rules.modeName = "Recapture";
            state.rules.pvp = false;

            Blocks.coreFoundation.health = Integer.MAX_VALUE;
            Blocks.coreNucleus.health = Integer.MAX_VALUE;
            Blocks.coreShard.health = Integer.MAX_VALUE;
        });
    }
}
