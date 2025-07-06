package dev.ogblackdiamond.proxymessages;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;

/**
 *  Main class for the plugin, sets up the listener and removes default join and leave messages.
 */
public class ProxyMessages extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "proxymessages:main");

        Bukkit.getPluginManager().registerEvents(this, this);

        Bukkit.getLogger().info("[ProxyMessages] Thank you for using ProxyMessages");
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.joinMessage(null);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        event.quitMessage(null);
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncChatEvent event) {
        
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF(event.getPlayer().getName());
        UUID playerUUID = event.getPlayer().getUniqueId();
        out.writeLong(playerUUID.getLeastSignificantBits());
        out.writeLong(playerUUID.getMostSignificantBits());
        out.writeUTF(event.signedMessage().message());

        event.getPlayer().sendPluginMessage(this, "proxymessages:main", out.toByteArray());

        event.setCancelled(true);

    }
}
