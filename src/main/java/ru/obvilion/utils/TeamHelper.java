package ru.obvilion.utils;

import arc.Events;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;

import java.util.concurrent.atomic.AtomicReference;

public class TeamHelper {
    public static Team[] teams = {Team.blue, Team.green, Team.purple, Team.all[252], Team.all[63]};

    public static void updatePlayerTeam(Player player) {
        final Team team = checkTeam(player);

        if (countTeams() > 1) {
            player.team(team);
            Call.setPlayerTeamEditor(player, team);
            player.unit().health = 0f;
        } else {
            Events.fire(new EventType.GameOverEvent(team));
        }
    }

    public static int countTeams() {
        return Vars.state.teams.active.size;
    }

    public static Team checkTeam(Player player) {
        final int[] minPlayers = {10000};

        AtomicReference<Team> result = new AtomicReference<>(null);
        Vars.state.teams.active.forEach(teamData -> {
            final Team team = teamData.team;
            if (team == Team.crux || team == Team.sharded) return;

            Seq<Player> players = Groups.player.copy(new Seq<>()).filter(pl -> pl.team() == team);

            if (player.team() == team) {
                if (players.size - 1 < minPlayers[0]) {
                    minPlayers[0] = players.size;
                    result.set(team);
                }

                return;
            }

            if (players.size < minPlayers[0]) {
                minPlayers[0] = players.size;
                result.set(team);
            }
        });

        return result.get();
    }
}
