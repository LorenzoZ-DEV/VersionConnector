package de.themoep.versionconnector.routing;

import de.themoep.versionconnector.VersionConnector;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServerRouter {

    private final VersionConnector plugin;

    public ServerRouter(VersionConnector plugin) {
        this.plugin = plugin;
    }

    public ServerInfo selectServer(ConnectorInfo connectorInfo,
                                   ServerInfo targetServer,
                                   int version,
                                   boolean isForge,
                                   Map<String, String> modList,
                                   int startBalancing) {
        List<ServerInfo> serverList = connectorInfo.getServers(version, isForge, modList);
        if (serverList == null || serverList.isEmpty() || (startBalancing < 0 && serverList.contains(targetServer))) {
            plugin.logDebug("No servers found for " + targetServer.getName() + "/" + version + "/forge: " + isForge);
            return null;
        }

        ServerInfo selected = null;
        for (ServerInfo tested : serverList) {
            if (selected == null || (startBalancing > -1 && selected.getPlayers().size() >= startBalancing && tested.getPlayers().size() < selected.getPlayers().size())) {
                selected = tested;
            }
        }
        plugin.logDebug("Selected server " + (selected != null ? selected.getName() : "null") + " for "
                + targetServer.getName() + "/" + version + "/forge: " + isForge + "/mods: " + modList.size()
                + " from " + serverList.stream().map(ServerInfo::getName).collect(Collectors.joining(",")));
        return selected;
    }
}

