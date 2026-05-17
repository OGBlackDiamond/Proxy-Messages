package dev.ogblackdiamond.proxymessages;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.clip.placeholderapi.PlaceholderAPI;

/**
 *  Main class for the plugin, sets up the listener and removes default join and leave messages.
 */
public class ProxyMessages extends JavaPlugin implements PluginMessageListener, Listener {

    boolean cancelPlayerMessages = true;

    boolean papiEnabled = false;

    private final String channelMain = "proxymessages:main";
    private final String channelPapi = "proxymessages:papi";


    @Override
    public void onEnable() {

        papiEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, channelMain);
        this.getServer().getMessenger().registerIncomingPluginChannel(this, channelMain, this);

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, channelPapi);
        this.getServer().getMessenger().registerIncomingPluginChannel(this, channelPapi, this);

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

    @EventHandler(ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncChatEvent event) {

        if (!cancelPlayerMessages) return;

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        UUID playerUUID = event.getPlayer().getUniqueId();
        out.writeLong(playerUUID.getLeastSignificantBits());
        out.writeLong(playerUUID.getMostSignificantBits());
        out.writeUTF(event.signedMessage().message());

        event.getPlayer().sendPluginMessage(this, channelMain, out.toByteArray());

        event.setCancelled(true);

    }


	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {

        if (channel.equalsIgnoreCase(channelMain)) handlePluginMessageMain(message);
        else if (channel.equalsIgnoreCase(channelPapi)) handlePluginMessagePapi(message, player);

	}

    private void handlePluginMessageMain(byte[] message) {
        ByteArrayDataInput data = ByteStreams.newDataInput(message);
        cancelPlayerMessages = data.readBoolean();
    }

    private void handlePluginMessagePapi(byte[] message, Player player) {

        ByteArrayDataInput data = ByteStreams.newDataInput(message);
        String placeholder = data.readUTF();


        if (papiEnabled)
            placeholder = PlaceholderAPI.setPlaceholders(player, placeholder);


        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        UUID playerUUID = player.getUniqueId();
        out.writeLong(playerUUID.getLeastSignificantBits());
        out.writeLong(playerUUID.getMostSignificantBits());
        out.writeUTF(placeholder);

        player.sendPluginMessage(this, channelPapi, out.toByteArray());
    }

}
