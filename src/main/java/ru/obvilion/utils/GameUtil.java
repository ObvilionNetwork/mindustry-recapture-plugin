package ru.obvilion.utils;

import arc.Events;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.game.Teams;
import mindustry.gen.Groups;
import mindustry.gen.Player;

public class GameUtil {
    private static Timer.Task task;
    private static Timer.Task taskLogic;

    public static void init() {
        if (task != null) task.cancel();
        if (taskLogic != null) taskLogic.cancel();

        task = Timer.schedule(GameUtil::updateUsers, 0, 0.1f);
        taskLogic = Timer.schedule(GameUtil::updateLogic, 0, 1.4f);
    }

    public static void updateLogic() {
        if (Vars.state.teams.active.size < 2) {
            if (Vars.state.teams.active.size == 1) {
                Events.fire(new EventType.GameOverEvent(Vars.state.teams.active.first().team));
            } else {
                Events.fire(new EventType.GameOverEvent(Team.crux));
            }
        }
    }

    public static void updateUsers() {
        for (Player pl : Groups.player) {
            if (pl.team() == Team.sharded || pl.team().cores().size == 0) {
                TeamHelper.updatePlayerTeam(pl);
            }
        }
    }
}
