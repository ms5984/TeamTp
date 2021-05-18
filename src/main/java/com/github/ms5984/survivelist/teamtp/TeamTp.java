/*
 * MIT License
 *
 * Copyright (c) 2021 Matt (ms5984) <https://github.com/ms5984>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.ms5984.survivelist.teamtp;

import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class TeamTp extends JavaPlugin {

    private YamlConfiguration locationConfig = new YamlConfiguration();
    private File locationConfFile;

    @Override
    public void onEnable() {
        // Plugin startup logic
        locationConfFile = new File(getDataFolder(), "teams.yml");
        if (locationConfFile.exists()) {
            locationConfig = YamlConfiguration.loadConfiguration(locationConfFile);
        }
        Item.SLAP_SALMON.setupItem(getConfig(), "slap-salmon");
        Optional.ofNullable(getCommand("setteamtp")).ifPresent(command -> {
            command.setUsage("Usage: /setteamtp <color>");
            command.setExecutor((sender, cmd, label, args) -> {
                if (sender instanceof Player) {
                    if (cmd.testPermission(sender)) {
                        if (args.length != 1) {
                            return false;
                        }
                        editTeams(conf -> conf.set(args[0].replaceAll("\\.", ""), ((Player) sender).getLocation()));
                    }
                    return true;
                }
                return false;
            });
            command.setTabCompleter((sender, cmd, alias, args) -> {
                if (args.length > 1) return Collections.emptyList();
                if (!cmd.testPermissionSilent(sender)) {
                    return Collections.emptyList();
                }
                return getLocationConfig().getKeys(false)
                        .stream()
                        .filter(s -> s.startsWith(args[0]))
                        .collect(Collectors.toCollection(LinkedList::new));
            });
        });
        Optional.ofNullable(getCommand("delteamtp")).ifPresent(command -> {
            command.setUsage("Usage: /delteamtp <color>");
            command.setExecutor((sender, cmd, label, args) -> {
                if (sender instanceof Player) {
                    if (cmd.testPermission(sender)) {
                        if (args.length != 1) {
                            return false;
                        }
                        editTeams(conf -> conf.set(args[0].replaceAll("\\.", ""), null));
                    }
                    return true;
                }
                return false;
            });
            command.setTabCompleter((sender, cmd, alias, args) -> {
                if (args.length > 1) return Collections.emptyList();
                if (!cmd.testPermissionSilent(sender)) {
                    return Collections.emptyList();
                }
                return getLocationConfig().getKeys(false)
                        .stream()
                        .filter(s -> s.startsWith(args[0]))
                        .collect(Collectors.toCollection(LinkedList::new));
            });
        });
        Optional.ofNullable(getCommand("teamtp")).ifPresent(command -> {
            command.setUsage("Usage: /teamtp <player> <color>");
            command.setExecutor((sender, cmd, label, args) -> {
                if (sender instanceof Player) {
                    if (cmd.testPermission(sender)) {
                        if (args.length != 2) {
                            return false;
                        }
                        try {
                            final Optional<? extends Player> any = getServer().getOnlinePlayers().stream()
                                    .filter(Entity::isValid)
                                    .filter(p -> p.getName().equals(args[0]))
                                    .findAny();
                            if (any.isPresent()) {
                                final Player player = any.get();
                                final Optional<String> locationKey = getLocationConfig().getKeys(false).stream()
                                        .filter(args[1]::equals)
                                        .findAny();
                                if (locationKey.isPresent()) {
                                    final Location location = getLocationConfig().getLocation(locationKey.get());
                                    if (location == null) throw new IllegalStateException("Unable to load location.");
                                    player.teleport(location);
                                    Item.SLAP_SALMON.give(player);
                                } else {
                                    sender.sendMessage("That location is not present.");
                                }
                            } else {
                                sender.sendMessage("Unable to find that player.");
                            }
                        } catch (IllegalStateException e) {
                            sender.sendMessage(e.getMessage());
                            getLogger().warning(e::getMessage);
                        }
                    }
                    return true;
                }
                return false;
            });
            command.setTabCompleter((sender, cmd, alias, args) -> {
                if (args.length > 2) return Collections.emptyList();
                if (!cmd.testPermissionSilent(sender)) {
                    return Collections.emptyList();
                }
                if (args.length <= 1) {
                    return null;
                }
                return getLocationConfig().getKeys(false)
                        .stream()
                        .filter(s -> s.startsWith(args[1]))
                        .collect(Collectors.toCollection(LinkedList::new));
            });
        });
        Optional.ofNullable(getCommand("teamtpreload")).ifPresent(command -> {
            command.setUsage("Usage: /teamtpreload");
            command.setExecutor((sender, cmd, label, args) -> {
                if (sender instanceof Player) {
                    if (cmd.testPermission(sender)) {
                        reloadConfig();
                        if (!locationConfFile.exists()) {
                            locationConfig = new YamlConfiguration();
                            try {
                                //noinspection ResultOfMethodCallIgnored
                                locationConfFile.createNewFile();
                            } catch (IOException e) {
                                final String message = "Error creating new teams.yml file. Check file i/o";
                                sender.sendMessage(message);
                                getLogger().warning(message);
                                getLogger().severe(e::getMessage);
                            }
                        }
                        try {
                            getLocationConfig().load(locationConfFile);
                        } catch (IOException | InvalidConfigurationException e) {
                            final String error_reloading_config = "Error reloading config";
                            sender.sendMessage(error_reloading_config);
                            getLogger().warning(error_reloading_config);
                            getLogger().severe(e::getMessage);
                        }
                    }
                    return true;
                }
                return false;
            });
            command.setTabCompleter((sender, cmd, alias, args) -> Collections.emptyList());
        });
    }

    synchronized private YamlConfiguration getLocationConfig() {
        return locationConfig;
    }

    private void editTeams(Consumer<YamlConfiguration> edits) throws IllegalStateException {
        edits.accept(getLocationConfig());
        try {
            getLocationConfig().save(locationConfFile);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
