package de.themoep.versionconnector.listeners;

import de.themoep.versionconnector.forge.ForgeDetector;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.DefinedPacket;

public class ForgeBrandListener implements Listener {

    private final ForgeDetector forgeDetector;

    public ForgeBrandListener(ForgeDetector forgeDetector) {
        this.forgeDetector = forgeDetector;
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer)
                || !"minecraft:brand".equals(event.getTag())
                || event.getData().length == 0) {
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        if (player.isForgeUser()) {
            return;
        }

        ByteBuf in = Unpooled.wrappedBuffer(event.getData());
        String brand = "";
        try {
            brand = DefinedPacket.readString(in);
        } catch (Exception e) {
            forgeDetector.getPlugin().logDebug("Invalid brand data sent! (length: " + event.getData().length + ") " + e.getMessage());
        } finally {
            in.release();
        }

        if ("forge".equalsIgnoreCase(brand)) {
            forgeDetector.markForge(player);
        }
    }
}
