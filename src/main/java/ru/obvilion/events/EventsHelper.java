package ru.obvilion.events;

import arc.Events;
import arc.util.Timer;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.gen.Player;

public class EventsHelper {
    private static Timer.Task task;

    public static void init() {
        if (task != null) {
            task.cancel();
        } else {
            Events.run(EventType.Trigger.update, EventsHelper::tick);
        }

        task = Timer.schedule(CoreCaptureEvent::update, 0, 0.1f);
    }

    public static void tick() {
        for(Player player : Groups.player) {
            if (PlayerMoveEvent.check(player)) {
                final int oldX = PlayerMoveEvent.getPlayerX(player);
                final int oldY = PlayerMoveEvent.getPlayerY(player);

                Events.fire(new PlayerMoveEvent(oldX, oldY, player));
            }
        }

        PlayerMoveEvent.update();
    }
}
