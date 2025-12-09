package de.themoep.versionconnector.listeners;

import de.themoep.versionconnector.VersionConnector;
import de.themoep.versionconnector.routing.ConnectorInfo;
import de.themoep.versionconnector.routing.ServerRouter;
import de.themoep.versionconnector.protocols.ProtocolVersion;
import de.themoep.versionconnector.config.ConnectorConfigService;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Map;

public class PlayerConnectListener implements Listener {

    private final VersionConnector plugin;
    private final ConnectorConfigService configService;
    private final ServerRouter serverRouter;

    public PlayerConnectListener(VersionConnector plugin,
                                 ConnectorConfigService configService,
                                 ServerRouter serverRouter) {
        this.plugin = plugin;
        this.configService = configService;
        this.serverRouter = serverRouter;
    }

    @EventHandler
    public void onPlayerConnect(ServerConnectEvent event) {
        if (!plugin.isEnabled() || event.isCancelled()) {
            return;
        }

        ProxiedPlayer player = event.getPlayer();
        int version = plugin.getVersion(player);
        boolean isForge = plugin.isForge(player);
        Map<String, String> modList = player.getModList();

        plugin.logDebug(player.getName() + "'s version: " + version
                + " (" + ProtocolVersion.getVersion(version) + ")/forge: " + isForge
                + "/mods: " + modList.size() + "/join: " + (player.getServer() == null));

        ConnectorInfo connectorInfo = null;
        if (player.getServer() == null) {
            connectorInfo = configService.getJoinConnectorMap().get(event.getTarget().getName().toLowerCase());
        }
        if (connectorInfo == null) {
            connectorInfo = configService.getConnectorMap().get(event.getTarget().getName().toLowerCase());
        }

        if (connectorInfo != null) {
            ServerInfo targetServer = serverRouter.selectServer(
                    connectorInfo,
                    event.getTarget(),
                    version,
                    isForge,
                    modList,
                    configService.getStartBalancing()
            );
            if (targetServer != null) {
                event.setTarget(targetServer);
            }
        } else {
            plugin.logDebug("Server " + event.getTarget().getName() + " does not have any special connection info set");
        }
    }
}

