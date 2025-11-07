package dev.ogblackdiamond.proxymessages.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;


public final class Reload implements SimpleCommand {

    private Runnable reloadMethod;

    public Reload(Runnable reloadMethod) {
        this.reloadMethod = reloadMethod;
    }

    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();

        source.sendMessage(Component.text(
            "Reloading ProxyMessages!!",
            NamedTextColor.DARK_AQUA
        ));
        
        try {reloadMethod.run();}
        catch (Exception e) {
            System.out.println("[ProxyMessages]: Reload failed:");
            e.printStackTrace();
        }
    }
}
