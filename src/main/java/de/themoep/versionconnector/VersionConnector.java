package de.themoep.versionconnector;

import de.themoep.versionconnector.cmds.VersionConnectorCommand;
import de.themoep.versionconnector.config.ConnectorConfigService;
import de.themoep.versionconnector.forge.ForgeDetector;
import de.themoep.versionconnector.listeners.ForgeBrandListener;
import de.themoep.versionconnector.listeners.ForgeDisconnectListener;
import de.themoep.versionconnector.listeners.PlayerConnectListener;
import de.themoep.versionconnector.routing.ConnectorInfo;
import de.themoep.versionconnector.routing.ServerRouter;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import revxrsal.commands.Lamp;
import revxrsal.commands.bungee.BungeeLamp;
import revxrsal.commands.bungee.actor.BungeeCommandActor;

import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;

public class VersionConnector extends Plugin {

    private boolean enabled;
    private ConnectorConfigService configService;
    private ForgeDetector forgeDetector;
    private ServerRouter serverRouter;

    @Override
    public void onEnable() {
        configService = new ConnectorConfigService(this);
        serverRouter = new ServerRouter(this);
        forgeDetector = new ForgeDetector(this);

        enabled = loadConfig();
        if (enabled) {
            registerListeners();
            forgeDetector.init();
        } else {
            getLogger().severe("VersionConnector couldn't load the configuration and will stay disabled.");
        }
        Lamp<BungeeCommandActor> lamp = BungeeLamp.builder (this).build ();
        lamp.register(
                new VersionConnectorCommand(this)
        );

    }

    private void registerListeners() {
        PlayerConnectListener playerConnectListener = new PlayerConnectListener(this, configService, serverRouter);
        ForgeBrandListener forgeBrandListener = new ForgeBrandListener(forgeDetector);
        ForgeDisconnectListener forgeDisconnectListener = new ForgeDisconnectListener(forgeDetector);
        getProxy().getPluginManager().registerListener(this, playerConnectListener);
        getProxy().getPluginManager().registerListener(this, forgeBrandListener);
        getProxy().getPluginManager().registerListener(this, forgeDisconnectListener);
    }

    public boolean loadConfig() {
        if (configService == null) {
            return false;
        }
        boolean loaded = configService.reload();
        enabled = loaded;
        return loaded;
    }

    public void handleForgeDetection(ProxiedPlayer player) {
        if (!enabled || player.getServer() == null) {
            return;
        }
        ConnectorInfo connectorInfo = configService.getConnectorMap().get(player.getServer().getInfo().getName().toLowerCase());
        if (connectorInfo == null) {
            return;
        }
        ServerInfo targetServer = serverRouter.selectServer(
                connectorInfo,
                player.getServer().getInfo(),
                getVersion(player),
                true,
                player.getModList(),
                configService.getStartBalancing()
        );
        if (targetServer != null && targetServer != player.getServer().getInfo()) {
            player.connect(targetServer);
        }
    }

    public int getVersion(ProxiedPlayer player) {
        if (forgeDetector != null) {
            return forgeDetector.getVersion(player);
        }
        return player.getPendingConnection().getVersion();
    }

    public boolean isForge(ProxiedPlayer player) {
        return forgeDetector != null && forgeDetector.isForge(player);
    }

    public void logDebug(String msg) {
        if (configService != null && configService.isDebug()) {
            getLogger().log(Level.INFO, msg);
        }
    }
    public boolean isEnabled() {
        return enabled;
    }
    public boolean isDebug() {
        return configService != null && configService.isDebug();
    }

    public Map<String, ConnectorInfo> getConnectorMap() {
        return configService != null ? configService.getConnectorMap() : Collections.emptyMap();
    }

    public int getStartBalancing() {
        return configService != null ? configService.getStartBalancing() : 0;
    }
}
