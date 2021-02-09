package ru.obvilion.events;

import arc.util.Timer;

public class EventsHelper {
    private static Timer.Task task;

    public static void init() {
        if (task != null) {
            task.cancel();
        }

        task = Timer.schedule(CoreCaptureEvent::update, 0, 0.1f);
    }
}
