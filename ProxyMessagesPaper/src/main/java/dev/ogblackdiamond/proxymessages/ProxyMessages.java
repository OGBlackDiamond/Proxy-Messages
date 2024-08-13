package dev.ogblackdiamond.proxymessages;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *  Main class for the plugin, sets up the listener and removes default join and leave messages.
 */
public class ProxyMessages extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        Bukkit.getLogger().info("[ProxyMessages] Thank you for using ProxyMessages");
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.joinMessage();
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        event.quitMessage();
    }
}
