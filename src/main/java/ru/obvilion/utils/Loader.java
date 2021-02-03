package ru.obvilion.utils;

import arc.Events;
import arc.graphics.Colors;
import arc.util.Log;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.game.Teams;
import mindustry.gen.*;

import mindustry.world.Block;
import mindustry.world.blocks.storage.CoreBlock;
import ru.obvilion.config.Config;
import ru.obvilion.config.Lang;
import ru.obvilion.effects.EffectHelper;
import ru.obvilion.events.CoreCaptureEvent;
import ru.obvilion.events.EventsHelper;
import ru.obvilion.events.PlayerMoveEvent;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
        Events.on(EventType.ServerLoadEvent.class, event -> {
            new Thread(() -> {
                final DiscordWebhook webhook = new DiscordWebhook(Config.get("discord.webhook"));
                webhook.setAvatarUrl(Config.get("discord.webhook.avatar"));
                webhook.setUsername(Config.get("discord.webhook.name"));
                webhook.addEmbed(new DiscordWebhook.EmbedObject()
                        .setTitle(Lang.get("onServerStart"))
                        .setColor(new Color(92, 137, 214)));
                try {
                    webhook.execute();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } finally {
                    Thread.currentThread().interrupt();
                }
            }).start();
        });

        Events.on(EventType.PlayerJoin.class, event -> {
            final Player player = event.player;
            TeamHelper.updatePlayerTeam(player);

            player.sendMessage(Lang.get("motd"));
            Call.sendMessage(Lang.get("onJoin", player.name));

            EffectHelper.onJoin(player);

            new Thread(() -> {
                final DiscordWebhook webhook = new DiscordWebhook(Config.get("discord.webhook"));
                webhook.setAvatarUrl(Config.get("discord.webhook.avatar"));
                webhook.setUsername(fixName(player.name));
                webhook.addEmbed(new DiscordWebhook.EmbedObject()
                        .setTitle(Lang.get("discord.onJoin"))
                        .setColor(new Color(110, 237, 139)));
                try {
                    webhook.execute();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } finally {
                    Thread.currentThread().interrupt();
                }
            }).start();
        });

        Events.on(EventType.WorldLoadEvent.class, playEvent -> {
            final int[] index = {0};

            Team.blue.cores().copy().forEach(coreBuild -> {
                Call.removeTile(coreBuild.tile);
                Call.setTile(coreBuild.tile, coreBuild.block, TeamHelper.teams[index[0]], 0);

                index[0]++;
            });
        });

        Events.on(EventType.PlayEvent.class, playEvent -> {
            Groups.player.forEach(TeamHelper::updatePlayerTeam);
        });

        Events.on(EventType.PlayerLeave.class, event -> {
            final Player player = event.player;

            EffectHelper.onLeave(player);
            PlayerMoveEvent.remove(player);

            Call.sendMessage(Lang.get("onLeave", player.name));

            new Thread(() -> {
                final DiscordWebhook webhook = new DiscordWebhook(Config.get("discord.webhook"));
                webhook.setAvatarUrl(Config.get("discord.webhook.avatar"));
                webhook.setUsername(fixName(player.name));
                webhook.addEmbed(new DiscordWebhook.EmbedObject()
                        .setTitle(Lang.get("discord.onLeave"))
                        .setColor(new Color(214, 92, 92)));
                try {
                    webhook.execute();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } finally {
                    Thread.currentThread().interrupt();
                }
            }).start();
        });

        Events.on(PlayerMoveEvent.class, event -> {
            final Player player = event.player;

            EffectHelper.onMove(player);
        });

        Events.on(CoreCaptureEvent.class, event -> {
            if (Vars.state.isPaused()) return;

            final Unit player = event.unit;
            final CoreBlock.CoreBuild core = event.core;

            int id = CoreCaptureEvent.cores.indexOf(core);
            final float multiplier = player.hitSize * 0.0015f;

            float x = core.x / 8 - (float) core.block.size / 2 - 0.5f;
            float y = core.y / 8 - (float) core.block.size / 2 - 0.5f;

            if (id == -1) {
                CoreCaptureEvent.statuses.add(event.unit.team.name);
                CoreCaptureEvent.cores.add(core);
                CoreCaptureEvent.health.add(0f);

                CoreCaptureEvent.x.add(x);
                CoreCaptureEvent.y.add(y);
                id = 0;
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
                        Call.sendMessage(Lang.get("captured", Lang.get("team." + event.unit.team.name), event.unit.team.color.toString()));
                    } else {
                        CoreCaptureEvent.remove(core);
                        Call.removeTile(core.tile);

                        Call.setTile(core.tile, core.block, Team.all[1], 0);
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

                EffectHelper.on("onMove", x, y);
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

        Vars.netServer.admins.addChatFilter((player, text) -> {
            Call.sendMessage(Lang.get("message", player.color.toString(), player.name, text), null, player);

            new Thread(() -> {
                final DiscordWebhook webhook = new DiscordWebhook(Config.get("discord.webhook"));
                webhook.setAvatarUrl(Config.get("discord.webhook.avatar"));
                webhook.setUsername(fixName(player.name));
                webhook.setContent(fixName(text).replaceAll("@", ""));
                try {
                    webhook.execute();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } finally {
                    Thread.currentThread().interrupt();
                }
            }).start();

            return null;
        });
    }

    static String fixName(String name){
        name = name.trim();
        if(name.equals("[") || name.equals("]")){
            return "";
        }

        for(int i = 0; i < name.length(); i++){
            if(name.charAt(i) == '[' && i != name.length() - 1 && name.charAt(i + 1) != '[' && (i == 0 || name.charAt(i - 1) != '[')){
                String prev = name.substring(0, i);
                String next = name.substring(i);
                String result = checkColor(next);

                name = prev + result;
            }
        }

        return name;
    }

    static String checkColor(String str){
        for(int i = 1; i < str.length(); i++){
            if(str.charAt(i) == ']'){
                String color = str.substring(1, i);

                if(Colors.get(color.toUpperCase()) != null || Colors.get(color.toLowerCase()) != null){
                    arc.graphics.Color result = (Colors.get(color.toLowerCase()) == null ? Colors.get(color.toUpperCase()) : Colors.get(color.toLowerCase()));
                    if(result.a <= 0.8f){
                        return str.substring(i + 1);
                    }
                }else{
                    try{
                        arc.graphics.Color result = arc.graphics.Color.valueOf(color);
                        if(result.a <= 0.8f){
                            return str.substring(i + 1);
                        }
                    }catch(Exception e){
                        return str;
                    }
                }
            }
        }
        return str;
    }
}
