package ru.obvilion.utils;

import arc.Events;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.game.Teams;
import mindustry.gen.*;
import mindustry.world.blocks.storage.CoreBlock;

import ru.obvilion.config.Config;
import ru.obvilion.config.Lang;
import ru.obvilion.effects.EffectHelper;
import ru.obvilion.events.CoreCaptureEvent;
import ru.obvilion.events.EventsHelper;

public class Loader {
    public static boolean firstInit = true;

    public static void init() {
        Config.init();
        Lang.init();

        EventsHelper.init();
        EffectHelper.init();

        GameUtil.init();

        if (firstInit) {
            initEvents();
            firstInit = false;
        }
    }

    public static void initEvents() {
        Vars.netServer.assigner = (player, players) -> TeamHelper.checkTeam(player);

        Events.on(EventType.WorldLoadEvent.class, playEvent -> {
            final int[] index = {0};
            Team.blue.cores().copy().forEach(coreBuild -> {
                Call.removeTile(coreBuild.tile);
                Call.setTile(coreBuild.tile, coreBuild.block, TeamHelper.teams[index[0]], 0);

                index[0]++;
            });

            for (Teams.TeamData team : Vars.state.teams.active) {
                for (CoreBlock.CoreBuild core : team.cores) {
                    core.health = Float.POSITIVE_INFINITY;
                }
            }
        });

        Events.on(CoreCaptureEvent.class, event -> {
            if (Vars.state.isPaused()) return;

            final Unit player = event.unit;
            final CoreBlock.CoreBuild core = event.core;

            int id = -1;
            if (!CoreCaptureEvent.cores.isEmpty()) {
                id = CoreCaptureEvent.cores.indexOf(core);
            }

            float multiplier = 0.001f;
            if (!player.type.weapons.isEmpty()) {
                multiplier = player.type.weapons.first().bullet.damage * 0.003f * Config.getFloat("multiplier");
            }

            float x = core.x / 8 - (float) core.block.size / 2 - 0.5f;
            float y = core.y / 8 - (float) core.block.size / 2 - 0.5f;

            if (id == -1) {
                CoreCaptureEvent.statuses.add(event.unit.team.name);
                CoreCaptureEvent.cores.add(core);
                CoreCaptureEvent.health.add(0f);

                CoreCaptureEvent.x.add(x);
                CoreCaptureEvent.y.add(y);
                id = CoreCaptureEvent.cores.indexOf(core);
            }

            final int finalId = id;
            final Teams.TeamData data = Vars.state.teams.active.find(teamData -> teamData.team.name.equals(CoreCaptureEvent.statuses.get(finalId)));
            if (data == null) return;

            if (!event.ok) {
                if (!CoreCaptureEvent.statuses.get(id).equals(event.unit.team.name)) {
                    CoreCaptureEvent.health.set(id, CoreCaptureEvent.health.get(id) - multiplier);
                } else {
                    CoreCaptureEvent.health.set(id, CoreCaptureEvent.health.get(id) + multiplier);
                }

                if (CoreCaptureEvent.health.get(id) > 100f) {
                    if (core.team == Team.sharded && event.unit.team.name.equals(CoreCaptureEvent.statuses.get(id))) {
                        CoreCaptureEvent.remove(core);
                        Call.removeTile(core.tile);

                        Call.setTile(core.tile, core.block, event.unit.team, 0);
                        core.health = Float.POSITIVE_INFINITY;

                        Call.sendMessage(Lang.get("captured", Lang.get("team." + event.unit.team.name), event.unit.team.color.toString()));
                    } else {
                        CoreCaptureEvent.remove(core);
                        Call.removeTile(core.tile);

                        Call.setTile(core.tile, core.block, Team.all[1], 0);
                        core.health = Float.POSITIVE_INFINITY;

                        Call.sendMessage(Lang.get("recaptured", Lang.get("team." + core.team.name), core.team.color.toString()));

                        if (core.team().cores().size == 0) {
                            Call.sendMessage(Lang.get("loss", Lang.get("team." + core.team.name), core.team.color.toString()));

                            core.team().data().units.forEach(unit -> {
                                if (!unit.isPlayer()) return;

                                TeamHelper.updatePlayerTeam(unit.getPlayer());
                            });
                        }
                    }

                    return;
                }

                final float pointX = x + core.block.size + 1f;
                final float pointY = y + core.block.size + 1f;
                final float fPointX = x;
                final float fPointY = y;

                x = CoreCaptureEvent.x.get(id);
                y = CoreCaptureEvent.y.get(id);

                if (x < pointX && y >= pointY) {
                    x += 0.15f;
                } else if (x > fPointX && y > fPointY) {
                    y -= 0.15f;
                } else if (x > fPointX && y <= fPointY) {
                    x -= 0.15f;
                } else if (x < pointX && y < pointY) {
                    y += 0.15f;
                }

                CoreCaptureEvent.x.set(id, x);
                CoreCaptureEvent.y.set(id, y);

                EffectHelper.on("onCapture", x, y);
            } else {
                if (CoreCaptureEvent.health.get(id) > 0f) {
                    CoreCaptureEvent.health.set(id, CoreCaptureEvent.health.get(id) - multiplier);
                }
            }

            if (CoreCaptureEvent.health.get(id) < 0f) {
                CoreCaptureEvent.health.set(id, 0f);
                CoreCaptureEvent.statuses.set(id, event.unit.team.name);
            } else if (CoreCaptureEvent.health.get(id) == 0f && !CoreCaptureEvent.statuses.get(id).equals(event.unit.team.name)) {
                CoreCaptureEvent.statuses.set(id, event.unit.team.name);
            }
        });
    }
}
