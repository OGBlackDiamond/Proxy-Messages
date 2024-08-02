package dev.ogblackdiamond.proxymessages;

import dev.ogblackdiamond.proxymessages.listener.MessageListener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteStreams;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.audience.Audience;

import com.google.common.io.ByteArrayDataInput;

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
