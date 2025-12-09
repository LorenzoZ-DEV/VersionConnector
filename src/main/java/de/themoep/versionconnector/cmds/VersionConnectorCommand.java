package de.themoep.versionconnector.cmds;

import de.themoep.versionconnector.VersionConnector;
import de.themoep.versionconnector.protocols.ProtocolVersion;
import de.themoep.versionconnector.routing.ConnectorInfo;
import de.themoep.versionconnector.util.C;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.bungee.actor.BungeeCommandActor;
import revxrsal.commands.bungee.annotation.CommandPermission;

import java.util.*;
import java.util.stream.Collectors;

@Description("Mostra informazioni su VersionConnector e comandi correlati")
public class VersionConnectorCommand {

    private final VersionConnector plugin;
    private final String basePermission;

    public VersionConnectorCommand(VersionConnector plugin) {
        this.plugin = plugin;
        this.basePermission = plugin.getDescription().getName().toLowerCase() + ".command";
    }

    // /vc, /vercon, /versionconnector
    @Command({"vc", "vercon", "versionconnector"})
    @Description("Mostra informazioni sul plugin VersionConnector")
    public void main(BungeeCommandActor actor) {
        actor.reply(C.translate("&b" + plugin.getDescription().getName()
                + "&e version " + plugin.getDescription().getVersion()));
        actor.reply(C.translate("&bUsage: &e/vc check [<player>|-all]"));
        actor.reply(C.translate("&b       &e/vc config"));
        actor.reply(C.translate("&b       &e/vc reload"));
    }

    // /vc check (senza argomenti -> riepilogo versioni/mods online)
    @Command({"vc check", "vercon check", "versionconnector check"})
    @Description("Mostra le versioni dei client e i mod attivi dei giocatori online")
    @CommandPermission("%plugin%.command.check")
    public void checkAllSummary(BungeeCommandActor actor) {
        if (plugin.getProxy().getOnlineCount() == 0) {
            actor.reply(C.translate("&cNo players are online."));
            return;
        }

        Map<Integer, Integer> versionMap = new LinkedHashMap<>();
        Map<Integer, Integer> forgeMap = new LinkedHashMap<>();
        Map<String, Integer> modsMap = new LinkedHashMap<>();

        for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
            int version = plugin.getVersion(player);
            if (plugin.isForge(player)) {
                forgeMap.put(version, forgeMap.getOrDefault(version, 0) + 1);
            } else {
                versionMap.put(version, versionMap.getOrDefault(version, 0) + 1);
            }
            if (!player.getModList().isEmpty()) {
                String mods = player.getModList().keySet().stream()
                        .sorted(String::compareToIgnoreCase)
                        .collect(Collectors.joining(","));
                modsMap.put(mods, modsMap.getOrDefault(mods, 0) + 1);
            }
        }

        actor.reply(C.translate("&6Player versions:"));
        versionMap.entrySet().stream()
                .sorted(Collections.reverseOrder(Comparator.comparingInt(Map.Entry::getKey)))
                .forEach(e -> {
                    ProtocolVersion version = ProtocolVersion.getVersion(e.getKey());
                    if (version != ProtocolVersion.UNKNOWN) {
                        actor.reply(C.translate("&b" + version + "&e: &6" + e.getValue()));
                    } else {
                        actor.reply(C.translate("&b" + e.getKey() + "&e: &6" + e.getValue()));
                    }
                });

        if (!forgeMap.isEmpty()) {
            actor.reply(C.translate("&8Forge versions:"));
            forgeMap.entrySet().stream()
                    .sorted(Collections.reverseOrder(Comparator.comparingInt(Map.Entry::getKey)))
                    .forEach(e -> {
                        ProtocolVersion version = ProtocolVersion.getVersion(e.getKey());
                        if (version != ProtocolVersion.UNKNOWN) {
                            actor.reply(C.translate("&b" + version + "&e: &6" + e.getValue()));
                        } else {
                            actor.reply(C.translate("&b" + e.getKey() + "&e: &6" + e.getValue()));
                        }
                    });
        }

        if (!modsMap.isEmpty()) {
            actor.reply(C.translate("&cMods"));
            modsMap.entrySet().stream()
                    .sorted(Collections.reverseOrder(Comparator.comparingInt(Map.Entry::getValue)))
                    .forEach(e ->
                            actor.reply(C.translate("&b" + e.getKey() + "&e: &6" + e.getValue()))
                    );
        }
    }

    // /vc check -all
    @Command({"vc check -all", "vercon check -all", "versionconnector check -all"})
    @Description("Mostra le informazioni di connessione per tutti i giocatori online")
    @CommandPermission("%plugin%.command.check.all")
    public void checkAllPlayers(BungeeCommandActor actor) {
        List<ProxiedPlayer> players = new ArrayList<>(plugin.getProxy().getPlayers());
        players.sort(Collections.reverseOrder(Comparator.comparingInt(plugin::getVersion)));

        for (ProxiedPlayer player : players) {
            int rawVersion = plugin.getVersion(player);
            String msg = String.format(
                    "&b%s&e: %s/%d/forge: %s/mods: %s",
                    player.getName(),
                    ProtocolVersion.getVersion(rawVersion),
                    rawVersion,
                    plugin.isForge(player),
                    player.getModList().entrySet().stream()
                            .map(e -> e.getKey() + "(" + e.getValue() + ")")
                            .collect(Collectors.joining(", "))
            );
            actor.reply(C.translate(msg));
        }
    }

    // /vc check <player...>
    @Command({"vc check", "vercon check", "versionconnector check"})
    @Usage("<player> [altri giocatori...]")
    @Description("Mostra le informazioni di connessione dei giocatori specificati")
    @CommandPermission("%plugin%.command.check.other")
    public void checkSpecificPlayers(BungeeCommandActor actor, String... playerNames) {
        if (playerNames == null || playerNames.length == 0) {
            actor.error(C.translate("&cUtilizzo: /vc check <player> [altri giocatori...]"));
            return;
        }

        List<ProxiedPlayer> players = new ArrayList<>();

        for (String name : playerNames) {
            ProxiedPlayer player = plugin.getProxy().getPlayer(name);
            if (player != null) {
                players.add(player);
            } else {
                actor.reply(C.translate(name + " &cis not online."));
            }
        }

        players.sort(Collections.reverseOrder(Comparator.comparingInt(plugin::getVersion)));

        for (ProxiedPlayer player : players) {
            int rawVersion = plugin.getVersion(player);
            String msg = String.format(
                    "&b%s&e: %s/%d/forge: %s/mods: %s",
                    player.getName(),
                    ProtocolVersion.getVersion(rawVersion),
                    rawVersion,
                    plugin.isForge(player),
                    player.getModList().entrySet().stream()
                            .map(e -> e.getKey() + "(" + e.getValue() + ")")
                            .collect(Collectors.joining(", "))
            );
            actor.reply(C.translate(msg));
        }
    }

    @Command({"vc config", "vercon config", "versionconnector config"})
    @Description("Mostra la configurazione delle route di VersionConnector")
    @CommandPermission("%plugin%.command.config")
    public void config(BungeeCommandActor actor) {
        actor.reply(C.translate("&bDebug: &e" + plugin.isDebug()));
        actor.reply(C.translate("&bStarting balancing at &6" + plugin.getStartBalancing()));

        for (Map.Entry<String, ConnectorInfo> entry : plugin.getConnectorMap().entrySet()) {
            actor.reply(C.translate("&e" + entry.getKey() + " configuration:"));
            ConnectorInfo info = entry.getValue();

            if (info.getVanillaMap().isEmpty()) {
                actor.reply(C.translate("&b  No versions config."));
            } else {
                actor.reply(C.translate("&e  Versions:"));
                for (Map.Entry<Integer, List<ServerInfo>> versionEntry : info.getVanillaMap().entrySet()) {
                    ProtocolVersion protocolVersion = ProtocolVersion.getVersion(versionEntry.getKey());
                    String versionName = protocolVersion != ProtocolVersion.UNKNOWN
                            ? protocolVersion.toString()
                            : String.valueOf(versionEntry.getKey());
                    actor.reply(C.translate("&b    " + versionName + "&e: &6"
                            + versionEntry.getValue().stream()
                            .map(ServerInfo::getName)
                            .collect(Collectors.joining(", "))));
                }
            }

            if (info.getForgeMap().isEmpty()) {
                actor.reply(C.translate("&b  No forge config."));
            } else {
                actor.reply(C.translate("&e  Forge:"));
                for (Map.Entry<Integer, List<ServerInfo>> versionEntry : info.getForgeMap().entrySet()) {
                    ProtocolVersion protocolVersion = ProtocolVersion.getVersion(versionEntry.getKey());
                    String versionName = protocolVersion != ProtocolVersion.UNKNOWN
                            ? protocolVersion.toString()
                            : String.valueOf(versionEntry.getKey());
                    actor.reply(C.translate("&b    " + versionName + "&e: &6"
                            + versionEntry.getValue().stream()
                            .map(ServerInfo::getName)
                            .collect(Collectors.joining(", "))));
                }
            }

            if (info.getModMap().isEmpty()) {
                actor.reply(C.translate("&b  No mods config."));
            } else {
                actor.reply(C.translate("&e  Mods:"));
                for (Map.Entry<String[], List<ServerInfo>> modEntry : info.getModMap().entrySet()) {
                    actor.reply(C.translate("&b    " + String.join(",", modEntry.getKey()) + "&e: &6"
                            + modEntry.getValue().stream()
                            .map(ServerInfo::getName)
                            .collect(Collectors.joining(", "))));
                }
            }
        }
    }

    @Command({"vc reload", "vercon reload", "versionconnector reload"})
    @Description("Ricarica la configurazione di VersionConnector")
    @CommandPermission("%plugin%.command.reload")
    public void reload(BungeeCommandActor actor) {
        if (plugin.loadConfig()) {
            actor.reply(C.translate("&aConfig reloaded!"));
        } else {
            actor.error(C.translate("&cError occured while reloading the config! Take a look at the console log."));
        }
    }
}