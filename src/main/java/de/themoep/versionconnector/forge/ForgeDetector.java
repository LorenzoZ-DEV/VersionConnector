package de.themoep.versionconnector.forge;

import de.themoep.versionconnector.VersionConnector;
import lombok.Getter;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import us.myles.ViaVersion.api.Via;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks Forge-capable players and exposes helper getters.
 */
@Getter
public class ForgeDetector {

    private final VersionConnector plugin;
    private final Map<UUID, Boolean> forgePlayers = new ConcurrentHashMap<>();
    private boolean viaVersionAvailable;

    public ForgeDetector(VersionConnector plugin) {
        this.plugin = plugin;
    }

    public void init() {
        viaVersionAvailable = plugin.getProxy().getPluginManager().getPlugin("ViaVersion") != null;
    }

    public int getVersion(ProxiedPlayer player) {
        if (viaVersionAvailable) {
            return Via.getAPI().getPlayerVersion(player.getUniqueId());
        }
        return player.getPendingConnection().getVersion();
    }

    public boolean isForge(ProxiedPlayer player) {
        return player.isForgeUser() || forgePlayers.getOrDefault(player.getUniqueId(), false);
    }

    public void markForge(ProxiedPlayer player) {
        forgePlayers.put(player.getUniqueId(), true);
        plugin.handleForgeDetection(player);
    }

    public void forgetPlayer(UUID uuid) {
        forgePlayers.remove(uuid);
    }
}
