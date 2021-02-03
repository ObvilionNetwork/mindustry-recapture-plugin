package ru.obvilion.utils;

import arc.struct.Seq;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.game.Teams;
import mindustry.gen.Building;

public class GameUtil {
    private static Timer.Task task;

    public static void init() {
        if (task != null) task.cancel();

        task = Timer.schedule(GameUtil::updateCores, 0, 1f);
    }

    public static void updateCores() {
        Seq<Teams.TeamData> teams = Vars.state.teams.present;
        for (Teams.TeamData team : teams) {
            for (Building core : team.cores) {
                core.health = Float.POSITIVE_INFINITY;
            }
        }
    }
}
