package dev.ogblackdiamond.proxymessages.commands;

import java.io.IOException;
import java.net.Proxy;
import java.util.function.Consumer;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import dev.ogblackdiamond.proxymessages.ProxyMessages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;


public final class Reload implements SimpleCommand {

    private Consumer<Integer> reloadMethod;

    public Reload(Consumer<Integer> reloadMethod) {
        this.reloadMethod = reloadMethod;
    }

    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();

        source.sendMessage(Component.text(
            "Reloading ProxyMessages!!",
            NamedTextColor.DARK_AQUA
        ));
        
        try {reloadMethod.accept(0);}
        catch (Exception e) {e.printStackTrace();}
    }
}
