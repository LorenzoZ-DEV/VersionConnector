package de.themoep.versionconnector.listeners;

import de.themoep.versionconnector.forge.ForgeDetector;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ForgeDisconnectListener implements Listener {

    private final ForgeDetector forgeDetector;

    public ForgeDisconnectListener(ForgeDetector forgeDetector) {
        this.forgeDetector = forgeDetector;
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        forgeDetector.forgetPlayer(event.getPlayer().getUniqueId());
    }
}

