package dev.ogblackdiamond.proxymessages;

import dev.ogblackdiamond.proxymessages.listener.MessageListener;

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

    MessageListener listener;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        listener = new MessageListener(this);

        Bukkit.getLogger().info("Thank you for using ProxyMessages");
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
