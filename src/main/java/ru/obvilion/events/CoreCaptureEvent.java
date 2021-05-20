package ru.obvilion.events;

import arc.Events;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.entities.Units;
import mindustry.game.Teams;
import mindustry.gen.Call;
import mindustry.gen.Unit;
import mindustry.world.blocks.storage.CoreBlock.CoreBuild;
import ru.obvilion.config.Config;

public class CoreCaptureEvent {
    public static Seq<CoreBuild> cores = new Seq<>();
    public static Seq<String> statuses = new Seq<>();
    public static Seq<Float> x = new Seq<>();
    public static Seq<Float> y = new Seq<>();
    public static Seq<Float> health = new Seq<>();

    public final Unit unit;
    public final CoreBuild core;
    public final boolean ok;

    public CoreCaptureEvent(Unit unit, CoreBuild build, boolean ok) {
        this.unit = unit;
        this.core = build;
        this.ok = ok;
    }

    public static void update() {
        int distance = Config.getInt("distance");

        Seq<Teams.TeamData> teams = Vars.state.teams.present;
        for (Teams.TeamData team : teams) {
            for (CoreBuild core : team.cores) {
                for (Teams.TeamData anotherTeam : teams) {
                    Units.nearby(anotherTeam.team, core.x - distance, core.y - distance, 2 * distance, 2 * distance, u -> {
                        Events.fire(new CoreCaptureEvent(u, core, anotherTeam.team == team.team));
                    });
                }

                int id = CoreCaptureEvent.cores.indexOf(core);
                if (id != -1) {
                    if (CoreCaptureEvent.health.get(id) > 0f)
                    CoreCaptureEvent.health.set(id, CoreCaptureEvent.health.get(id) - 0.001f);

                    if (CoreCaptureEvent.health.get(id) < 0f) {
                        CoreCaptureEvent.health.set(id, 0f);
                    }

                    final Teams.TeamData data = Vars.state.teams.active.find(teamData -> teamData.team.name.equals(CoreCaptureEvent.statuses.get(id)));
                    if (data == null) CoreCaptureEvent.statuses.set(id, team.team.name);

                    final String message = data == null
                            ? "[#" + team.team.color.toString() + "]" + round(CoreCaptureEvent.health.get(id), 2) + "%"
                            : "[#" + data.team.color.toString() + "]" + round(CoreCaptureEvent.health.get(id), 2) + "%";

                    Call.label(
                            message,
                            0.10f, core.x, core.y
                    );
                }
            }
        }
    }

    public static double round(double value, int scale) {
        return Math.round(value * Math.pow(10, scale)) / Math.pow(10, scale);
    }

    public static void remove(CoreBuild core) {
        final int id = cores.indexOf(core);
        if (id == -1) return;

        cores.remove(id);
        x.remove(id);
        y.remove(id);
        health.remove(id);
        statuses.remove(id);
    }
}


