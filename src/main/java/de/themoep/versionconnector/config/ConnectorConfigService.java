package de.themoep.versionconnector.config;

import de.themoep.bungeeplugin.FileConfiguration;
import de.themoep.versionconnector.routing.ConnectorInfo;
import de.themoep.versionconnector.VersionConnector;
import de.themoep.versionconnector.protocols.ProtocolVersion;
import lombok.Getter;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.protocol.ProtocolConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;

/**
 * Handles loading and exposing configuration-backed data structures.
 */
@Getter
public class ConnectorConfigService {

    private final VersionConnector plugin;

    private FileConfiguration fileConfiguration;
    private Configuration configuration;
    private boolean debug;
    private int startBalancing;

    private Map<String, ConnectorInfo> joinConnectorMap = new HashMap<>();
    private Map<String, ConnectorInfo> connectorMap = new HashMap<>();

    public ConnectorConfigService(VersionConnector plugin) {
        this.plugin = plugin;
    }

    public boolean reload() {
        try {
            fileConfiguration = new FileConfiguration(plugin, "config.yml");
            configuration = fileConfiguration.getConfiguration();
            debug = configuration.getBoolean("debug", true);
            startBalancing = configuration.getInt("start-balancing", 0);

            joinConnectorMap = new HashMap<>();
            connectorMap = new HashMap<>();

            if (configuration.contains("join")) {
                Configuration joinSection = configuration.getSection("join");
                for (String key : joinSection.getKeys()) {
                    ConnectorInfo connectorInfo = loadConnectorInfo(
                            joinConnectorMap,
                            loadVersionMap(joinSection.getSection(key + ".versions")),
                            loadVersionMap(joinSection.getSection(key + ".forge")),
                            loadModsMap(joinSection.getSection(key + ".mods"))
                    );
                    if (plugin.getProxy().getServerInfo(key) != null) {
                        joinConnectorMap.put(key.toLowerCase(), connectorInfo);
                    }
                }
            }

            loadConnectorInfo(
                    connectorMap,
                    loadVersionMap(configuration.getSection("versions")),
                    loadVersionMap(configuration.getSection("forge")),
                    loadModsMap(configuration.getSection("mods"))
            );

            if (configuration.contains("servers")) {
                Configuration serversSection = configuration.getSection("servers");
                for (String key : serversSection.getKeys()) {
                    ConnectorInfo connectorInfo = loadConnectorInfo(
                            connectorMap,
                            loadVersionMap(serversSection.getSection(key + ".versions")),
                            loadVersionMap(serversSection.getSection(key + ".forge")),
                            loadModsMap(serversSection.getSection(key + ".mods"))
                    );
                    if (plugin.getProxy().getServerInfo(key) != null) {
                        connectorMap.put(key.toLowerCase(), connectorInfo);
                    }
                }
            }

            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Can't load plugin config!", e);
        }
        return false;
    }

    public Configuration getConfig() {
        return configuration;
    }

    private ConnectorInfo loadConnectorInfo(Map<String, ConnectorInfo> map,
                                            SortedMap<Integer, List<ServerInfo>> versions,
                                            SortedMap<Integer, List<ServerInfo>> forge,
                                            Map<String[], List<ServerInfo>> mods) {
        ConnectorInfo connectorInfo = new ConnectorInfo(versions, forge, mods);
        for (ServerInfo server : connectorInfo.getServers()) {
            map.putIfAbsent(server.getName().toLowerCase(), connectorInfo);
        }
        return connectorInfo;
    }

    private SortedMap<Integer, List<ServerInfo>> loadVersionMap(Configuration section) {
        SortedMap<Integer, List<ServerInfo>> map = new TreeMap<>();
        if (section == null) {
            return map;
        }
        for (String versionStr : section.getKeys()) {
            int rawVersion;
            try {
                rawVersion = Integer.parseInt(versionStr);
            } catch (NumberFormatException e2) {
                String getVersion = versionStr.toUpperCase().replace('.', '_');
                if (!getVersion.startsWith("MINECRAFT_")) {
                    getVersion = "MINECRAFT_" + getVersion;
                }
                try {
                    rawVersion = ProtocolVersion.valueOf(getVersion).toInt();
                } catch (IllegalArgumentException e1) {
                    try {
                        rawVersion = (int) ProtocolConstants.class.getField(getVersion).get(null);
                    } catch (IllegalAccessException | NoSuchFieldException e) {
                        plugin.getLogger().warning(versionStr + " is neither a valid Integer nor a string representation of a major protocol version?");
                        continue;
                    }
                }
            }
            String serverStr = section.getString(versionStr, null);
            if (serverStr != null && !serverStr.isEmpty()) {
                String[] serverNames = serverStr.split(",");
                List<ServerInfo> serverList = new ArrayList<>();
                for (String serverName : serverNames) {
                    ServerInfo server = plugin.getProxy().getServerInfo(serverName.trim());
                    if (server != null) {
                        serverList.add(server);
                    } else {
                        plugin.getLogger().warning(serverStr + " is defined for version " + rawVersion + "/" + versionStr + " but there is no server with that name?");
                    }
                }
                if (!serverList.isEmpty()) {
                    map.put(rawVersion, serverList);
                }
            }
        }
        return map;
    }

    private Map<String[], List<ServerInfo>> loadModsMap(Configuration section) {
        Map<String[], List<ServerInfo>> map = new LinkedHashMap<>();
        if (section == null) {
            return map;
        }
        for (String modsStr : section.getKeys()) {
            String[] mods = modsStr.split(",");
            String serverStr = section.getString(modsStr, null);
            if (serverStr != null && !serverStr.isEmpty()) {
                String[] serverNames = serverStr.split(",");
                List<ServerInfo> serverList = new ArrayList<>();
                for (String serverName : serverNames) {
                    ServerInfo server = plugin.getProxy().getServerInfo(serverName.trim());
                    if (server != null) {
                        serverList.add(server);
                    } else {
                        plugin.getLogger().warning(serverStr + " is defined for mods " + modsStr + " but there is no server with that name?");
                    }
                }
                if (!serverList.isEmpty()) {
                    map.put(mods, serverList);
                }
            }
        }
        return map;
    }
}

